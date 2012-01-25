package fitnesse.junit;

import fitnesse.responders.run.*;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import util.TimeMeasurement;

import fitnesse.wiki.WikiPagePath;

public class JUnitRunNotifierResultsListener implements ResultsListener {
  private final Class<?> mainClass;
  private final RunNotifier notifier;

  public JUnitRunNotifierResultsListener(RunNotifier notifier, Class<?> mainClass) {
    this.notifier = notifier;
    this.mainClass = mainClass;
  }

  @Override
  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) {
  }

  @Override
  public void announceNumberTestsToRun(int testsToRun) {
  }

  @Override
  public void errorOccured() {
  }

  @Override
  public void newTestStarted(TestPage test, TimeMeasurement timeMeasurement) {
    notifier.fireTestStarted(descriptionFor(test));
  }

  private Description descriptionFor(TestPage test) {
    return Description.createTestDescription(mainClass, new WikiPagePath(test.getSourcePage()).toString());
  }

  @Override
  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) {
  }

  @Override
  public void testComplete(TestPage test, TestSummary testSummary, TimeMeasurement timeMeasurement)  {
    if (testSummary.wrong == 0 && testSummary.exceptions == 0) {
      notifier.fireTestFinished(descriptionFor(test));
    } else {
      notifier.fireTestFailure(new Failure(descriptionFor(test), new AssertionError("wrong: "
          + testSummary.wrong + " exceptions: " + testSummary.exceptions)));
    }
  }

  @Override
  public void testOutputChunk(String output) {
  }

  @Override
  public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) {
  }
}
