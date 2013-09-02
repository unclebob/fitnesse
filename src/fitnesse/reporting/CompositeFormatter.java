package fitnesse.reporting;

import java.io.Closeable;
import java.io.IOException;

import fitnesse.testrunner.CompositeExecutionLog;
import fitnesse.testrunner.TestsRunnerListener;
import fitnesse.testsystems.CompositeTestSystemListener;
import fitnesse.testsystems.TestSystemListener;

public class CompositeFormatter extends CompositeTestSystemListener implements TestsRunnerListener, Closeable {

  @Override
  public void announceNumberTestsToRun(int testsToRun) {
    for (TestSystemListener listener : listeners())
      if (listener instanceof TestsRunnerListener)
        ((TestsRunnerListener) listener).announceNumberTestsToRun(testsToRun);
  }

  @Override
  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) {
    for (TestSystemListener listener : listeners())
      if (listener instanceof TestsRunnerListener)
        ((TestsRunnerListener) listener).setExecutionLogAndTrackingId(stopResponderId, log);
  }

  @Override
  public void close() throws IOException {
    for (TestSystemListener listener : listeners())
      if (listener instanceof Closeable)
        ((Closeable) listener).close();
  }
}
