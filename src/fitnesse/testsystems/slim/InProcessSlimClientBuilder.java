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

  private ClassLoader classLoader;

  public InProcessSlimClientBuilder(Descriptor descriptor, ClassLoader classLoader) {
    super(descriptor);
    this.classLoader = classLoader;
  }

  @Override
  public SlimClient build() {
    final SlimService.Options options = SlimService.parseCommandLine(getSlimFlags());
    Integer statementTimeout = options != null ? options.statementTimeout : null;
    FixtureInteraction interaction = options != null ? options.interaction : JavaSlimFactory.createInteraction(null, classLoader);

    SlimServer slimServer = createSlimServer(interaction, statementTimeout, isDebug());
    return new InProcessSlimClient(getTestSystemName(), slimServer, getExecutionLogListener(), classLoader);
  }

  @Override
  protected String defaultTestRunner() {
    return "in-process";
  }

  protected SlimServer createSlimServer(FixtureInteraction interaction, Integer timeout, boolean verbose) {
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
