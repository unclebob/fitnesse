package fitnesse.reporting.history;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import fitnesse.reporting.BaseFormatter;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.ExecutionLog;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.wiki.PageType;
import fitnesse.wiki.PathParser;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import util.TimeMeasurement;
import fitnesse.FitNesseContext;
import fitnesse.wiki.WikiPage;

public class SuiteHistoryFormatter extends BaseFormatter implements Closeable {
  private SuiteExecutionReport.PageHistoryReference referenceToCurrentTest;
  private SuiteExecutionReport suiteExecutionReport;
  private final TimeMeasurement totalTimeMeasurement;
  private TestXmlFormatter.WriterFactory writerFactory;
  private TimeMeasurement suiteTime;
  private TestXmlFormatter testHistoryFormatter;

  public SuiteHistoryFormatter(FitNesseContext context, WikiPage page, TestXmlFormatter.WriterFactory source) {
    super(context, page);
    writerFactory = source;
    suiteExecutionReport = new SuiteExecutionReport(context.version, getPage().getPageCrawler().getFullPath().toString());
    totalTimeMeasurement = new TimeMeasurement().start();
  }

  @Override
  public void testSystemStarted(TestSystem testSystem) {
    if (suiteTime == null)
      suiteTime = new TimeMeasurement().start();
  }

  @Override
  public void testSystemStopped(TestSystem testSystem, ExecutionLog executionLog, Throwable cause) {
    super.testSystemStopped(testSystem, executionLog, cause);
  }

  @Override
  public void testStarted(WikiTestPage test) {
    String pageName = PathParser.render(test.getSourcePage().getPageCrawler().getFullPath());
    testHistoryFormatter = new TestXmlFormatter(context, test.getSourcePage(), writerFactory);
    testHistoryFormatter.testStarted(test);
    referenceToCurrentTest = new SuiteExecutionReport.PageHistoryReference(pageName, testHistoryFormatter.startedAt());
  }

  @Override
  public void testOutputChunk(String output) {
    testHistoryFormatter.testOutputChunk(output);
  }

  @Override
  public void testComplete(WikiTestPage test, TestSummary testSummary) throws IOException {
    testHistoryFormatter.testComplete(test, testSummary);
    testHistoryFormatter.close();
    referenceToCurrentTest.setTestSummary(testSummary);
    referenceToCurrentTest.setRunTimeInMillis(testHistoryFormatter.runTime());
    suiteExecutionReport.addPageHistoryReference(referenceToCurrentTest);
    suiteExecutionReport.tallyPageCounts(ExecutionResult.getExecutionResult(test.getName(), testSummary));
    testHistoryFormatter = null;
    super.testComplete(test, testSummary);
  }

  @Override
  public void testAssertionVerified(Assertion assertion, TestResult testResult) {
    testHistoryFormatter.testAssertionVerified(assertion, testResult);
    super.testAssertionVerified(assertion, testResult);
  }

  @Override
  public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {
    testHistoryFormatter.testExceptionOccurred(assertion, exceptionResult);
    super.testExceptionOccurred(assertion, exceptionResult);
  }

  @Override
  public void errorOccurred(Throwable cause) {
    if (testHistoryFormatter != null) {
      testHistoryFormatter.errorOccurred(cause);
    }
    super.errorOccurred(cause);
  }

  @Override
  public void close() throws IOException {
    if (suiteTime == null) return;
    suiteTime.stop();
    totalTimeMeasurement.stop();

    suiteExecutionReport.setTotalRunTimeInMillis(totalTimeMeasurement);

    if (testHistoryFormatter != null) {
      testHistoryFormatter.close();
    }

    if (PageType.fromWikiPage(getPage()) == PageType.SUITE) {
      Writer writer = writerFactory.getWriter(context, getPage(), getPageCounts(), suiteTime.startedAt());
      try {
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("suiteExecutionReport", getSuiteExecutionReport());
        VelocityEngine velocityEngine = context.pageFactory.getVelocityEngine();
        Template template = velocityEngine.getTemplate("suiteHistoryXML.vm");
        template.merge(velocityContext, writer);
      } finally {
        writer.close();
      }
    }
  }

  @Override
  public int getErrorCount() {
    return getPageCounts().getWrong() + getPageCounts().getExceptions();
  }

  public List<SuiteExecutionReport.PageHistoryReference> getPageHistoryReferences() {
    return suiteExecutionReport.getPageHistoryReferences();
  }

  public TestSummary getPageCounts() {
    return suiteExecutionReport.getFinalCounts();
  }

  public SuiteExecutionReport getSuiteExecutionReport() {
    return suiteExecutionReport;
  }
}
