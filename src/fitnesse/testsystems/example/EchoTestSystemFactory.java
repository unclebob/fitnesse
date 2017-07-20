package fitnesse.testsystems.example;

import fitnesse.testsystems.CompositeTestSystemListener;
import fitnesse.testsystems.Descriptor;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemFactory;
import fitnesse.testsystems.TestSystemListener;

/**
 * A simple example implementation. It outputs the page's wiki text as
 * verbatim output.
 */
public class EchoTestSystemFactory implements TestSystemFactory {
  @Override
  public TestSystem create(Descriptor descriptor) {
    return new EchoTestSystem();
  }

  public static class EchoTestSystem implements TestSystem {
    private final CompositeTestSystemListener testSystemListener;

    public EchoTestSystem() {
      testSystemListener = new CompositeTestSystemListener();
    }

    @Override
    public String getName() {
      return "Example";
    }

    @Override
    public void start() {
      // Nothing to do, except sending an event
      testSystemListener.testSystemStarted(this);
    }

    @Override
    public void bye() {

      // We're done
      testSystemListener.testSystemStopped(this, null);
    }

    @Override
    public void kill() {
      // We're really done
       testSystemListener.testSystemStopped(this, null);
    }

    @Override
    public void runTests(TestPage pageToTest) {
      testSystemListener.testStarted(pageToTest);
      testSystemListener.testOutputChunk("<pre>" + pageToTest.getHtml() + "</pre>");
      testSystemListener.testComplete(pageToTest, new TestSummary(1, 0, 0, 0));
    }

    @Override
    public boolean isSuccessfullyStarted() {
      // Can be used to check on asynchronously started processes.
      return true;
    }

    @Override
    public void addTestSystemListener(TestSystemListener listener) {
      testSystemListener.addTestSystemListener(listener);
    }
  }
}
