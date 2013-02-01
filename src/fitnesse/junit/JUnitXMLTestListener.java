package fitnesse.junit;

import fitnesse.responders.run.ResultsListener;
import fitnesse.responders.run.TestPage;
import fitnesse.testsystems.CompositeExecutionLog;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.TestResult;
import fitnesse.testsystems.slim.tables.Assertion;
import fitnesse.wiki.WikiPagePath;
import util.TimeMeasurement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class JUnitXMLTestListener implements ResultsListener {
  
  private String outputPath;

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
  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) {
  }

  @Override
  public void announceNumberTestsToRun(int testsToRun) {
  }

  @Override
  public void errorOccured() {
  }

  @Override
  public void newTestStarted(TestPage test, TimeMeasurement timeMeasurement) {
  }

  @Override
  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) {
  }

  @Override
  public void testComplete(TestPage test, TestSummary testSummary, TimeMeasurement timeMeasurement) throws IOException {
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
  public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) {
  }
}
