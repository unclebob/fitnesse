package fitnesse.junit;

import org.apache.commons.text.StringEscapeUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class JUnitXMLReportHelper {

  private final File reportDir;

  JUnitXMLReportHelper(File reportDir) {
    this.reportDir = reportDir;
  }

  String readReportFile(String reportName) throws IOException {
    return new String(Files.readAllBytes(new File(this.reportDir, reportName).toPath()), StandardCharsets.UTF_8);
  }

  String getXmlResultOnSuccess(String testName, long executionTimeMillis) {
    return getXmlResult(testName, executionTimeMillis, 0, 0, 0, null);
  }

  String getXmlResultOnSkipped(String testName, long executionTimeMillis, Throwable throwable) {
    return getXmlResult(testName, executionTimeMillis, 1, 0, 0, throwable);
  }

  String getXmlResultOnFailure(String testName, long executionTimeMillis, Throwable throwable) {
    return getXmlResult(testName, executionTimeMillis, 0, 1, 0, throwable);
  }

  String getXmlResultOnError(String testName, long executionTimeMillis, Throwable throwable) {
    return getXmlResult(testName, executionTimeMillis, 0, 0, 1, throwable);
  }

  String getXmlResult(String testName, long executionTimeMillis, int skipped, int failures, int errors, Throwable throwable) {
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

  String getMessage(Throwable throwable) {
    String errorMessage = throwable.getMessage();
    return StringEscapeUtils.escapeXml10(errorMessage);
  }
}
