package fitnesse.junit;

import java.io.Closeable;
import java.util.logging.Logger;

import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.wiki.WikiPagePath;
import fitnesse.util.TimeMeasurement;

public class PrintTestListener implements TestSystemListener<WikiTestPage>, Closeable {
  private static final Logger LOG = Logger.getLogger(PrintTestListener.class.getName());

  private TimeMeasurement timeMeasurement;
  private TimeMeasurement totalTimeMeasurement = new TimeMeasurement().start();

  @Override
  public void close() {
    LOG.info("--complete: " + totalTimeMeasurement.elapsedSeconds() + " seconds--");
  }

  @Override
  public void testStarted(WikiTestPage test) {
    timeMeasurement = new TimeMeasurement().start();
  }

  @Override
  public void testComplete(WikiTestPage test, TestSummary testSummary) {
    LOG.info(new WikiPagePath(test.getSourcePage()).toString() + " r " + testSummary.getRight() + " w "
        + testSummary.getWrong() + " " + testSummary.getExceptions()
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
  public void testSystemStopped(TestSystem testSystem, Throwable cause) {
  }

}
