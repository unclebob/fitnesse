package fitnesse.responders.run.formatters;

import fitnesse.wiki.WikiPage;
import fitnesse.responders.run.CompositeExecutionLog;
import fitnesse.responders.run.TestSystem;
import fitnesse.responders.run.TestSummary;

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
  public void addMessageForBlankHtml() throws Exception {
    for (BaseFormatter formatter : formatters)
      formatter.addMessageForBlankHtml();
  }

  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) throws Exception {
    for (BaseFormatter formatter : formatters)
      formatter.setExecutionLogAndTrackingId(stopResponderId, log);
  }

  public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) throws Exception {
    for (BaseFormatter formatter : formatters)
      formatter.testSystemStarted(testSystem, testSystemName, testRunner);
  }

  public void newTestStarted(WikiPage test, long time) throws Exception {
    for (BaseFormatter formatter : formatters)
      formatter.newTestStarted(test, time);
  }

  public void testOutputChunk(String output) throws Exception {
    for (BaseFormatter formatter : formatters)
      formatter.testOutputChunk(output);
  }

  public void testComplete(WikiPage test, TestSummary testSummary) throws Exception {
    for (BaseFormatter formatter : formatters)
      formatter.testComplete(test, testSummary);
  }

  public void writeHead(String pageType) throws Exception {
    for (BaseFormatter formatter : formatters)
      formatter.writeHead(pageType);
  }

  @Override
  public void allTestingComplete() throws Exception {
    for (BaseFormatter formatter : formatters)
      formatter.allTestingComplete();
  }

  public int getErrorCount() {
    int exitCode = 0;
    for (BaseFormatter formatter : formatters)
      exitCode = Math.max(exitCode, formatter.getErrorCount());
    return exitCode;
  }

}
