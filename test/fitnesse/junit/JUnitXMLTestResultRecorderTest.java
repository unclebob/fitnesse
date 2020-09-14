package fitnesse.junit;

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

  private static final StringBuilder xmlResult = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
    .append("<testsuite errors=\"")
    .append("%s")
    .append("\" skipped=\"")
    .append("%s")
    .append("\" tests=\"1\" time=\"1.275")
    .append("\" failures=\"")
    .append("%s")
    .append("\" name=\"myTestName\">")
    .append("<properties></properties>")
    .append("<testcase classname=\"myTestName")
    .append("\" time=\"1.275")
    .append("\" name=\"myTestName\">")
    .append("%s")
    .append("</testcase></testsuite>");

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
      String xmlResultOnSuccess = String.format(xmlResult.toString(), "0", "0", "0", "");

      // when the test result recorder is called
      jUnitXMLTestResultRecorder.recordTestResult(testName, 0, 0, 0, null, 1275);

      // then the correct report is written to disk
      assertEquals(xmlResultOnSuccess, readReportFile("TEST-" + testName + ".xml"));
    } catch (IOException e) {
      // and no exception is thrown
      fail("IOException was caught but should have never been thrown.");
    }
  }

  private String readReportFile(String reportName) throws IOException {
    return new String(Files.readAllBytes(new File(reportDir.getRoot(), reportName).toPath()), StandardCharsets.UTF_8);
  }
}
