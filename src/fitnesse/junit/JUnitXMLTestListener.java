package fitnesse.junit;

import fitnesse.testrunner.ResultsListener;
import fitnesse.testrunner.CompositeExecutionLog;
import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.ExecutionLog;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.wiki.WikiPagePath;
import util.TimeMeasurement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class JUnitXMLTestListener implements ResultsListener {
  
  private String outputPath;
  private TimeMeasurement timeMeasurement;

  public JUnitXMLTestListener(String outputPath) {
    this.outputPath=outputPath;
    new File(outputPath).mkdirs();
  }
  
  public void recordTestResult(String testName, TestSummary result, long executionTime) throws IOException {
    int errors = 0;
    int failures = 0;
    String failureXml = "";
    
    if (result.exceptions + result.wrong > 0) {
      failureXml = "<failure type=\"java.lang.AssertionError\" message=\"" + " exceptions: "
          + result.exceptions + " wrong: " + result.wrong + "\"></failure>";
      if (result.exceptions > 0)
        errors = 1;
      else
        failures = 1;
    }

    String resultXml = "<testsuite errors=\"" + errors + "\" skipped=\"0\" tests=\"1\" time=\""
        + executionTime / 1000d + "\" failures=\"" + failures + "\" name=\""
        + testName + "\">" + "<properties></properties>" + "<testcase classname=\""
        + testName + "\" time=\"" + executionTime / 1000d + "\" name=\""
        + testName + "\">" + failureXml + "</testcase>" + "</testsuite>";

    String finalPath = new File(outputPath, "TEST-" + testName + ".xml").getAbsolutePath();
    FileWriter fw = new FileWriter(finalPath);
    fw.write(resultXml);
    fw.close();
  }

  @Override
  public void allTestingComplete() {
  }

  @Override
  public void announceNumberTestsToRun(int testsToRun) {
  }

  @Override
  public void newTestStarted(WikiTestPage test) {
    timeMeasurement = new TimeMeasurement().start();
  }

  @Override
  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) {
  }

  @Override
  public void testComplete(WikiTestPage test, TestSummary testSummary) throws IOException {
    recordTestResult(new WikiPagePath(test.getSourcePage()).toString(), testSummary, timeMeasurement.elapsed());
  }

  @Override
  public void testOutputChunk(String output)  {
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
  public void testSystemStopped(TestSystem testSystem, ExecutionLog executionLog, Throwable cause) {
  }
}
