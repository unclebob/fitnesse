package fitnesse.testrunner;

import java.io.Closeable;

import fitnesse.testsystems.CompositeTestSystemListener;
import fitnesse.testsystems.TestSystemListener;
import util.FileUtil;

public class CompositeFormatter extends CompositeTestSystemListener implements TestsRunnerListener, Closeable {

  @Override
  public void announceNumberTestsToRun(int testsToRun) {
    for (TestSystemListener listener : listeners())
      if (listener instanceof TestsRunnerListener)
        ((TestsRunnerListener) listener).announceNumberTestsToRun(testsToRun);
  }

  @Override
  public void unableToStartTestSystem(final String testSystemName, final Throwable cause) {
    for (TestSystemListener listener : listeners())
      if (listener instanceof TestsRunnerListener)
        ((TestsRunnerListener) listener).unableToStartTestSystem(testSystemName, cause);
  }

  @Override
  public void close() {
    for (TestSystemListener listener : listeners())
        if (listener instanceof Closeable)
          FileUtil.close((Closeable) listener);
  }
}
