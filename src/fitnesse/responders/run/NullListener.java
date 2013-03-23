package fitnesse.responders.run;

import java.io.IOException;

import fitnesse.testsystems.CompositeExecutionLog;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.TestResult;
import fitnesse.testsystems.slim.tables.Assertion;
import util.TimeMeasurement;

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
  public void testAssertionVerified(Assertion assertion, TestResult testResult) {}

  @Override
  public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {}

  @Override
  public void testComplete(TestPage test, TestSummary testSummary, TimeMeasurement timeMeasurement) throws IOException {}

  @Override
  public void errorOccured() {}
}
