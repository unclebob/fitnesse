package fitnesse.testsystems.slim;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import fitnesse.slim.JavaSlimFactory;
import fitnesse.slim.SlimServer;
import fitnesse.slim.SlimService;
import fitnesse.testsystems.ClientBuilder;
import fitnesse.testsystems.CommandRunner;
import fitnesse.testsystems.Descriptor;
import fitnesse.testsystems.MockCommandRunner;

/**
 * In-process version, mainly for testing FitNesse itself.
 */
public class InProcessSlimClientBuilder extends ClientBuilder<SlimClient> {
  private static final Logger LOG = Logger.getLogger(InProcessSlimClientBuilder.class.getName());

  public InProcessSlimClientBuilder(Descriptor descriptor) {
    super(descriptor);
  }

  public SlimClient build() throws IOException {
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
