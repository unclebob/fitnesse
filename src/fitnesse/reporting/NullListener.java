package fitnesse.reporting;

import java.io.IOException;

import fitnesse.testrunner.CompositeExecutionLog;
import fitnesse.testrunner.ResultsListener;
import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testrunner.WikiTestPage;

public class NullListener implements ResultsListener {
  @Override
  public void allTestingComplete() throws IOException {}

  @Override
  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) {}

  @Override
  public void announceNumberTestsToRun(int testsToRun) {}

  @Override
  public void testSystemStarted(TestSystem testSystem) {}

  @Override
  public void newTestStarted(WikiTestPage test) throws IOException {}

  @Override
  public void testOutputChunk(String output) throws IOException {}

  @Override
  public void testAssertionVerified(Assertion assertion, TestResult testResult) {}

  @Override
  public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {}

  @Override
  public void testComplete(WikiTestPage test, TestSummary testSummary) throws IOException {}

  @Override
  public void errorOccurred(Throwable cause) {}
}
