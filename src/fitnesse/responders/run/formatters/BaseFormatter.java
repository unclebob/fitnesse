package fitnesse.responders.run.formatters;

import fitnesse.FitNesseContext;
import fitnesse.responders.run.CompositeExecutionLog;
import fitnesse.responders.run.ResultsListener;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestSystem;
import fitnesse.wiki.WikiPage;
import util.DateTimeUtil;
import util.TimeMeasurement;

public abstract class BaseFormatter implements ResultsListener {

  protected WikiPage page = null;
  protected FitNesseContext context;
  public static final BaseFormatter NULL = new NullFormatter();
  protected static long testTime;
  public static int finalErrorCount = 0;
  protected int testCount = 0;
  protected int failCount = 0;

  public abstract void writeHead(String pageType) throws Exception;

  protected BaseFormatter() {
  }

  protected BaseFormatter(FitNesseContext context, final WikiPage page) {
    this.page = page;
    this.context = context;
  }

  protected WikiPage getPage() {
    return page;
  }

  @Override
  public void errorOccured() {
    try {
      allTestingComplete();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void allTestingComplete() throws Exception {
    finalErrorCount = failCount;
  }

  @Override
  public void announceNumberTestsToRun(int testsToRun) {
  }

  @Override
  public void testComplete(WikiPage test, TestSummary summary, TimeMeasurement timeMeasurement) throws Exception {
    testCount++;
    if (summary.wrong > 0) {
      failCount++;
    }
    if (summary.exceptions > 0) {
      failCount++;
    }
  }

  public void addMessageForBlankHtml() throws Exception {
  }

  public int getErrorCount() {
    return 0;
  }

  public static void setTestTime(String dateString) {
    BaseFormatter.testTime = DateTimeUtil.getTimeFromString(dateString);
  }

  public static void clearTestTime() {
    testTime = 0;
  }
}

class NullFormatter extends BaseFormatter {
  NullFormatter() {
    super(null, null);
  }

  protected WikiPage getPage() {
    return null;
  }

  @Override
  public void errorOccured() {

  }

  @Override
  public void announceNumberTestsToRun(int testsToRun) {
  }

  @Override
  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) throws Exception {
  }

  @Override
  public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) throws Exception {
  }

  @Override
  public void newTestStarted(WikiPage test, TimeMeasurement timeMeasurement) throws Exception {
  }

  @Override
  public void testOutputChunk(String output) throws Exception {
  }

  @Override
  public void testComplete(WikiPage test, TestSummary testSummary, TimeMeasurement timeMeasurement) throws Exception {
  }

  public void writeHead(String pageType) throws Exception {
  }
}