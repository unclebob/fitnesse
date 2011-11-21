package fitnesse.responders.run.formatters;

import fitnesse.FitNesseContext;
import fitnesse.FitNesseVersion;
import fitnesse.responders.run.*;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.PathParser;

import java.util.List;

import util.TimeMeasurement;

public class SuiteExecutionReportFormatter extends BaseFormatter {
  private SuiteExecutionReport.PageHistoryReference referenceToCurrentTest;
  protected SuiteExecutionReport suiteExecutionReport;

  public SuiteExecutionReportFormatter(FitNesseContext context, final WikiPage page) throws Exception {
    super(context, page);
    suiteExecutionReport = new SuiteExecutionReport();
    suiteExecutionReport.version = new FitNesseVersion().toString();
    suiteExecutionReport.rootPath = this.page.getName();
  }

  @Override
  public void writeHead(String pageType) throws Exception {
  }

  @Override
  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) throws Exception {
  }

  @Override
  public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) throws Exception {
  }

  @Override
  public void newTestStarted(TestPage test, TimeMeasurement timeMeasurement) throws Exception {
    String pageName = PathParser.render(test.getSourcePage().getPageCrawler().getFullPath(test.getSourcePage()));
    referenceToCurrentTest = new SuiteExecutionReport.PageHistoryReference(pageName, timeMeasurement.startedAt(), timeMeasurement.elapsed());
  }

  @Override
  public void testOutputChunk(String output) throws Exception {
  }

  public String getRootPageName() {
     return suiteExecutionReport.getRootPath();
  }

  public String getFitNesseVersion() {
    return new FitNesseVersion().toString();
  }

  @Override
  public void testComplete(TestPage test, TestSummary testSummary, TimeMeasurement timeMeasurement) throws Exception {
    referenceToCurrentTest.setTestSummary(testSummary);
    referenceToCurrentTest.setRunTimeInMillis(timeMeasurement.elapsed());
    suiteExecutionReport.addPageHistoryReference(referenceToCurrentTest);
    suiteExecutionReport.tallyPageCounts(testSummary);
	failCount+=testSummary.wrong;
	failCount+=testSummary.exceptions; 
  }

  public List<SuiteExecutionReport.PageHistoryReference> getPageHistoryReferences() {
    return suiteExecutionReport.getPageHistoryReferences();
  }

  @Override
  public int getErrorCount() {
   return getPageCounts().wrong + getPageCounts().exceptions;
  }

  @Override
  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws Exception {
    super.allTestingComplete(totalTimeMeasurement);
    suiteExecutionReport.setTotalRunTimeInMillis(totalTimeMeasurement);
  }
  
  public TestSummary getPageCounts() {
   return suiteExecutionReport.getFinalCounts();
 }

}
