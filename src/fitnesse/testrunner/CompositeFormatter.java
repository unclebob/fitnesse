package fitnesse.testrunner;

import java.io.Closeable;
import java.io.IOException;

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
  public void unableToStartTestSystem(String testSystemName, Throwable cause) throws IOException {
    for (TestSystemListener listener : listeners())
      if (listener instanceof TestsRunnerListener)
        ((TestsRunnerListener) listener).unableToStartTestSystem(testSystemName, cause);
  }

  @Override
  public void close() throws IOException {
    for (TestSystemListener listener : listeners())
      if (listener instanceof Closeable)
        ((Closeable) listener).close();
  }
}
