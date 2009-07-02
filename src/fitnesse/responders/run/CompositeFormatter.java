package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.wiki.WikiPage;

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

  public void newTestStarted(WikiPage test) throws Exception {
    for (BaseFormatter formatter : formatters)
      formatter.newTestStarted(test);
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

  public int allTestingComplete() throws Exception {
    int exitCode = 0;
    for (BaseFormatter formatter : formatters)
      exitCode = Math.max(exitCode, formatter.allTestingComplete());
    return exitCode;
  }
}
