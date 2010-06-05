package fitnesse.junit;

import util.TimeMeasurement;
import fitnesse.responders.run.CompositeExecutionLog;
import fitnesse.responders.run.ResultsListener;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestSystem;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class PrintTestListener implements ResultsListener {
  public void allTestingComplete() throws Exception {
    System.out.println("--complete--");
  }

  public void announceNumberTestsToRun(int testsToRun) {

  }

  public void errorOccured() {
  }

  public void newTestStarted(WikiPage test, TimeMeasurement timeMeasurement) throws Exception {

  }

  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log)
      throws Exception {

  }

  public void testComplete(WikiPage test, TestSummary testSummary, TimeMeasurement timeMeasurement) throws Exception {
    System.out.println(new WikiPagePath(test).toString() + " r " + testSummary.right + " w "
        + testSummary.wrong + " " + testSummary.exceptions);
  }

  public void testOutputChunk(String output) throws Exception {

  }

  public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner)
      throws Exception {
  }
}
