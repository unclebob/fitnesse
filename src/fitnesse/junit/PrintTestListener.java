package fitnesse.junit;

import fitnesse.testrunner.CompositeExecutionLog;
import fitnesse.testrunner.ResultsListener;
import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testrunner.WikiTestPage;
import util.TimeMeasurement;
import fitnesse.wiki.WikiPagePath;

public class PrintTestListener implements ResultsListener {
  private TimeMeasurement timeMeasurement;
  private TimeMeasurement totalTimeMeasurement = new TimeMeasurement().start();

  @Override
  public void allTestingComplete() {
    System.out.println("--complete: " + totalTimeMeasurement.elapsedSeconds() + " seconds--");
  }

  @Override
  public void announceNumberTestsToRun(int testsToRun) {
  }

  @Override
  public void errorOccurred(Throwable cause) {
  }

  @Override
  public void newTestStarted(WikiTestPage test) {
    timeMeasurement = new TimeMeasurement().start();
  }

  @Override
  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) {
  }

  @Override
  public void testComplete(WikiTestPage test, TestSummary testSummary) {
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
  public void testSystemStarted(TestSystem testSystem) {
  }
}
