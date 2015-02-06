package fitnesse.reporting;

import java.io.Closeable;
import java.io.IOException;
import java.text.SimpleDateFormat;

import fitnesse.testrunner.WikiTestPage;
import fitnesse.util.TimeMeasurement;
import fitnesse.http.ChunkedResponse;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;

public class TestTextFormatter extends BaseFormatter implements Closeable {
  private ChunkedResponse response;
  private TimeMeasurement timeMeasurement;
  private TimeMeasurement totalTimeMeasurement;
  private int testCount = 0;
  private int failCount = 0;

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

  @Override
  public void testOutputChunk(String output) {
  }

  @Override
  public void testComplete(WikiTestPage page, TestSummary summary) throws IOException {
    timeMeasurement.stop();
    updateCounters(summary);
    String timeString = new SimpleDateFormat("HH:mm:ss").format(timeMeasurement.startedAtDate());
    response.add(String.format("%s %s R:%-4d W:%-4d I:%-4d E:%-4d %s\t(%s)\t%.03f seconds\n",
      passFail(summary), timeString, summary.getRight(), summary.getWrong(), summary.getIgnores(), summary.getExceptions(), page.getName(), page.getFullPath(), timeMeasurement.elapsedSeconds()));
  }

  private void updateCounters(TestSummary summary) {
    testCount++;
    if (summary.getWrong() > 0) {
      failCount++;
    }
    if (summary.getExceptions() > 0) {
      failCount++;
    }

  }
  private String passFail(TestSummary summary) {
    if (summary.getWrong() > 0){
      return "F";
    }
    if (summary.getExceptions() > 0) {
      return "X";
    }
    return ".";
  }

  @Override
  public void close() throws IOException {
    totalTimeMeasurement.stop();
    response.add(String.format("--------\n%d Tests,\t%d Failures\t%.03f seconds.\n", testCount, failCount, totalTimeMeasurement.elapsedSeconds()));
  }
}
