package fitnesse.responders.testHistory;

import fitnesse.responders.run.TestSummary;
import fitnesse.FitNesseContext;
import fitnesse.wiki.WikiPage;
import util.FileUtil;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PageHistory extends PageHistoryReader{
  private int failures = 0;
  private int passes = 0;
  private Date minDate = null;
  private Date maxDate = null;
  private int maxAssertions = 0;
  private BarGraph barGraph;
  private String fullPageName;
  private final HashMap<Date, TestResultRecord> testResultMap = new HashMap<Date, TestResultRecord>();
  private HashMap<Date, File> pageFiles = new HashMap<Date,File>();

  public PageHistory(File pageDirectory) {
    fullPageName = pageDirectory.getName();
    try {
      readHistoryFromPageDirectory(pageDirectory);
      compileBarGraph();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void compileBarGraph() {
    List<Date> dates = new ArrayList<Date>(testResultMap.keySet());
    Collections.sort(dates, reverseChronologicalDateComparator());
    barGraph = new BarGraph();
    for (int i = 0; i < dates.size() && i < 20; i++) {
      Date date = dates.get(i);
      barGraph.addSummary(date, get(date));
    }
  }

  private Comparator<Date> reverseChronologicalDateComparator() {
    return new Comparator<Date>() {
      public int compare(Date d1, Date d2) {
        long diff = d2.getTime() - d1.getTime();
        if (diff < 0)
          return -1;
        if (diff > 0)
          return 1;
        return 0;
      }
    };
  }

  void processTestFile(TestResultRecord record) throws ParseException {
    Date date = record.getDate();
    addTestResult(record, date);
    countResult(record);
    setMinMaxDate(date);
    setMaxAssertions(record);
    pageFiles.put(date, record.getFile());
  }

  private void addTestResult(TestResultRecord record, Date date) {
    Date keyDate = trimMilliseconds(date);

    testResultMap.put(date, record);
  }

  private Date trimMilliseconds(Date date) {
    long milliseconds = date.getTime();
    long seconds = milliseconds / 1000;
    return new Date(seconds *1000);
  }

  public String getPageFileName(Date date){
    if(pageFiles.get(date) != null)
    return pageFiles.get(date).getName();
    return null;
  }


  private void setMaxAssertions(TestResultRecord summary) {
    int assertions = summary.getRight() + summary.getWrong() + summary.getExceptions();
    maxAssertions = Math.max(maxAssertions, assertions);
  }


  private void setMinMaxDate(Date date) {
    if (minDate == null)
      minDate = maxDate = date;
    else if (date.getTime() > maxDate.getTime())
      maxDate = date;
    else if (date.getTime() < minDate.getTime())
      minDate = date;
  }

  private void countResult(TestResultRecord summary) {
    if (summary.getWrong() > 0 || summary.getExceptions() > 0 || summary.getRight() == 0)
      failures++;
    else
      passes++;
  }

  public int getFailures() {
    return failures;
  }

  public int getPasses() {
    return passes;
  }

  public Date getMinDate() {
    return minDate;
  }

  public Date getMaxDate() {
    return maxDate;
  }

  public BarGraph getBarGraph() {
    return barGraph;
  }

  public int size() {
    return testResultMap.size();
  }

  public TestResultRecord get(Date key) {
    return testResultMap.get(trimMilliseconds(key));
  }

  public int maxAssertions() {
    return maxAssertions;
  }

  public SortedSet<Date> datesInChronologicalOrder() {
    Set<Date> dates = testResultMap.keySet();
    SortedSet<Date> sortedDates = new TreeSet<Date>(Collections.reverseOrder());
    sortedDates.addAll(dates);
    return sortedDates;
  }

  public PassFailBar getPassFailBar(Date date, int maxUnits) {
    TestResultRecord summary = get(date);
    int fail = summary.getWrong() + summary.getExceptions();
    double unitsPerAssertion = (double)maxUnits/(double)maxAssertions;
    int unitsForThisTest = (int)Math.round((fail + summary.getRight()) * unitsPerAssertion);
    double doubleFailUnits = fail * unitsPerAssertion;
    int failUnits = (int) doubleFailUnits;

    if (Math.abs(doubleFailUnits - failUnits) > .001)
      failUnits++;
    int passUnits = unitsForThisTest - failUnits;
    return new PassFailBar(summary.getRight(), fail, passUnits, failUnits);
  }

  public String getFullPageName() {
    return fullPageName;
  }

  public Date getLatestDate() {
    Set<Date> dateSet = testResultMap.keySet();
    List<Date> dates = new ArrayList<Date>(dateSet);
    Collections.sort(dates);
    return dates.get(dates.size()-1);
  }

  public static String formatDate(String format, Date date) {
    SimpleDateFormat fmt = new SimpleDateFormat(format, Locale.US);
    return fmt.format(date);
  }

  public static class PassFailReport {
    private String date;
    private boolean pass;

    public PassFailReport(Date date, boolean pass) {
      SimpleDateFormat dateFormat = new SimpleDateFormat(TestHistory.TEST_RESULT_FILE_DATE_PATTERN);
      this.date = dateFormat.format(date);
      this.pass = pass;
    }

    public String getDate() {
      return date;
    }

    public boolean isPass() {
      return pass;
    }
  }

  public static class BarGraph {
    private Date startingDate;
    private Date endingDate;
    private List<PassFailReport> passFailList = new ArrayList<PassFailReport>();

    public Date getStartingDate() {
      return startingDate;
    }

    public String formatStartingDate(String format) {
      return formatDate(format, startingDate);
    }

    public String formatEndingDate(String format) {
      return formatDate(format, endingDate);
    }

    public Date getEndingDate() {
      return endingDate;
    }

    public void addSummary(Date date, TestResultRecord summary) {
      minMaxDate(summary);
      boolean pass = summary.getWrong() == 0 && summary.getExceptions() == 0 && summary.getRight() > 0;
      passFailList.add(new PassFailReport(date, pass));
    }

    private void minMaxDate(TestResultRecord summary) {
      Date date = summary.getDate();
      if (startingDate == null)
        startingDate = endingDate = date;
      else if (date.getTime() < startingDate.getTime())
        startingDate = date;
      else if (date.getTime() > endingDate.getTime())
        endingDate = date;
    }

    public int size() {
      return passFailList.size();
    }

    public PassFailReport getPassFail(int i) {
      return passFailList.get(i);
    }

    public PassFailReport[] passFailArray() {
      return passFailList.toArray(new PassFailReport[passFailList.size()]);
    }

    public String testString() {
      StringBuilder builder = new StringBuilder();
      for (PassFailReport report : passFailList) {
        builder.append(report.pass ? "+" : "-");
      }
      return builder.toString();
    }
  }

  public class PassFailBar {
    private int passUnits;
    private int failUnits;
    private int pass;
    private int fail;

    public PassFailBar(int pass, int fail, int passUnits, int failUnits) {
      this.pass = pass;
      this.fail = fail;
      this.passUnits = passUnits;
      this.failUnits = failUnits;
    }

    public int getPassUnits() {
      return passUnits;
    }

    public int getFailUnits() {
      return failUnits;
    }

    public int getPass() {
      return pass;
    }

    public int getFail() {
      return fail;
    }
  }
}
