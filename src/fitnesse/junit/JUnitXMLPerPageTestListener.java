package fitnesse.junit;

import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.TestListener;
import org.gradle.api.tasks.testing.TestResult;

import java.io.File;
import java.io.IOException;

public class JUnitXMLPerPageTestListener implements TestListener {

  private final JUnitXMLTestResultRecorder testResultRecorder;

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

  protected void testSuccess(TestDescriptor testDescriptor, TestResult result) {
    try {
      testResultRecorder.recordTestResult(testDescriptor.getName(), 0, 0, 0, null, calculateExecutionTimeInSeconds(result));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  static double calculateExecutionTimeInSeconds(TestResult result) {
    return (double) (result.getEndTime() - result.getStartTime()) / 1000;
  }
}
