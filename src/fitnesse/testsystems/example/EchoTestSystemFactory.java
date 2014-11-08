package fitnesse.testsystems.example;

import java.io.IOException;

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
  public TestSystem create(Descriptor descriptor) throws IOException {
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
    public void start() throws IOException {
      // Nothing to do, except sending an event
      testSystemListener.testSystemStarted(this);
    }

    @Override
    public void bye() throws IOException, InterruptedException {

      // We're done
      testSystemListener.testSystemStarted(this);
    }

    @Override
    public void kill() throws IOException {

      // We're really done
      testSystemListener.testSystemStarted(this);
    }

    @Override
    public void runTests(TestPage pageToTest) throws IOException, InterruptedException {

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
