package fitnesse.reporting.history;

import fitnesse.testsystems.ExecutionResult;
import fitnesse.util.Clock;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class PageHistory extends PageHistoryReader{
  private static final String TEST_RESULT_FILE_DATE_PATTERN = "yyyyMMddHHmmss";

  public static SimpleDateFormat getDateFormat() {
    SimpleDateFormat format = new SimpleDateFormat(TEST_RESULT_FILE_DATE_PATTERN);
    format.setTimeZone(Clock.currentTimeZone());
    return format;
  }

  private int failures = 0;
  private int passes = 0;
  private Date minDate = null;
  private Date maxDate = null;
  private int maxAssertions = 0;
  private BarGraph barGraph;
  private String fullPageName;
  private final Map<Date, TestResultRecord> testResultMap = new HashMap<>();
  private Map<Date, File> pageFiles = new HashMap<>();

  public PageHistory(File pageDirectory) {
    fullPageName = pageDirectory.getName();
    readHistoryFromPageDirectory(pageDirectory);
    compileBarGraph();
  }

  private void compileBarGraph() {
    List<Date> dates = new ArrayList<>(testResultMap.keySet());
    Collections.sort(dates, reverseChronologicalDateComparator());
    barGraph = new BarGraph();
    for (int i = 0; i < dates.size() && i < 20; i++) {
      Date date = dates.get(i);
      barGraph.addSummary(date, get(date));
    }
  }

  private Comparator<Date> reverseChronologicalDateComparator() {
    return new Comparator<Date>() {
      @Override
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

  @Override
  void processTestFile(TestResultRecord record) {
    Date date = record.getDate();
    addTestResult(record, date);
    countResult(record);
    setMinMaxDate(date);
    setMaxAssertions(record);
    pageFiles.put(date, record.getFile());
  }

  private void addTestResult(TestResultRecord record, Date date) {
    testResultMap.put(date, record);
  }

  private Date trimMilliseconds(Date date) {
    long milliseconds = date.getTime();
    long seconds = milliseconds / 1000;
    return new Date(seconds *1000);
  }

  public String getPageFileName(Date date){
    if (pageFiles.get(date) != null) {
      return pageFiles.get(date).getName();
    }
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
    ExecutionResult result = ExecutionResult.getExecutionResult(summary.getWikiPageName(), summary.toTestSummary());
    if (result == ExecutionResult.FAIL || result == ExecutionResult.ERROR)
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
    return new Date(minDate.getTime());
  }

  public Date getMaxDate() {
    return new Date(maxDate.getTime());
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
    SortedSet<Date> sortedDates = new TreeSet<>(Collections.reverseOrder());
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
    List<Date> dates = new ArrayList<>(dateSet);
    Collections.sort(dates);
    return dates.get(dates.size()-1);
  }

  public static String formatDate(String format, Date date) {
    SimpleDateFormat fmt = new SimpleDateFormat(format, Locale.US);
    return fmt.format(date);
  }

  public static class PassFailReport {
    private String date;
    private ExecutionResult result;

    public PassFailReport(Date date, ExecutionResult result) {
      this.date = getDateFormat().format(date);
      this.result = result;
    }

    public String getDate() {
      return date;
    }

    public ExecutionResult getResult() {
      return result;
    }

    public boolean isPass() {
      return result == ExecutionResult.PASS;
    }
  }

  public static class BarGraph {
    private Date startingDate;
    private Date endingDate;
    private List<PassFailReport> passFailList = new ArrayList<>();

    public Date getStartingDate() {
      return new Date(startingDate.getTime());
    }

    public String formatStartingDate(String format) {
      return formatDate(format, startingDate);
    }

    public String formatEndingDate(String format) {
      return formatDate(format, endingDate);
    }

    public Date getEndingDate() {
      return new Date(endingDate.getTime());
    }

    public void addSummary(Date date, TestResultRecord summary) {
      minMaxDate(summary);

      ExecutionResult result = ExecutionResult.getExecutionResult(summary.getWikiPageName(), summary.toTestSummary());

      passFailList.add(new PassFailReport(date, result));
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
        builder.append(report.isPass() ? "+" : "-");
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
