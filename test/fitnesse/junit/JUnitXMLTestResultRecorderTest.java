package fitnesse.junit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class JUnitXMLTestResultRecorderTest {

  @Rule
  public TemporaryFolder reportDir = new TemporaryFolder();

  private JUnitXMLReportHelper jUnitXMLReportHelper;

  // SUT
  private JUnitXMLTestResultRecorder jUnitXMLTestResultRecorder;

  @Before
  public void setUp() {
    jUnitXMLTestResultRecorder = new JUnitXMLTestResultRecorder(reportDir.getRoot());
    jUnitXMLReportHelper = new JUnitXMLReportHelper(reportDir.getRoot());
  }

  @Test
  public void recordTestResultOnSuccess() {
    try {
      // given a test name and a test success
      String testName = "myTestName";
      long executionTimeMillis = 1275;
      String xmlResultOnSuccess = jUnitXMLReportHelper.getXmlResultOnSuccess(testName, executionTimeMillis);

      // when the test result recorder is called
      jUnitXMLTestResultRecorder.recordTestResult(testName, 0, 0, 0, null, executionTimeMillis);

      // then the correct report is written to disk
      assertEquals(xmlResultOnSuccess, jUnitXMLReportHelper.readReportFile("TEST-" + testName + ".xml"));
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
      Throwable throwable = new AssertionError("Gallier approved detail message");
      String xmlResultOnSkipped = jUnitXMLReportHelper.getXmlResultOnSkipped(testName, executionTimeMillis, throwable);

      // when the test result recorder is called
      jUnitXMLTestResultRecorder.recordTestResult(testName, 1, 0, 0, throwable, executionTimeMillis);

      // then the correct report is written to disk
      assertEquals(xmlResultOnSkipped, jUnitXMLReportHelper.readReportFile("TEST-" + testName + ".xml"));
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
      Throwable throwable = new AssertionError("Gallier approved detail message");
      String xmlResultOnFailure = jUnitXMLReportHelper.getXmlResultOnFailure(testName, executionTimeMillis, throwable);

      // when the test result recorder is called
      jUnitXMLTestResultRecorder.recordTestResult(testName, 0, 1, 0, throwable, executionTimeMillis);

      // then the correct report is written to disk
      assertEquals(xmlResultOnFailure, jUnitXMLReportHelper.readReportFile("TEST-" + testName + ".xml"));
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
      Throwable throwable = new RuntimeException("Gallier approved detail message");
      String xmlResultOnError = jUnitXMLReportHelper.getXmlResultOnError(testName, executionTimeMillis, throwable);

      // when the test result recorder is called
      jUnitXMLTestResultRecorder.recordTestResult(testName, 0, 0, 1, throwable, executionTimeMillis);

      // then the correct report is written to disk
      assertEquals(xmlResultOnError, jUnitXMLReportHelper.readReportFile("TEST-" + testName + ".xml"));
    } catch (IOException e) {
      // and no exception is thrown
      fail("IOException was caught but should have never been thrown.");
    }
  }
}

