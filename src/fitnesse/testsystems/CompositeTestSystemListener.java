package fitnesse.testsystems;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;

/**
 * Send commands to a set of listeners.
 * Misbehaving listeners (the ones that throw IOException's) are removed from the list of listeners.
 */
public class CompositeTestSystemListener implements TestSystemListener {

  private final List<TestSystemListener> listeners = new LinkedList<TestSystemListener>();

  public final void addTestSystemListener(TestSystemListener listener) {
    listeners.add(listener);
  }

  protected final List<TestSystemListener> listeners() {
    return listeners;
  }

  @Override
  public void testSystemStarted(final TestSystem testSystem) throws IOException {
    invokeListeners(new Handler() {
      @Override public void invoke(TestSystemListener listener) throws IOException {
        listener.testSystemStarted(testSystem);
      }
    });
  }

  @Override
  public void testOutputChunk(final String output) throws IOException {
    invokeListeners(new Handler() {
      @Override public void invoke(TestSystemListener listener) throws IOException {
        listener.testOutputChunk(output);
      }
    });
  }

  @Override
  public void testStarted(final TestPage testPage) throws IOException {
    invokeListeners(new Handler() {
      @Override public void invoke(TestSystemListener listener) throws IOException {
        listener.testStarted(testPage);
      }
    });
  }

  @Override
  public void testComplete(final TestPage testPage, final TestSummary testSummary) throws IOException {
    invokeListeners(new Handler() {
      @Override public void invoke(TestSystemListener listener) throws IOException {
        listener.testComplete(testPage, testSummary);
      }
    });
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

  protected void invokeListeners(Handler handler) throws IOException {
    List<IOException> caughtExceptions = new ArrayList<>();
    for (Iterator<TestSystemListener> iter = listeners.iterator(); iter.hasNext(); ) {
      TestSystemListener listener = iter.next();
      try {
        handler.invoke(listener);
      } catch (IOException e) {
        caughtExceptions.add(e);
        iter.remove();
      }
    }

    if (caughtExceptions.size() == 1) {
      throw caughtExceptions.get(0);
    } else if (!caughtExceptions.isEmpty()) {
      throw new CompositeIOException(format("%s test system listeners threw exceptions", caughtExceptions.size()), caughtExceptions);
    }
  }

  protected interface Handler {
    void invoke(TestSystemListener listener) throws IOException;
  }

  public static class CompositeIOException extends IOException {

    private final List<IOException> causes;

    public CompositeIOException(String message, List<IOException> causes) {
      super(message, causes.get(0));
      this.causes = causes;
    }

    public List<IOException> getCauses() {
      return causes;
    }
  }
}
