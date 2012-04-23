package fitnesse.junit;

import fitnesse.responders.run.*;
import util.TimeMeasurement;
import fitnesse.wiki.WikiPagePath;

public class PrintTestListener implements ResultsListener {
  @Override
  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) {
    System.out.println("--complete: " + totalTimeMeasurement.elapsedSeconds() + " seconds--");
  }

  @Override
  public void announceNumberTestsToRun(int testsToRun) {
  }

  @Override
  public void errorOccured() {
  }

  @Override
  public void newTestStarted(TestPage test, TimeMeasurement timeMeasurement) {
  }

  @Override
  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) {
  }

  @Override
  public void testComplete(TestPage test, TestSummary testSummary, TimeMeasurement timeMeasurement) {
    System.out.println(new WikiPagePath(test.getSourcePage()).toString() + " r " + testSummary.right + " w "
        + testSummary.wrong + " " + testSummary.exceptions 
        + " " + timeMeasurement.elapsedSeconds() + " seconds");
  }

  @Override
  public void testOutputChunk(String output) {
  }

  @Override
  public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) {
  }
}
