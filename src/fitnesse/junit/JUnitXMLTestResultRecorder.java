package fitnesse.junit;

import org.apache.commons.text.StringEscapeUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class JUnitXMLTestResultRecorder {

  private final File reportsDir;

  public JUnitXMLTestResultRecorder(File reportsDir) {
    this.reportsDir = reportsDir;
    this.reportsDir.mkdirs();
  }

  /**
   * Records result of single page (i.e. test).
   * @param testName name of test
   * @param skipped number of skipped tests
   * @param failures number of test failures
   * @param errors number of errors of test
   * @param throwable throwable from test
   * @param executionTime execution time in seconds
   */
  void recordTestResult(String testName, int skipped, int failures, int errors, Throwable throwable, double executionTime) throws
    IOException {
    String resultXml = generateResultXml(testName, skipped, failures, errors, throwable, executionTime);
    writeResult(testName, resultXml);
  }

  /**
   * Writes XML result to disk.
   * @param testName name of test
   * @param resultXml XML description of test outcome
   * @throws IOException if unable to write result
   */
  private void writeResult(String testName, String resultXml) throws IOException {
    String finalPath = getXmlFileName(testName);
    Writer fw = null;
    try {
      fw = new BufferedWriter(
        new OutputStreamWriter(
          new FileOutputStream(finalPath),
          StandardCharsets.UTF_8.displayName()));
      fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      fw.write(resultXml);
    } finally {
      if (fw != null) {
        fw.close();
      }
    }
  }

  /**
   * @param testName name of test
   * @return file name to use
   */
  private String getXmlFileName(String testName) {
    return new File(reportsDir, "TEST-" + testName + ".xml").getAbsolutePath();
  }

  /**
   * Creates XML string describing test outcome.
   * @param testName name of test
   * @param skipped number of skipped tests
   * @param failures number of test failures
   * @param errors number of errors of test
   * @param throwable throwable from test
   * @param executionTime execution time in seconds
   * @return XML description of test result
   */
  private String generateResultXml(String testName, int skipped, int failures, int errors, Throwable throwable, double executionTime) {
    String failureXml = "";

    if (throwable != null) {
      failureXml = "<failure type=\"" + throwable.getClass().getName()
        + "\" message=\"" + getMessage(throwable)
        + "\"></failure>";
    }

    return "<testsuite errors=\"" + errors
      + "\" skipped=\"" + skipped
      + "\" tests=\"1\" time=\"" + executionTime
      + "\" failures=\"" + failures
      + "\" name=\"" + testName + "\">"
      + "<properties></properties>"
      + "<testcase classname=\"" + testName
      + "\" time=\"" + executionTime
      + "\" name=\"" + testName + "\">"
      + failureXml
      + "</testcase>" + "</testsuite>";
  }

  private String getMessage(Throwable throwable) {
    String errorMessage = throwable.getMessage();
    return StringEscapeUtils.escapeXml10(errorMessage);
  }
}
