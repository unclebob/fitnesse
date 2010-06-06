package fitnesse.junit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import util.TimeMeasurement;

import fitnesse.responders.run.CompositeExecutionLog;
import fitnesse.responders.run.ResultsListener;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestSystem;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

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
  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws Exception {
  }

  @Override
  public void announceNumberTestsToRun(int testsToRun) {
  }

  @Override
  public void errorOccured() {
  }

  @Override
  public void newTestStarted(WikiPage test, TimeMeasurement timeMeasurement) throws Exception {
  }

  @Override
  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log)
      throws Exception {
  }

  @Override
  public void testComplete(WikiPage test, TestSummary testSummary, TimeMeasurement timeMeasurement) throws Exception {
    recordTestResult(new WikiPagePath(test).toString(), testSummary, timeMeasurement.elapsed());
  }

  @Override
  public void testOutputChunk(String output) throws Exception {
  }

  @Override
  public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner)
      throws Exception {
  }
}
