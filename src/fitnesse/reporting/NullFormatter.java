package fitnesse.reporting;

import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.TestResult;
import fitnesse.FitNesseContext;
import fitnesse.testrunner.CompositeExecutionLog;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.wiki.WikiPage;

public class NullFormatter extends BaseFormatter {
  NullFormatter(FitNesseContext context, WikiPage page) {
    super(context, page);
  }

  @Override
  public void announceNumberTestsToRun(int testsToRun) {
  }

  @Override
  public void errorOccurred(Throwable cause) {
  }

  @Override
  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) {
  }

  @Override
  public void testSystemStarted(TestSystem testSystem) {
  }

  @Override
  public void newTestStarted(WikiTestPage test) {
  }

  @Override
  public void testOutputChunk(String output) {
  }

  @Override
  public void testAssertionVerified(Assertion assertion, TestResult testResult) {
  }

  @Override
  public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {
  }

  @Override
  public void testComplete(WikiTestPage test, TestSummary testSummary) {
  }
}

