package fitnesse.responders.run;

import util.TimeMeasurement;

import java.io.IOException;

public class NullListener implements ResultsListener {
  @Override
  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws IOException {}

  @Override
  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) {}

  @Override
  public void announceNumberTestsToRun(int testsToRun) {}

  @Override
  public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) {}

  @Override
  public void newTestStarted(TestPage test, TimeMeasurement timeMeasurement) throws IOException {}

  @Override
  public void testOutputChunk(String output) throws IOException {}

  @Override
  public void testComplete(TestPage test, TestSummary testSummary, TimeMeasurement timeMeasurement) throws IOException {}

  @Override
  public void errorOccured() {}
}
