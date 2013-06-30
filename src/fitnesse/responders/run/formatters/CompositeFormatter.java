package fitnesse.responders.run.formatters;

import fitnesse.testrunner.CompositeExecutionLog;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.TestResult;
import fitnesse.testsystems.slim.tables.Assertion;
import fitnesse.wiki.WikiPage;
import util.TimeMeasurement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CompositeFormatter extends BaseFormatter {
  List<BaseFormatter> formatters = new ArrayList<BaseFormatter>();

  public void add(BaseFormatter formatter) {
    formatters.add(formatter);
  }

  @Override
  protected WikiPage getPage() {
    throw new RuntimeException("Should not get here.");
  }

  @Override
  public void errorOccured() {
    for (BaseFormatter formatter : formatters)
      formatter.errorOccured();
  }

  @Override
  public void announceNumberTestsToRun(int testsToRun) {
    for (BaseFormatter formatter : formatters)
      formatter.announceNumberTestsToRun(testsToRun);
  }

  @Override
  public void addMessageForBlankHtml() {
    for (BaseFormatter formatter : formatters)
      formatter.addMessageForBlankHtml();
  }

  @Override
  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) {
    for (BaseFormatter formatter : formatters)
      formatter.setExecutionLogAndTrackingId(stopResponderId, log);
  }

  @Override
  public void testSystemStarted(TestSystem testSystem) {
    for (BaseFormatter formatter : formatters)
      formatter.testSystemStarted(testSystem);
  }

  @Override
  public void newTestStarted(WikiTestPage test, TimeMeasurement timeMeasurement) throws IOException {
    for (BaseFormatter formatter : formatters)
      formatter.newTestStarted(test, timeMeasurement);
  }

  @Override
  public void testOutputChunk(String output) throws IOException {
    for (BaseFormatter formatter : formatters)
      formatter.testOutputChunk(output);
  }

  @Override
  public void testComplete(WikiTestPage test, TestSummary testSummary, TimeMeasurement timeMeasurement) throws IOException {
    for (BaseFormatter formatter : formatters)
      formatter.testComplete(test, testSummary, timeMeasurement);
  }

  @Override
  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws IOException {
    for (BaseFormatter formatter : formatters) {
      formatter.allTestingComplete(totalTimeMeasurement);
    }
  }

  @Override
  public int getErrorCount() {
    int exitCode = 0;
    for (BaseFormatter formatter : formatters)
      exitCode = Math.max(exitCode, formatter.getErrorCount());
    return exitCode;
  }

  @Override
  public void testAssertionVerified(Assertion assertion, TestResult testResult) {
    for (BaseFormatter formatter : formatters)
      formatter.testAssertionVerified(assertion, testResult);

  }

  @Override
  public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {
    for (BaseFormatter formatter : formatters)
      formatter.testExceptionOccurred(assertion, exceptionResult);
  }
}
