package fitnesse.junit;

import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.TestSystemListener;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

public class JUnitRunNotifierResultsListener implements TestSystemListener<WikiTestPage> {

  private final Class<?> mainClass;
  private final RunNotifier notifier;
  private Throwable firstFailure;

  public JUnitRunNotifierResultsListener(RunNotifier notifier, Class<?> mainClass) {
    this.notifier = notifier;
    this.mainClass = mainClass;
  }

  @Override
  public void testStarted(WikiTestPage test) {
    firstFailure = null;
    if (test.isTestPage()) {
      notifier.fireTestStarted(descriptionFor(test));
    }
  }

  @Override
  public void testComplete(WikiTestPage test, TestSummary testSummary) {
    if (firstFailure != null) {
      notifier.fireTestFailure(new Failure(descriptionFor(test), firstFailure));
    } else if (test.isTestPage()) {
      notifier.fireTestFinished(descriptionFor(test));
    }
  }

  @Override
  public void testOutputChunk(String output) {
  }

  @Override
  public void testAssertionVerified(Assertion assertion, TestResult testResult) {
    if (testResult != null &&
            testResult.doesCount() &&
            (testResult.getExecutionResult() == ExecutionResult.FAIL ||
                    testResult.getExecutionResult() == ExecutionResult.ERROR)) {
      firstFailure(testResult.getExecutionResult(), createMessage(testResult));
    }
  }

  @Override
  public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {
    firstFailure(exceptionResult.getExecutionResult(), exceptionResult.getMessage());
  }

  @Override
  public void testSystemStarted(TestSystem testSystem) {
  }

  @Override
  public void testSystemStopped(TestSystem testSystem, Throwable cause) {
  }

  private Description descriptionFor(WikiTestPage test) {
    return Description.createTestDescription(mainClass, test.getFullPath());
  }

  String createMessage(TestResult testResult) {
    if (testResult.hasActual() && testResult.hasExpected()) {
      return String.format("[%s] expected [%s]",
              testResult.getActual(),
              testResult.getExpected());
    } else if ((testResult.hasActual() || testResult.hasExpected()) && testResult.hasMessage()) {
      return String.format("[%s] %s",
              testResult.hasActual() ? testResult.getActual() : testResult.getExpected(),
              testResult.getMessage());
    }
    return testResult.getMessage();
  }

  private void firstFailure(ExecutionResult executionResult, String message) {
    if (firstFailure != null) {
      return;
    }
    if (executionResult == ExecutionResult.ERROR) {
      firstFailure = new Exception(message);
    } else {
      firstFailure = new AssertionError(message);
    }
  }
}
