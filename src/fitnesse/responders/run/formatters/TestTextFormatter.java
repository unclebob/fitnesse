package fitnesse.responders.run.formatters;

import fitnesse.http.ChunkedResponse;
import fitnesse.responders.run.CompositeExecutionLog;
import fitnesse.responders.run.TestPage;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestSystem;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

import java.text.SimpleDateFormat;

import util.TimeMeasurement;

public class TestTextFormatter extends BaseFormatter {
  private ChunkedResponse response;

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

  public void newTestStarted(TestPage page, TimeMeasurement timeMeasurement) throws Exception {
  }

  private String getPath(WikiPage page) throws Exception {
    return PathParser.render(page.getPageCrawler().getFullPath(page));
  }

  public void testOutputChunk(String output) throws Exception {
  }

  public void testComplete(TestPage page, TestSummary summary, TimeMeasurement timeMeasurement) throws Exception {
    super.testComplete(page, summary, timeMeasurement);
    String timeString = new SimpleDateFormat("HH:mm:ss").format(timeMeasurement.startedAtDate());
    response.add(String.format("%s %s R:%-4d W:%-4d I:%-4d E:%-4d %s\t(%s)\t%.03f seconds\n",
      passFail(summary), timeString, summary.right, summary.wrong, summary.ignores, summary.exceptions, page.getName(), getPath(page.getSourcePage()), timeMeasurement.elapsedSeconds()));
  }

  private String passFail(TestSummary summary) {
    if (summary.wrong > 0){
      return "F";
    }
    if (summary.exceptions > 0) {
      return "X";
    }
    return ".";
  }

  @Override
  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws Exception {
    super.allTestingComplete(totalTimeMeasurement);
    response.add(String.format("--------\n%d Tests,\t%d Failures\t%.03f seconds.\n", testCount, failCount, totalTimeMeasurement.elapsedSeconds()));
  }
}
