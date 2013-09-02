package fitnesse.reporting;

import fitnesse.testrunner.CompositeExecutionLog;
import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.wiki.WikiPage;

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
  public void errorOccurred(Throwable cause) {
    for (BaseFormatter formatter : formatters)
      formatter.errorOccurred(cause);
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
  public void testStarted(WikiTestPage test) throws IOException {
    for (BaseFormatter formatter : formatters)
      formatter.testStarted(test);
  }

  @Override
  public void testOutputChunk(String output) throws IOException {
    for (BaseFormatter formatter : formatters)
      formatter.testOutputChunk(output);
  }

  @Override
  public void testComplete(WikiTestPage test, TestSummary testSummary) throws IOException {
    for (BaseFormatter formatter : formatters)
      formatter.testComplete(test, testSummary);
  }

  @Override
  public void close() throws IOException {
    for (BaseFormatter formatter : formatters) {
      formatter.close();
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
