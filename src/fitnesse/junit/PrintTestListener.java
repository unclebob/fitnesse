package fitnesse.junit;

import fitnesse.responders.run.*;
import fitnesse.testsystems.CompositeExecutionLog;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.TestResult;
import fitnesse.testsystems.slim.tables.Assertion;
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
  public void testAssertionVerified(Assertion assertion, TestResult testResult) {
  }

  @Override
  public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {
  }

  @Override
  public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) {
  }
}
