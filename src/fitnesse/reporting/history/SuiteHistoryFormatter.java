package fitnesse.reporting.history;

import java.io.IOException;
import java.io.Writer;

import fitnesse.reporting.SuiteExecutionReportFormatter;
import fitnesse.reporting.TestXmlFormatter;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.ExecutionLog;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import util.TimeMeasurement;
import fitnesse.FitNesseContext;
import fitnesse.wiki.WikiPage;

public class SuiteHistoryFormatter extends SuiteExecutionReportFormatter {
  private TestXmlFormatter.WriterFactory writerFactory;
  private TimeMeasurement suiteTime;
  private TestXmlFormatter testHistoryFormatter;

  public SuiteHistoryFormatter(FitNesseContext context, WikiPage page, TestXmlFormatter.WriterFactory source) {
    super(context, page);
    writerFactory = source;
  }

  @Override
  public void testSystemStarted(TestSystem testSystem) {
    if (suiteTime == null)
      suiteTime = new TimeMeasurement().start();
    super.testSystemStarted(testSystem);
  }

  @Override
  public void testSystemStopped(TestSystem testSystem, ExecutionLog executionLog, Throwable cause) {
    super.testSystemStopped(testSystem, executionLog, cause);
  }

  @Override
  public void testStarted(WikiTestPage test) {
    testHistoryFormatter = new TestXmlFormatter(context, test.getSourcePage(), writerFactory);
    testHistoryFormatter.testStarted(test);
    super.testStarted(test);
  }

  @Override
  public void testOutputChunk(String output) {
    testHistoryFormatter.testOutputChunk(output);
    super.testOutputChunk(output);
  }

  @Override
  public void testComplete(WikiTestPage test, TestSummary testSummary) throws IOException {
    testHistoryFormatter.testComplete(test, testSummary);
    testHistoryFormatter.close();
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
    super.close();
    if (testHistoryFormatter != null) {
      testHistoryFormatter.close();
    }
    Writer writer = writerFactory.getWriter(context, getPage(), getPageCounts(), suiteTime.startedAt());
    try {
      VelocityContext velocityContext = new VelocityContext();
      velocityContext.put("suiteExecutionReport", suiteExecutionReport);
      VelocityEngine velocityEngine = context.pageFactory.getVelocityEngine();
      Template template = velocityEngine.getTemplate("suiteHistoryXML.vm");
      template.merge(velocityContext, writer);
    } finally {
      writer.close();
    }
  }
}
