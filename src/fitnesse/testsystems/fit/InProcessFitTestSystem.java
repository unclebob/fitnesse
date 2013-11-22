package fitnesse.testsystems.fit;

import fitnesse.FitNesseContext;
import fitnesse.testsystems.Descriptor;
import fitnesse.testsystems.TestSystemListener;

/**
 * In-process version of a Fit test system, mainly for testing FitNesse itself.
 */
public class InProcessFitTestSystem extends FitTestSystem {

  public InProcessFitTestSystem(Descriptor descriptor, int port) {
    super(descriptor, port);
  }

  @Override
  public FitClient build() {
    String testRunner = descriptor.getTestRunner();
    CommandRunningFitClient.CommandRunningStrategy runningStrategy =
            new CommandRunningFitClient.InProcessCommandRunner(testRunner);

    return buildFitClient(runningStrategy);
  }


}
