package fitnesse.responders.run.formatters;

import fitnesse.FitNesseContext;
import fitnesse.FitNesseVersion;
import fitnesse.responders.run.*;
import fitnesse.testsystems.CompositeExecutionLog;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.PathParser;

import java.io.IOException;
import java.util.List;

import util.TimeMeasurement;

public class SuiteExecutionReportFormatter extends BaseFormatter {
  private SuiteExecutionReport.PageHistoryReference referenceToCurrentTest;
  protected SuiteExecutionReport suiteExecutionReport;

  public SuiteExecutionReportFormatter(FitNesseContext context, final WikiPage page) {
    super(context, page);
    suiteExecutionReport = new SuiteExecutionReport();
    suiteExecutionReport.version = new FitNesseVersion().toString();
    suiteExecutionReport.rootPath = this.page.getName();
  }

  @Override
  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) {
  }

  @Override
  public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) {
  }

  @Override
  public void newTestStarted(TestPage test, TimeMeasurement timeMeasurement) {
    String pageName = PathParser.render(test.getSourcePage().getPageCrawler().getFullPath(test.getSourcePage()));
    referenceToCurrentTest = new SuiteExecutionReport.PageHistoryReference(pageName, timeMeasurement.startedAt(), timeMeasurement.elapsed());
  }

  @Override
  public void testOutputChunk(String output) {
  }

  public String getRootPageName() {
     return suiteExecutionReport.getRootPath();
  }

  public String getFitNesseVersion() {
    return new FitNesseVersion().toString();
  }

  @Override
  public void testComplete(TestPage test, TestSummary testSummary, TimeMeasurement timeMeasurement) {
    referenceToCurrentTest.setTestSummary(testSummary);
    referenceToCurrentTest.setRunTimeInMillis(timeMeasurement.elapsed());
    suiteExecutionReport.addPageHistoryReference(referenceToCurrentTest);
    suiteExecutionReport.tallyPageCounts(ExecutionResult.getExecutionResult(test.getName(), testSummary));
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
  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws IOException {
    super.allTestingComplete(totalTimeMeasurement);
    suiteExecutionReport.setTotalRunTimeInMillis(totalTimeMeasurement);
  }
  
  public TestSummary getPageCounts() {
   return suiteExecutionReport.getFinalCounts();
 }

}
