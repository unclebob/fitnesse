package fitnesse.junit;

import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.TestListener;
import org.gradle.api.tasks.testing.TestResult;

import java.io.File;
import java.io.IOException;

/**
 * Gradle TestListener to be used during integration test executing FitNesse pages.
 * It will create a result XML file per FitNesse page, instead of default behavior that creates only
 * 1 file per Java test runner class (and we have only 1 class that runs all pages).
 * This allows build servers to report progress during the run.
 * The page names are used as test names, the Java class executing them is ignored.
 * <p/>
 * Usage example:<br/>
 * <pre>
 * {@code
 * buildscript {
 *   ...
 *   dependencies {
 *     classpath "org.fitnesse:fitnesse:20200911"
 *   }
 * }
 * ...
 * integrationtest {
 *   addTestListener(new JUnitXMLPerPageTestListener(reports.junitXml.destination) as TestListener)
 *   ...
 * }
 * }
 * </pre>
 * NOTE: The standard JUnit xml reports have to be disabled via <code>junitXml.enabled = false</code>
 */
public class JUnitXMLPerPageTestListener implements TestListener {

  private final JUnitXMLTestResultRecorder testResultRecorder;

  /**
   * Constructs a new JUnitXMLPerPageTestListener.
   * @param reportsDir directory the reports are written to (eg. <code>reports.junitXml.destination</code>)
   */
  public JUnitXMLPerPageTestListener(File reportsDir) {
    testResultRecorder = new JUnitXMLTestResultRecorder(reportsDir);
  }

  @Override
  public void beforeSuite(TestDescriptor testDescriptor) {
    // Nothing to do
  }

  @Override
  public void afterSuite(TestDescriptor testDescriptor, TestResult testResult) {
    // Nothing to do
  }

  @Override
  public void beforeTest(TestDescriptor testDescriptor) {
    // Nothing to do
  }

  @Override
  public void afterTest(TestDescriptor testDescriptor, TestResult testResult) {
    switch (testResult.getResultType()) {
    case SUCCESS:
      testSuccess(testDescriptor, testResult);
      break;
    case FAILURE:
      testFailure(testDescriptor, testResult);
      break;
    case SKIPPED:
      testSkipped(testDescriptor, testResult);
      break;
    default:
      throw new UnsupportedOperationException(
        "ResultType " + testResult.getResultType() + " received. Only SUCCESS, SKIPPED and FAILURE are supported!");
    }
  }

  /**
   * Processes a test failure.
   * @param testDescriptor
   * @param result
   */
  protected void testFailure(TestDescriptor testDescriptor, TestResult result) {
    try {
      if (result.getExceptions() instanceof AssertionError)
        testResultRecorder.recordTestResult(testDescriptor.getName(), 0, 1, 0, result.getException(), calculateExecutionTimeInSeconds(result));
      else
        testResultRecorder.recordTestResult(testDescriptor.getName(), 0, 0, 1, result.getException(), calculateExecutionTimeInSeconds(result));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * Processes a skipped test.
   * @param testDescriptor
   * @param result
   */
  protected void testSkipped(TestDescriptor testDescriptor, TestResult result) {
    try {
      if (result.getException() instanceof AssertionError)
        testResultRecorder.recordTestResult(testDescriptor.getName(), 1, 0, 0, result.getException(), calculateExecutionTimeInSeconds(result));
      else
        testResultRecorder.recordTestResult(testDescriptor.getName(), 0, 0, 1, result.getException(), calculateExecutionTimeInSeconds(result));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Processes a test success
   * @param testDescriptor
   * @param result
   */
  protected void testSuccess(TestDescriptor testDescriptor, TestResult result) {
    try {
      testResultRecorder.recordTestResult(testDescriptor.getName(), 0, 0, 0, null, calculateExecutionTimeInSeconds(result));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Calculates the execution time.
   * @param result
   * @return execution time in seconds
   */
  protected double calculateExecutionTimeInSeconds(TestResult result) {
    return (double) (result.getEndTime() - result.getStartTime()) / 1000;
  }
}
