package fitnesse.junit;

import java.io.Closeable;
import java.io.IOException;

import fitnesse.testrunner.TestsRunnerListener;
import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemListener;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

public class JUnitRunNotifierResultsListener
        implements TestSystemListener, TestsRunnerListener, Closeable {

  private final Class<?> mainClass;
  private final RunNotifier notifier;
  private int totalNumberOfTests;
  private int completedTests;
  private Throwable firstFailure;

  public JUnitRunNotifierResultsListener(RunNotifier notifier, Class<?> mainClass) {
    this.notifier = notifier;
    this.mainClass = mainClass;
  }

  @Override
  public void announceNumberTestsToRun(int testsToRun) {
    totalNumberOfTests = testsToRun;
  }

  @Override
  public void unableToStartTestSystem(String testSystemName, Throwable cause) throws IOException {
    notifyOfTestSystemException(testSystemName, cause);
  }

  @Override
  public void testStarted(TestPage test) {
    firstFailure = null;
    notifier.fireTestStarted(descriptionFor(test));
  }

  @Override
  public void testComplete(TestPage test, TestSummary testSummary) {
    increaseCompletedTests();
    if (firstFailure != null) {
      notifier.fireTestFailure(new Failure(descriptionFor(test), firstFailure));
    } else if (testSummary.getExceptions() > 0) {
      notifier.fireTestFailure(new Failure(descriptionFor(test), new Exception("Exception occurred on page " + test.getFullPath())));
    } else if (testSummary.getWrong() > 0) {
      notifier.fireTestFailure(new Failure(descriptionFor(test), new AssertionError("Test failures occurred on page " + test.getFullPath())));
    }
    fireTestFinishedFor(test);
  }

  private void fireTestFinishedFor(TestPage test) {
    notifier.fireTestFinished(descriptionFor(test));
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
    notifyOfTestSystemException(testSystem.getName(), cause);
  }

  @Override
  public void close() {
    if (completedTests != totalNumberOfTests) {
      String msg = String.format(
              "Not all tests executed. Completed %s of %s tests.",
              completedTests, totalNumberOfTests);
      Exception e = new Exception(msg);
      notifier.fireTestFailure(new Failure(Description.createSuiteDescription(mainClass), e));
    }
  }

  protected void notifyOfTestSystemException(String testSystemName, Throwable cause) {
    if (cause != null) {
      Exception e = new Exception("Exception while executing tests using: " + testSystemName, cause);
      notifier.fireTestFailure(new Failure(Description.createSuiteDescription(mainClass), e));
    }
  }

  private Description descriptionFor(TestPage test) {
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

  public Class<?> getMainClass() {
    return mainClass;
  }

  public RunNotifier getNotifier() {
    return notifier;
  }

  public int getTotalNumberOfTests() {
    return totalNumberOfTests;
  }

  protected void increaseCompletedTests() {
    completedTests++;
  }

  public int getCompletedTests() {
    return completedTests;
  }

  public Throwable getFirstFailure() {
    return firstFailure;
  }

  protected void setFirstFailure(Throwable firstFailure) {
    this.firstFailure = firstFailure;
  }
}
