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
 * <p><br>
 * Usage example:
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
    this.testResultRecorder = new JUnitXMLTestResultRecorder(reportsDir);
  }

  /**
   * Constructs a new JUnitXMLPerPageTestListener.
   * @param jUnitXMLTestResultRecorder the recorder used to record the test results
   */
  public JUnitXMLPerPageTestListener(JUnitXMLTestResultRecorder jUnitXMLTestResultRecorder) {
    this.testResultRecorder = jUnitXMLTestResultRecorder;
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
   * @param testDescriptor the descriptor of the test
   * @param result the result of the test
   */
  protected void testFailure(TestDescriptor testDescriptor, TestResult result) {
    String testName = testDescriptor.getName();
    Throwable throwable = result.getException();
    long executionTime = calculateExecutionTimeInSeconds(result);
    try {
      if (throwable instanceof AssertionError) {
        testResultRecorder.recordTestResult(testName, 0, 1, 0, throwable, executionTime);
      } else {
        testResultRecorder.recordTestResult(testName, 0, 0, 1, throwable, executionTime);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Processes a skipped test.
   * @param testDescriptor the descriptor of the test
   * @param result the result of the test
   */
  protected void testSkipped(TestDescriptor testDescriptor, TestResult result) {
    String testName = testDescriptor.getName();
    Throwable throwable = result.getException();
    long executionTime = calculateExecutionTimeInSeconds(result);
    try {
      if (throwable instanceof AssertionError) {
        testResultRecorder.recordTestResult(testName, 1, 0, 0, throwable, executionTime);
      } else {
        testResultRecorder.recordTestResult(testName, 0, 0, 1, throwable, executionTime);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Processes a test success
   * @param testDescriptor the descriptor of the test
   * @param result the result of the test
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
   * @param result the result of the test
   * @return execution time in milliseconds
   */
  protected long calculateExecutionTimeInSeconds(TestResult result) {
    return result.getEndTime() - result.getStartTime();
  }
}
