package fitnesse.testsystems.slim;

import java.io.IOException;

import fitnesse.slim.JavaSlimFactory;
import fitnesse.slim.SlimServer;
import fitnesse.testsystems.ClientBuilder;
import fitnesse.testsystems.Descriptor;

/**
 * In-process version, mainly for testing FitNesse itself.
 */
public class InProcessSlimClientBuilder extends ClientBuilder<SlimClient> {

  public InProcessSlimClientBuilder(Descriptor descriptor) {
    super(descriptor);
  }

  public SlimClient build() {
    SlimServer slimServer = createSlimServer(1000, isDebug());
    return new InProcessSlimClient(getTestSystemName(), slimServer, getExecutionLogListener());
  }

  @Override
  protected String defaultTestRunner() {
    return "in-process";
  }

  protected SlimServer createSlimServer(int timeout, boolean verbose) {
    return JavaSlimFactory.createJavaSlimFactory(timeout, verbose).getSlimServer();
  }

}
