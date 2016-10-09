package fitnesse.testsystems.slim;

import fitnesse.slim.JavaSlimFactory;
import fitnesse.slim.SlimServer;
import fitnesse.slim.SlimService;
import fitnesse.slim.fixtureInteraction.FixtureInteraction;
import fitnesse.testsystems.ClientBuilder;
import fitnesse.testsystems.Descriptor;

import static fitnesse.testsystems.slim.SlimClientBuilder.SLIM_FLAGS;

/**
 * In-process version, mainly for testing FitNesse itself.
 */
public class InProcessSlimClientBuilder extends ClientBuilder<SlimClient> {

  public InProcessSlimClientBuilder(Descriptor descriptor) {
    super(descriptor);
  }

  @Override
  public SlimClient build() {
    final SlimService.Options options = SlimService.parseCommandLine(getSlimFlags());
    Integer statementTimeout = options != null ? options.statementTimeout : null;
    SlimServer slimServer = createSlimServer(statementTimeout, isDebug());
    return new InProcessSlimClient(getTestSystemName(), slimServer, getExecutionLogListener());
  }

  @Override
  protected String defaultTestRunner() {
    return "in-process";
  }

  protected SlimServer createSlimServer(Integer timeout, boolean verbose) {
    FixtureInteraction interaction = JavaSlimFactory.createInteraction(null);
    return JavaSlimFactory.createJavaSlimFactory(interaction, timeout, verbose).getSlimServer();
  }

  protected String[] getSlimFlags() {
    String slimFlags = getVariable("slim.flags");
    if (slimFlags == null) {
      slimFlags = getVariable(SLIM_FLAGS);
    }
    return slimFlags == null ? new String[] {} : parseCommandLine(slimFlags);
  }

}
