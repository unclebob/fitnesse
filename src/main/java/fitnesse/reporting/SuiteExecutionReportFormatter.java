package fitnesse.reporting;

import fitnesse.FitNesseContext;
import fitnesse.FitNesseVersion;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.*;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.PathParser;

import java.io.IOException;
import java.util.List;

import util.TimeMeasurement;

public class SuiteExecutionReportFormatter extends BaseFormatter {
  private SuiteExecutionReport.PageHistoryReference referenceToCurrentTest;
  protected SuiteExecutionReport suiteExecutionReport;
  private TimeMeasurement timeMeasurement;
  private final TimeMeasurement totalTimeMeasurement;

  public SuiteExecutionReportFormatter(FitNesseContext context, final WikiPage page) {
    super(context, page);
    suiteExecutionReport = new SuiteExecutionReport();
    suiteExecutionReport.version = new FitNesseVersion().toString();
    suiteExecutionReport.rootPath = this.page.getName();
    totalTimeMeasurement = new TimeMeasurement().start();
  }

  @Override
  public void testSystemStarted(TestSystem testSystem) {
  }

  @Override
  public void testStarted(WikiTestPage test) {
    String pageName = PathParser.render(test.getSourcePage().getPageCrawler().getFullPath());
    timeMeasurement = new TimeMeasurement().start();
    referenceToCurrentTest = new SuiteExecutionReport.PageHistoryReference(pageName, timeMeasurement.startedAt());
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
  public void testComplete(WikiTestPage test, TestSummary testSummary) throws IOException {
    timeMeasurement.stop();
    referenceToCurrentTest.setTestSummary(testSummary);
    referenceToCurrentTest.setRunTimeInMillis(timeMeasurement.elapsed());
    suiteExecutionReport.addPageHistoryReference(referenceToCurrentTest);
    suiteExecutionReport.tallyPageCounts(ExecutionResult.getExecutionResult(test.getName(), testSummary));
    super.testComplete(test, testSummary);
  }

  public List<SuiteExecutionReport.PageHistoryReference> getPageHistoryReferences() {
    return suiteExecutionReport.getPageHistoryReferences();
  }

  @Override
  public int getErrorCount() {
   return getPageCounts().wrong + getPageCounts().exceptions;
  }

  @Override
  public void close() throws IOException {
    totalTimeMeasurement.stop();
    super.close();
    suiteExecutionReport.setTotalRunTimeInMillis(totalTimeMeasurement);
  }
  
  public TestSummary getPageCounts() {
   return suiteExecutionReport.getFinalCounts();
 }

  public SuiteExecutionReport getSuiteExecutionReport() {
    return suiteExecutionReport;
  }
}
