package fitnesse.junit;

import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.ExecutionLog;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.wiki.WikiPagePath;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

public class JUnitRunNotifierResultsListener implements TestSystemListener<WikiTestPage> {

  private final Class<?> mainClass;
  private final RunNotifier notifier;

  public JUnitRunNotifierResultsListener(RunNotifier notifier, Class<?> mainClass) {
    this.notifier = notifier;
    this.mainClass = mainClass;
  }

  @Override
  public void testStarted(WikiTestPage test) {
    if (test.isTestPage()) {
      notifier.fireTestStarted(descriptionFor(test));
    }
  }

  private Description descriptionFor(WikiTestPage test) {
    return Description.createTestDescription(mainClass, new WikiPagePath(test.getSourcePage()).toString());
  }

  @Override
  public void testComplete(WikiTestPage test, TestSummary testSummary) {
    if (testSummary.wrong == 0 && testSummary.exceptions == 0) {
      if (test.isTestPage()) {
        notifier.fireTestFinished(descriptionFor(test));
      }
    } else {
      notifier.fireTestFailure(new Failure(descriptionFor(test), new AssertionError("wrong: "
          + testSummary.wrong + " exceptions: " + testSummary.exceptions)));
    }
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
  public void testSystemStarted(TestSystem testSystem) {
  }

  @Override
  public void testSystemStopped(TestSystem testSystem, ExecutionLog executionLog, Throwable cause) {
  }

}
