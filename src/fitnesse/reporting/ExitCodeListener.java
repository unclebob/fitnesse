package fitnesse.reporting;

import fitnesse.testsystems.*;

public class ExitCodeListener implements TestSystemListener {
  private int failCount;

  public int getFailCount() {
    return failCount;
  }

  @Override
  public void testSystemStarted(TestSystem testSystem) {
  }

  @Override
  public void testOutputChunk(TestPage testPage, String output) {
  }

  @Override
  public void testStarted(TestPage testPage) {
  }

  @Override
  public void testComplete(TestPage testPage, TestSummary testSummary) {
    if (testSummary.getWrong() > 0 || testSummary.getExceptions() > 0) {
      failCount++;
    }
  }

  @Override
  public void testSystemStopped(TestSystem testSystem, Throwable cause) {
    if (cause != null) {
      failCount++;
    }
  }

  @Override
  public void testAssertionVerified(Assertion assertion, TestResult testResult) {
  }

  @Override
  public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {
  }
}
