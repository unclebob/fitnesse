package fitnesse.testsystems.slim;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import fitnesse.slim.JavaSlimFactory;
import fitnesse.slim.SlimService;
import fitnesse.testsystems.CommandRunner;
import fitnesse.testsystems.Descriptor;
import fitnesse.testsystems.MockCommandRunner;

/**
 * In-process version, mainly for testing FitNesse itself.
 */
public class InProcessSlimClientBuilder extends SlimClientBuilder {
  private static final Logger LOG = Logger.getLogger(InProcessSlimClientBuilder.class.getName());

  public InProcessSlimClientBuilder(Descriptor descriptor) {
    super(descriptor);
  }

  @Override
  public SlimCommandRunningClient build() throws IOException {
    CommandRunner commandRunner = new MockCommandRunner(getExecutionLogListener());
    final String[] slimArguments = buildArguments();
    createSlimService(slimArguments);

    return new SlimCommandRunningClient(commandRunner, determineSlimHost(), getSlimPort(), determineTimeout(), getSlimVersion(), determineSSL(), determineHostSSLParameterClass());
  }

  @Override
  protected int getNextSlimPort() {
    return 0;
  }

  void createSlimService(String[] args) throws IOException {
    while (!tryCreateSlimService(args))
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        LOG.log(Level.WARNING, "Interrupted while waiting for Slim server to come on line", e);
      }
  }

  private boolean tryCreateSlimService(String[] args) throws IOException {
    try {
      SlimService.Options options = SlimService.parseCommandLine(args);
      int actualPort = SlimService.startWithFactoryAsync(JavaSlimFactory.createJavaSlimFactory(options), options);
      setSlimPort(actualPort);
      return true;
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Could not start async Slim service", e);
      return false;
    }
  }

}
