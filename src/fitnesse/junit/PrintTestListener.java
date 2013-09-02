package fitnesse.junit;

import java.io.Closeable;

import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.ExecutionLog;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.wiki.WikiPagePath;
import util.TimeMeasurement;

public class PrintTestListener implements TestSystemListener<WikiTestPage>, Closeable {
  private TimeMeasurement timeMeasurement;
  private TimeMeasurement totalTimeMeasurement = new TimeMeasurement().start();

  @Override
  public void close() {
    System.out.println("--complete: " + totalTimeMeasurement.elapsedSeconds() + " seconds--");
  }

  @Override
  public void testStarted(WikiTestPage test) {
    timeMeasurement = new TimeMeasurement().start();
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

  @Override
  public void testSystemStopped(TestSystem testSystem, ExecutionLog executionLog, Throwable cause) {
  }

}
