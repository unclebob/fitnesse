package fitnesse.reporting;

import java.io.IOException;
import java.text.SimpleDateFormat;

import fitnesse.testrunner.WikiTestPage;
import util.TimeMeasurement;
import fitnesse.http.ChunkedResponse;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class TestTextFormatter extends BaseFormatter {
  private ChunkedResponse response;
  private TimeMeasurement timeMeasurement;
  private TimeMeasurement totalTimeMeasurement;

  public TestTextFormatter(ChunkedResponse response) {
    this.response = response;
    this.totalTimeMeasurement = new TimeMeasurement().start();
  }

  @Override
  public void testSystemStarted(TestSystem testSystem) {
    response.add(String.format("\nStarting Test System: %s.\n", testSystem.getName()));
  }

  @Override
  public void testStarted(WikiTestPage page) {
    timeMeasurement = new TimeMeasurement().start();
  }

  private String getPath(WikiPage page) {
    return PathParser.render(page.getPageCrawler().getFullPath());
  }

  @Override
  public void testOutputChunk(String output) {
  }

  @Override
  public void testComplete(WikiTestPage page, TestSummary summary) throws IOException {
    timeMeasurement.stop();
    super.testComplete(page, summary);
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
  public void close() throws IOException {
    totalTimeMeasurement.stop();
    super.close();
    response.add(String.format("--------\n%d Tests,\t%d Failures\t%.03f seconds.\n", testCount, failCount, totalTimeMeasurement.elapsedSeconds()));
  }
}
