package fitnesse.junit;

import util.TimeMeasurement;
import fitnesse.responders.run.CompositeExecutionLog;
import fitnesse.responders.run.ResultsListener;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestSystem;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class PrintTestListener implements ResultsListener {
  @Override
  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws Exception {
    System.out.println("--complete: " + totalTimeMeasurement.elapsedSeconds() + " seconds--");
  }

  @Override
  public void announceNumberTestsToRun(int testsToRun) {
  }

  @Override
  public void errorOccured() {
  }

  @Override
  public void newTestStarted(WikiPage test, TimeMeasurement timeMeasurement) throws Exception {
  }

  @Override
  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log)
      throws Exception {
  }

  @Override
  public void testComplete(WikiPage test, TestSummary testSummary, TimeMeasurement timeMeasurement) throws Exception {
    System.out.println(new WikiPagePath(test).toString() + " r " + testSummary.right + " w "
        + testSummary.wrong + " " + testSummary.exceptions 
        + " " + timeMeasurement.elapsedSeconds() + " seconds");
  }

  @Override
  public void testOutputChunk(String output) throws Exception {
  }

  @Override
  public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner)
      throws Exception {
  }
}
