package fitnesse.responders.run.formatters;

import fitnesse.responders.run.*;
import fitnesse.wiki.WikiPage;
import fitnesse.FitNesseContext;
import util.DateTimeUtil;

public abstract class BaseFormatter implements ResultsListener {

  protected WikiPage page = null;
  protected FitNesseContext context;
  public static final BaseFormatter NULL = new NullFormatter();
  protected static long testTime;

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

  public void errorOccured() {
    try {
      allTestingComplete();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void allTestingComplete() throws Exception {
  }

  public void announceNumberTestsToRun(int testsToRun) {
  }

  public void addMessageForBlankHtml() throws Exception
  {}

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

  public void errorOccured() {

  }

  public void announceNumberTestsToRun(int testsToRun) {
  }

  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) throws Exception {
  }

  public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) throws Exception {
  }

  public void newTestStarted(WikiPage test, long time) throws Exception {
  }

  public void testOutputChunk(String output) throws Exception {
  }

  public void testComplete(WikiPage test, TestSummary testSummary) throws Exception {
  }

  public void writeHead(String pageType) throws Exception {
  }
}