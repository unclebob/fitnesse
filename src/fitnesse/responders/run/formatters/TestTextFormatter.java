package fitnesse.responders.run.formatters;

import fitnesse.http.ChunkedResponse;
import fitnesse.responders.run.CompositeExecutionLog;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestSystem;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

import java.text.SimpleDateFormat;

public class TestTextFormatter extends BaseFormatter {
  public static int finalErrorCount = 0;
  private ChunkedResponse response;
  private int testCount = 0;
  private int failCount = 0;
  private String timeString;

  public TestTextFormatter(ChunkedResponse response) {
    this.response = response;
  }

  public void writeHead(String pageType) throws Exception {
  }

  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) throws Exception {
  }

  public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) throws Exception {
    response.add(String.format("\nStarting Test System: %s using %s.\n", testSystemName, testRunner));
  }

  public void newTestStarted(WikiPage page, long time) throws Exception {
    timeString = new SimpleDateFormat("HH:mm:ss").format(time);
  }

  private String getPath(WikiPage page) throws Exception {
    return PathParser.render(page.getPageCrawler().getFullPath(page));
  }

  public void testOutputChunk(String output) throws Exception {
  }

  public void testComplete(WikiPage page, TestSummary summary) throws Exception {
    response.add(String.format("%s %s R:%-4d W:%-4d I:%-4d E:%-4d %s\t(%s)\n",
      passFail(summary), timeString, summary.right, summary.wrong, summary.ignores, summary.exceptions, page.getName(), getPath(page)));
  }

  private String passFail(TestSummary summary) {
    testCount++;
    if (summary.wrong > 0){
      failCount++;
      return "F";
    }
    if (summary.exceptions > 0) {
      failCount++;
      return "X";
    }
    return ".";
  }

  @Override
  public void allTestingComplete() throws Exception {
    response.add(String.format("--------\n%d Tests,\t%d Failures.\n", testCount, failCount));
    finalErrorCount = failCount;
  }
}
