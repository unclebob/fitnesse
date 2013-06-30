package fitnesse.responders.run.formatters;

import java.io.IOException;
import java.text.SimpleDateFormat;

import fitnesse.testrunner.WikiTestPage;
import util.TimeMeasurement;
import fitnesse.http.ChunkedResponse;
import fitnesse.testrunner.CompositeExecutionLog;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class TestTextFormatter extends BaseFormatter {
  private ChunkedResponse response;

  public TestTextFormatter(ChunkedResponse response) {
    this.response = response;
  }

  public void writeHead(String pageType) {
  }

  @Override
  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) {
  }

  @Override
  public void testSystemStarted(TestSystem testSystem) {
    response.add(String.format("\nStarting Test System: %s.\n", testSystem.getName()));
  }

  @Override
  public void newTestStarted(WikiTestPage page, TimeMeasurement timeMeasurement) {
  }

  private String getPath(WikiPage page) {
    return PathParser.render(page.getPageCrawler().getFullPath());
  }

  @Override
  public void testOutputChunk(String output) {
  }

  @Override
  public void testComplete(WikiTestPage page, TestSummary summary, TimeMeasurement timeMeasurement) throws IOException {
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
  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws IOException {
    super.allTestingComplete(totalTimeMeasurement);
    response.add(String.format("--------\n%d Tests,\t%d Failures\t%.03f seconds.\n", testCount, failCount, totalTimeMeasurement.elapsedSeconds()));
  }
}
