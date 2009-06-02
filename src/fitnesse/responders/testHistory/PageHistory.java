package fitnesse.responders.testHistory;

import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.XmlFormatter;
import util.FileUtil;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PageHistory {
  private SimpleDateFormat dateFormat = new SimpleDateFormat(XmlFormatter.TEST_RESULT_FILE_DATE_PATTERN);
  public static final String TEST_FILE_FORMAT = "\\A\\d{14}_\\d+_\\d+_\\d+_\\d+(.xml)*\\Z";
  private int failures = 0;
  private int passes = 0;
  private Date minDate = null;
  private Date maxDate = null;
  private int maxAssertions = 0;
  private BarGraph barGraph;
  private String fullPageName;
  private final HashMap<Date, TestResultRecord> testResultMap = new HashMap<Date, TestResultRecord>();

  public PageHistory(File pageDirectory) {
    fullPageName = pageDirectory.getName();
    try {
      compileHistoryFromPageDirectory(pageDirectory);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void compileHistoryFromPageDirectory(File pageDirectory) throws ParseException {
    File[] resultFiles = FileUtil.getDirectoryListing(pageDirectory);
    for (File file : resultFiles)
      if (fileIsNotADirectoryAndIsValid(file))
        compileResultFileIntoHistory(file);
    compileBarGraph();
  }

  private boolean fileIsNotADirectoryAndIsValid(File file) {
    if(file.isDirectory())
      return false;
    if(!matchesPageHistoryFileFormat(file.getName()))
      return false;
    return true;

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

  private void compileResultFileIntoHistory(File file) throws ParseException {
    TestResultRecord record = buildTestResultRecord(file);
    testResultMap.put(record.getDate(), record);
    countResult(record);
    setMinMaxDate(record.getDate());
    setMaxAssertions(record);
  }

  private void setMaxAssertions(TestResultRecord summary) {
    int assertions = summary.getRight() + summary.getWrong() + summary.getExceptions();
    maxAssertions = Math.max(maxAssertions, assertions);
  }

  private TestResultRecord buildTestResultRecord(File file) throws ParseException {
    String parts[] = file.getName().split("_|\\.");
    Date date = dateFormat.parse(parts[0]);
    TestResultRecord testResultRecord = new TestResultRecord(
      file,
      date,
      Integer.parseInt(parts[1]),
      Integer.parseInt(parts[2]),
      Integer.parseInt(parts[3]),
      Integer.parseInt(parts[4]));
    return testResultRecord;
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
    return testResultMap.get(key);
  }

  public int maxAssertions() {
    return maxAssertions;
  }

  public SortedSet<Date> datesInChronologicalOrder() {
    Set<Date> dates = testResultMap.keySet();
    SortedSet<Date> sortedDates = new TreeSet<Date>(dates);
    return sortedDates;
  }

  public PassFailBar getPassFailBar(Date date, int maxUnits) {
    TestResultRecord summary = testResultMap.get(date);
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

  public static boolean matchesPageHistoryFileFormat(String pageHistoryFileName) {
    return pageHistoryFileName.matches(TEST_FILE_FORMAT);
  }

  public static class TestResultRecord extends TestSummary {
    private File file;
    private Date date;

    TestResultRecord(File file, Date date, int right, int wrong, int ignores, int exceptions) {
      super(right, wrong, ignores, exceptions);
      this.file = file;
      this.date = date;
    }

    public Date getDate() {
      return date;
    }

    public File getFile() {
      return file;
    }
  }

  public static String formatDate(String format, Date date) {
    SimpleDateFormat fmt = new SimpleDateFormat(format);
    return fmt.format(date);
  }

  public static class PassFailReport {
    private String date;
    private boolean pass;

    public PassFailReport(Date date, boolean pass) {
      SimpleDateFormat dateFormat = new SimpleDateFormat(XmlFormatter.TEST_RESULT_FILE_DATE_PATTERN);
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
