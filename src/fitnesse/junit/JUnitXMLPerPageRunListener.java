package fitnesse.junit;

import fitnesse.util.TimeMeasurement;
import org.apache.commons.text.StringEscapeUtils;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * JUnit RunListener to be used during integration test executing FitNesse pages.
 * It will create a result XML file per FitNesse page, instead of default (surefire/maven) behavior that creates only
 * 1 file per Java class (and we have only 1 class that runs all pages).
 * This allows build servers to report progress during the run.
 * The page names are used as test names, the Java class executing them is ignored.
 */
public class JUnitXMLPerPageRunListener extends RunListener {
  // default directory for maven-failsafe-plugin
  private final static String OUTPUT_PATH = "target/failsafe-reports/";
  private TimeMeasurement timeMeasurement;

  /**
   * Creates new.
   */
  public JUnitXMLPerPageRunListener() {
    new File(getOutputPath()).mkdirs();
  }

  @Override
  public void testStarted(Description description) throws Exception {
    timeMeasurement = new TimeMeasurement().start();
    super.testStarted(description);
  }

  @Override
  public void testFinished(Description description) throws Exception {
    super.testFinished(description);
    if (!timeMeasurement.isStopped()) {
      recordTestResult(description, null, getExecutionTime());
    }
  }

  @Override
  public void testFailure(Failure failure) throws Exception {
    super.testFailure(failure);
    recordTestResult(failure.getDescription(), failure.getException(), getExecutionTime());
  }

  protected double getExecutionTime() {
    double executionTime = 0;
    if (timeMeasurement != null) {
      executionTime = timeMeasurement.elapsedSeconds();
      if (!timeMeasurement.isStopped()) {
        timeMeasurement.stop();
      }
    }
    return executionTime;
  }

  /**
   * Records result of single page (i.e. test).
   * @param description JUnit test description
   * @param exception exception from test
   * @param executionTime execution time in seconds
   * @throws IOException if unable to write XML
   */
  protected void recordTestResult(Description description, Throwable exception, double executionTime) throws IOException {
    String testName = getTestName(description);
    String resultXml = generateResultXml(testName, exception, executionTime);
    writeResult(testName, resultXml);
  }

  /**
   * Creates XML string describing test outcome.
   * @param testName name of test.
   * @param exception exception from test
   * @param executionTime execution time in seconds
   * @return XML description of test result
   */
  protected String generateResultXml(String testName, Throwable exception, double executionTime) {
    int errors = 0;
    int failures = 0;
    String failureXml = "";

    if (exception != null) {
      failureXml = "<failure type=\"" + exception.getClass().getName()
        + "\" message=\"" + getMessage(exception)
        + "\"></failure>";
      if (exception instanceof AssertionError)
        failures = 1;
      else
        errors = 1;
    }

    return "<testsuite errors=\"" + errors + "\" skipped=\"0\" tests=\"1\" time=\""
      + executionTime + "\" failures=\"" + failures + "\" name=\""
      + testName + "\">" + "<properties></properties>" + "<testcase classname=\""
      + testName + "\" time=\"" + executionTime + "\" name=\""
      + testName + "\">" + failureXml + "</testcase>" + "</testsuite>";
  }

  protected String getMessage(Throwable exception) {
    String errorMessage = exception.getMessage();
    return StringEscapeUtils.escapeXml10(errorMessage);
  }

  /**
   * Writes XML result to disk.
   * @param testName name of test.
   * @param resultXml XML description of test outcome.
   * @throws IOException if unable to write result.
   */
  protected void writeResult(String testName, String resultXml) throws IOException {
    String finalPath = getXmlFileName(testName);
    Writer fw = null;
    try {
      fw = new BufferedWriter(
        new OutputStreamWriter(
          new FileOutputStream(finalPath),
          "UTF-8"));
      fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      fw.write(resultXml);
    } finally {
      if (fw != null) {
        fw.close();
      }
    }
  }

  /**
   * @param testName name of test.
   * @return file name to use.
   */
  protected String getXmlFileName(String testName) {
    // default pattern used by maven-failsafe-plugin
    return new File(getOutputPath(), "TEST-" + testName + ".xml").getAbsolutePath();
  }

  /**
   * @return directory to store test XMLs in.
   */
  protected String getOutputPath() {
    return OUTPUT_PATH;
  }

  /**
   * @param description JUnit description of test executed
   * @return name to use in report
   */
  protected String getTestName(Description description) {
    return description.getMethodName();
  }
}
