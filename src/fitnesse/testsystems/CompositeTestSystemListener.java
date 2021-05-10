package fitnesse.testsystems;

import java.util.LinkedList;
import java.util.List;

/**
 * Send commands to a set of listeners.
 * Misbehaving listeners (the ones that throw IOException's) are removed from the list of listeners.
 */
public class CompositeTestSystemListener implements TestSystemListener {

  private final List<TestSystemListener> listeners = new LinkedList<>();

  public final void addTestSystemListener(TestSystemListener listener) {
    listeners.add(listener);
  }

  protected final List<TestSystemListener> listeners() {
    return listeners;
  }

  @Override
  public void testSystemStarted(final TestSystem testSystem) {
    for (TestSystemListener listener : listeners)
      listener.testSystemStarted(testSystem);
  }

  @Override
  public void testOutputChunk(final TestPage testPage, final String output) {
    for (TestSystemListener listener : listeners)
      listener.testOutputChunk(testPage, output);
  }

  @Override
  public void testStarted(final TestPage testPage) {
    for (TestSystemListener listener : listeners)
      listener.testStarted(testPage);
  }

  @Override
  public void testComplete(final TestPage testPage, final TestSummary testSummary) {
    for (TestSystemListener listener : listeners)
      listener.testComplete(testPage, testSummary);
  }

  @Override
  public void testSystemStopped(final TestSystem testSystem, final Throwable cause) {
    for (TestSystemListener listener : listeners)
      listener.testSystemStopped(testSystem, cause);
  }

  @Override
  public void testAssertionVerified(Assertion assertion, TestResult testResult) {
    for (TestSystemListener listener : listeners)
      listener.testAssertionVerified(assertion, testResult);
  }

  @Override
  public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {
    for (TestSystemListener listener : listeners)
      listener.testExceptionOccurred(assertion, exceptionResult);
  }
}
