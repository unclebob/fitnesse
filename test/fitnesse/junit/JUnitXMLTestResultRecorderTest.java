package fitnesse.junit;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class JUnitXMLTestResultRecorderTest {

  @Rule
  public TemporaryFolder reportDir = new TemporaryFolder();

  // SUT
  JUnitXMLTestResultRecorder jUnitXMLTestResultRecorder;

  @Before
  public void setUp() {
    jUnitXMLTestResultRecorder = new JUnitXMLTestResultRecorder(reportDir.getRoot());
  }

  @Test
  public void recordTestResultOnSuccess() {
    try {
      // given a test name and a test success
      String testName = "myTestName";
      long executionTimeMillis = 1275;
      String xmlResultOnSuccess = getXmlResultOnSuccess(testName, executionTimeMillis);

      // when the test result recorder is called
      jUnitXMLTestResultRecorder.recordTestResult(testName, 0, 0, 0, null, executionTimeMillis);

      // then the correct report is written to disk
      assertEquals(xmlResultOnSuccess, readReportFile("TEST-" + testName + ".xml"));
    } catch (IOException e) {
      // and no exception is thrown
      fail("IOException was caught but should have never been thrown.");
    }
  }

  @Test
  public void recordTestResultOnSkipped() {
    try {
      // given a test success
      String testName = "myTestName";
      long executionTimeMillis = 1275;
      Throwable throwable = new AssertionError("Gallier rule ;-)");
      String xmlResultOnSkipped = getXmlResultOnSkipped(testName, executionTimeMillis, throwable);

      // when the test result recorder is called
      jUnitXMLTestResultRecorder.recordTestResult(testName, 1, 0, 0, throwable, executionTimeMillis);

      // then the correct report is written to disk
      assertEquals(xmlResultOnSkipped, readReportFile("TEST-" + testName + ".xml"));
    } catch (IOException e) {
      // and no exception is thrown
      fail("IOException was caught but should have never been thrown.");
    }
  }

  @Test
  public void recordTestResultOnFailure() {
    try {
      // given a test failure
      String testName = "myTestName";
      long executionTimeMillis = 1275;
      Throwable throwable = new AssertionError("Gallier rule ;-)");
      String xmlResultOnFailure = getXmlResultOnFailure(testName, executionTimeMillis, throwable);

      // when the test result recorder is called
      jUnitXMLTestResultRecorder.recordTestResult(testName, 0, 1, 0, throwable, executionTimeMillis);

      // then the correct report is written to disk
      assertEquals(xmlResultOnFailure, readReportFile("TEST-" + testName + ".xml"));
    } catch (IOException e) {
      // and no exception is thrown
      fail("IOException was caught but should have never been thrown.");
    }
  }

  @Test
  public void recordTestResultOnError() {
    try {
      // given a test error
      String testName = "myTestName";
      long executionTimeMillis = 1275;
      Throwable throwable = new RuntimeException("Gallier rule ;-)");
      String xmlResultOnError = getXmlResultOnError(testName, executionTimeMillis, throwable);

      // when the test result recorder is called
      jUnitXMLTestResultRecorder.recordTestResult(testName, 0, 0, 1, throwable, executionTimeMillis);

      // then the correct report is written to disk
      assertEquals(xmlResultOnError, readReportFile("TEST-" + testName + ".xml"));
    } catch (IOException e) {
      // and no exception is thrown
      fail("IOException was caught but should have never been thrown.");
    }
  }

  private String readReportFile(String reportName) throws IOException {
    return new String(Files.readAllBytes(new File(reportDir.getRoot(), reportName).toPath()), StandardCharsets.UTF_8);
  }

  private String getXmlResultOnSuccess(String testName, long executionTimeMillis) {
    return getXmlResult(testName, executionTimeMillis, 0, 0, 0, null);
  }

  private String getXmlResultOnSkipped(String testName, long executionTimeMillis, Throwable throwable) {
    return getXmlResult(testName, executionTimeMillis, 1, 0, 0, throwable);
  }

  private String getXmlResultOnFailure(String testName, long executionTimeMillis, Throwable throwable) {
    return getXmlResult(testName, executionTimeMillis, 0, 1, 0, throwable);
  }

  private String getXmlResultOnError(String testName, long executionTimeMillis, Throwable throwable) {
    return getXmlResult(testName, executionTimeMillis, 0, 0, 1, throwable);
  }

  private String getXmlResult(String testName, long executionTimeMillis, int skipped, int failures, int errors, Throwable throwable) {
    String failureXml = "";
    if (throwable != null) {
      failureXml = "<failure type=\"" + throwable.getClass().getName()
        + "\" message=\"" + getMessage(throwable)
        + "\"></failure>";
    }
    double executionTime = executionTimeMillis / 1000d;
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<testsuite errors=\""
      + String.valueOf(errors)
      + "\" skipped=\""
      + String.valueOf(skipped)
      + "\" tests=\"1\" time=\""
      + String.valueOf(executionTime)
      + "\" failures=\""
      + String.valueOf(failures)
      + "\" name=\""
      + testName
      + "\"><properties></properties>"
      + "<testcase classname=\""
      + testName
      + "\" time=\""
      + String.valueOf(executionTime)
      + "\" name=\""
      + testName
      + "\">"
      + failureXml
      + "</testcase></testsuite>";
  }

  private String getMessage(Throwable throwable) {
    String errorMessage = throwable.getMessage();
    return StringEscapeUtils.escapeXml10(errorMessage);
  }
}

