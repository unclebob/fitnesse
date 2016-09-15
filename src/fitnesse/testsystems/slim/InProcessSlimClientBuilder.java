package fitnesse.testsystems.slim;

import fitnesse.slim.JavaSlimFactory;
import fitnesse.slim.SlimServer;
import fitnesse.slim.fixtureInteraction.FixtureInteraction;
import fitnesse.testsystems.ClientBuilder;
import fitnesse.testsystems.Descriptor;

/**
 * In-process version, mainly for testing FitNesse itself.
 */
public class InProcessSlimClientBuilder extends ClientBuilder<SlimClient> {

  public InProcessSlimClientBuilder(Descriptor descriptor) {
    super(descriptor);
  }

  @Override
  public SlimClient build() {
    SlimServer slimServer = createSlimServer(1000, isDebug());
    return new InProcessSlimClient(getTestSystemName(), slimServer, getExecutionLogListener());
  }

  @Override
  protected String defaultTestRunner() {
    return "in-process";
  }

  protected SlimServer createSlimServer(int timeout, boolean verbose) {
    FixtureInteraction interaction = JavaSlimFactory.createInteraction(null);
    return JavaSlimFactory.createJavaSlimFactory(interaction, timeout, verbose).getSlimServer();
  }

}
