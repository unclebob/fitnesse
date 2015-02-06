package fitnesse.testsystems.fit;

import fitnesse.testsystems.Descriptor;

/**
 * In-process version of a Fit test system, mainly for testing FitNesse itself.
 */
public class InProcessFitClientBuilder extends FitClientBuilder {

  public InProcessFitClientBuilder(Descriptor descriptor) {
    super(descriptor);
  }

  @Override
  public CommandRunningFitClient build() {
    String testRunner = getTestRunner();

    CommandRunningFitClient.CommandRunningStrategy runningStrategy =
            new CommandRunningFitClient.InProcessCommandRunner(testRunner, getExecutionLogListener());

    return buildFitClient(runningStrategy);
  }

}
