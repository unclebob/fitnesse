package fitnesse.reporting;

import java.io.IOException;

import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemListener;

public class ExitCodeListener implements TestSystemListener {
  private int failCount;

  public int getFailCount() {
    return failCount;
  }

  @Override
  public void testSystemStarted(TestSystem testSystem) {
  }

  @Override
  public void testOutputChunk(String output) throws IOException {
  }

  @Override
  public void testStarted(TestPage testPage) throws IOException {
  }

  @Override
  public void testComplete(TestPage testPage, TestSummary testSummary) throws IOException {
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
