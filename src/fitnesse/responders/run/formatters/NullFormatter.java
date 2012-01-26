package fitnesse.responders.run.formatters;

import fitnesse.responders.run.CompositeExecutionLog;
import fitnesse.responders.run.TestPage;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestSystem;
import fitnesse.wiki.WikiPage;
import util.TimeMeasurement;

public class NullFormatter extends BaseFormatter {
  NullFormatter() {
    super(null, null);
  }

  protected WikiPage getPage() {
    return null;
  }

  @Override
  public void announceNumberTestsToRun(int testsToRun) {
  }

  @Override
  public void errorOccured() {
  }

  @Override
  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) {
  }

  @Override
  public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) {
  }

  @Override
  public void newTestStarted(TestPage test, TimeMeasurement timeMeasurement) {
  }

  @Override
  public void testOutputChunk(String output) {
  }

  @Override
  public void testComplete(TestPage test, TestSummary testSummary, TimeMeasurement timeMeasurement) {
  }

  @Override
  public void writeHead(String pageType) throws Exception {
  }
}

