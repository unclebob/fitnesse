package fitnesse.reporting;

import java.io.IOException;

import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.ExecutionLog;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemListener;

public class NullListener implements TestSystemListener {

  @Override
  public void testSystemStarted(TestSystem testSystem) {}

  @Override
  public void testOutputChunk(String output) throws IOException {}

  @Override
  public void testStarted(TestPage testPage) throws IOException {}

  @Override
  public void testAssertionVerified(Assertion assertion, TestResult testResult) {}

  @Override
  public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {}

  @Override
  public void testComplete(TestPage test, TestSummary testSummary) throws IOException {}

  @Override
  public void testSystemStopped(TestSystem testSystem, ExecutionLog executionLog, Throwable cause) {}
}
