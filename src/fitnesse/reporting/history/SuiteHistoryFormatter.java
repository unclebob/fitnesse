package fitnesse.reporting.history;

import java.io.IOException;
import java.io.Writer;

import fitnesse.reporting.PageHistoryFormatter;
import fitnesse.reporting.SuiteExecutionReportFormatter;
import fitnesse.reporting.XmlFormatter;
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
  private XmlFormatter.WriterFactory writerFactory;
  private TimeMeasurement suiteTime;
  private PageHistoryFormatter pageHistoryFormatter;

  public SuiteHistoryFormatter(FitNesseContext context, WikiPage page, XmlFormatter.WriterFactory source) {
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
    pageHistoryFormatter = new PageHistoryFormatter(context, test.getSourcePage(), writerFactory);
    pageHistoryFormatter.testStarted(test);
    super.testStarted(test);
  }

  @Override
  public void testOutputChunk(String output) {
    pageHistoryFormatter.testOutputChunk(output);
    super.testOutputChunk(output);
  }

  @Override
  public void testComplete(WikiTestPage test, TestSummary testSummary) throws IOException {
    pageHistoryFormatter.testComplete(test, testSummary);
    pageHistoryFormatter.close();
    pageHistoryFormatter = null;
    super.testComplete(test, testSummary);
  }

  @Override
  public void testAssertionVerified(Assertion assertion, TestResult testResult) {
    pageHistoryFormatter.testAssertionVerified(assertion, testResult);
    super.testAssertionVerified(assertion, testResult);
  }

  @Override
  public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {
    pageHistoryFormatter.testExceptionOccurred(assertion, exceptionResult);
    super.testExceptionOccurred(assertion, exceptionResult);
  }

  @Override
  public void errorOccurred(Throwable cause) {
    if (pageHistoryFormatter != null) {
      pageHistoryFormatter.errorOccurred(cause);
    }
    super.errorOccurred(cause);
  }

  @Override
  public void close() throws IOException {
    if (suiteTime == null) return;
    suiteTime.stop();
    super.close();
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
