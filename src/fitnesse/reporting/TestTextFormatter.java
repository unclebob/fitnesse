package fitnesse.reporting;

import java.io.Closeable;
import java.io.IOException;
import java.text.SimpleDateFormat;

import fitnesse.testsystems.TestPage;
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
    writeData("\nStarting Test System: %s.\n", testSystem.getName());
  }

  @Override
  public void testStarted(TestPage page) {
    timeMeasurement = new TimeMeasurement().start();
  }

  @Override
  public void testOutputChunk(String output) {
  }

  @Override
  public void testComplete(TestPage page, TestSummary summary) {
    timeMeasurement.stop();
    updateCounters(summary);
    String timeString = new SimpleDateFormat("HH:mm:ss").format(timeMeasurement.startedAtDate());
    writeData("%s %s R:%-4d W:%-4d I:%-4d E:%-4d %s\t(%s)\t%.03f seconds\n",
      passFail(summary), timeString, summary.getRight(), summary.getWrong(), summary.getIgnores(), summary.getExceptions(), page.getName(), page.getFullPath(), timeMeasurement.elapsedSeconds());
  }

  private void writeData(String format, Object... args) {
    try {
      response.add(String.format(format, args));
    } catch (IOException e) {
      throw new FormatterException("Unable to write data, abort", e);
    }
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
