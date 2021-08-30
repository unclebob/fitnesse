package fitnesse.junit;

import java.io.Closeable;
import java.util.logging.Level;
import java.util.logging.Logger;

import fitnesse.slim.SlimException;
import fitnesse.slim.SlimServer;
import fitnesse.testrunner.TestsRunnerListener;
import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.testsystems.slim.results.SlimExceptionResult;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import static fitnesse.testsystems.ExecutionResult.getExecutionResult;

public class JUnitRunNotifierResultsListener
        implements TestSystemListener, TestsRunnerListener, Closeable {
  private static final Logger LOG = Logger.getLogger(JUnitRunNotifierResultsListener.class.getName());

  private final Class<?> mainClass;
  private final RunNotifier notifier;
  private final DescriptionFactory descriptionFactory;
  private int totalNumberOfTests;
  private int completedTests;
  private Throwable firstFailure;

  public JUnitRunNotifierResultsListener(RunNotifier notifier, Class<?> mainClass, DescriptionFactory descriptionFactory) {
    this.notifier = notifier;
    this.mainClass = mainClass;
    this.descriptionFactory = descriptionFactory;
  }

  @Override
  public void announceNumberTestsToRun(int testsToRun) {
    totalNumberOfTests = testsToRun;
  }

  @Override
  public void unableToStartTestSystem(String testSystemName, Throwable cause) {
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
    Description description = descriptionFor(test);


    if (firstFailure != null) {
      notifier.fireTestFailure(new Failure(description, firstFailure));
    } else if (testSummary.getExceptions() > 0) {
      notifier.fireTestFailure(new Failure(description, new Exception("Exception occurred on page " + test.getFullPath())));
    } else if (testSummary.getWrong() > 0) {
      notifier.fireTestFailure(new Failure(description, new AssertionError("Test failures occurred on page " + test.getFullPath())));
    } else if (getExecutionResult(testSummary) == ExecutionResult.IGNORE) {
      notifier.fireTestIgnored(description);
      return;
    }
    notifier.fireTestFinished(description);
  }

  @Override
  public void testOutputChunk(TestPage testPage, String output) {
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
    String message = exceptionResult.getMessage();
    if (message == null && exceptionResult instanceof SlimExceptionResult) {
      message = extractMessageFromSlimException((SlimExceptionResult) exceptionResult);
    }
    firstFailure(exceptionResult.getExecutionResult(), message);
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
    if (completedTests < totalNumberOfTests) {
      String msg = String.format(
              "Not all tests executed. Completed %s of %s tests.",
              completedTests, totalNumberOfTests);
      Exception e = new Exception(msg);
      notifier.fireTestFailure(new Failure(suiteDescription(), e));
    }
    if (completedTests > totalNumberOfTests) {
      if (LOG.isLoggable(Level.WARNING)) {
        String msg = String.format(
          "Too many tests completed. Completed %s of %s tests.",
          completedTests, totalNumberOfTests);
        LOG.log(Level.WARNING, msg);
      }
    }
  }

  protected String extractMessageFromSlimException(SlimExceptionResult slimExceptionResult) {
    String slimExceptionMessage = slimExceptionResult.getException();
    String result = slimExceptionMessage.replace(SlimServer.EXCEPTION_TAG, "");
    int index = result.indexOf("\n\tat fitnesse.slim.MethodExecutor.findAndInvoke(");
    if (index > 0) {
      result = result.substring(0, index);
    }
    return result;
  }

  protected void notifyOfTestSystemException(String testSystemName, Throwable cause) {
    if (cause != null) {
      Exception e = new Exception("Exception while executing tests using: " + testSystemName, cause);
      notifier.fireTestFailure(new Failure(suiteDescription(), e));
    }
  }

  private Description suiteDescription() {
    return getDescriptionFactory().createSuiteDescription(getMainClass());
  }

  protected Description descriptionFor(TestPage test) {
    return getDescriptionFactory().createDescription(getMainClass(), test);
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
    } else if (message == null) {
      firstFailure = new AssertionError();
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

  public DescriptionFactory getDescriptionFactory() {
    return descriptionFactory;
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
