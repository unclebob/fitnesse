package fitnesse.responders.run.formatters;

import fitnesse.FitNesseContext;
import fitnesse.FitNesseVersion;
import fitnesse.responders.run.CompositeExecutionLog;
import fitnesse.responders.run.TestSystem;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.SuiteExecutionReport;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.PathParser;

import java.util.List;

public class SuiteExecutionReportFormatter extends BaseFormatter {
  private SuiteExecutionReport.PageHistoryReference referenceToCurrentTest;
  protected SuiteExecutionReport suiteExecutionReport;

  public SuiteExecutionReportFormatter(FitNesseContext context, final WikiPage page) throws Exception {
    super(context, page);
    suiteExecutionReport = new SuiteExecutionReport();
    suiteExecutionReport.version = new FitNesseVersion().toString();
    suiteExecutionReport.rootPath = this.page.getName();
  }

  public void writeHead(String pageType) throws Exception {
  }

  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) throws Exception {
  }

  public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) throws Exception {
  }

  public void newTestStarted(WikiPage test, long time) throws Exception {
    String pageName = PathParser.render(test.getPageCrawler().getFullPath(test));
    referenceToCurrentTest = new SuiteExecutionReport.PageHistoryReference(pageName, time);
  }

  public void testOutputChunk(String output) throws Exception {
  }

  public String getRootPageName() {
     return suiteExecutionReport.getRootPath();
  }

  public void testComplete(WikiPage test, TestSummary testSummary) throws Exception {
    referenceToCurrentTest.setTestSummary(testSummary);
    suiteExecutionReport.addPageHistoryReference(referenceToCurrentTest);
    suiteExecutionReport.tallyPageCounts(testSummary);
  }

  public List<SuiteExecutionReport.PageHistoryReference> getPageHistoryReferences() {
    return suiteExecutionReport.getPageHistoryReferences();
  }

  @Override
  public int getErrorCount() {
   return getPageCounts().wrong + getPageCounts().exceptions;
 }

  public TestSummary getPageCounts() {
   return suiteExecutionReport.getFinalCounts();
 }

}
