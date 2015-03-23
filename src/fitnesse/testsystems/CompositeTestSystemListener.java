package fitnesse.testsystems;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class CompositeTestSystemListener implements TestSystemListener {

  private final List<TestSystemListener> listeners = new LinkedList<TestSystemListener>();

  public final void addTestSystemListener(TestSystemListener listener) {
    listeners.add(listener);
  }

  protected final List<TestSystemListener> listeners() {
    return listeners;
  }

  @Override
  public void testSystemStarted(TestSystem testSystem) throws IOException {
    for (TestSystemListener listener : listeners)
      listener.testSystemStarted(testSystem);
  }

  @Override
  public void testOutputChunk(String output) throws IOException {
    for (TestSystemListener listener : listeners)
      listener.testOutputChunk(output);
  }

  @Override
  public void testStarted(TestPage testPage) throws IOException {
    for (TestSystemListener listener : listeners)
      listener.testStarted(testPage);
  }

  @Override
  public void testComplete(TestPage testPage, TestSummary testSummary) throws IOException {
    for (TestSystemListener listener : listeners)
      listener.testComplete(testPage, testSummary);
  }

  @Override
  public void testSystemStopped(TestSystem testSystem, Throwable cause) {
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
