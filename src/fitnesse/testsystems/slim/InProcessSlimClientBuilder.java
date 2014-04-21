package fitnesse.testsystems.slim;

import java.io.IOException;

import fitnesse.slim.JavaSlimFactory;
import fitnesse.slim.SlimService;
import fitnesse.testsystems.CommandRunner;
import fitnesse.testsystems.Descriptor;
import fitnesse.testsystems.MockCommandRunner;

/**
 * In-process version, mainly for testing FitNesse itself.
 */
public class InProcessSlimClientBuilder extends SlimClientBuilder {

  public InProcessSlimClientBuilder(Descriptor descriptor) {
    super(descriptor);
  }

  @Override
  public SlimCommandRunningClient build() throws IOException {
    CommandRunner commandRunner = new MockCommandRunner();
    final String[] slimArguments = buildArguments();
    createSlimService(slimArguments);

    return new SlimCommandRunningClient(commandRunner, determineSlimHost(), getSlimPort());
  }

  void createSlimService(String[] args) throws IOException {
    while (!tryCreateSlimService(args))
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
  }

  private boolean tryCreateSlimService(String[] args) throws IOException {
    try {
      SlimService.Options options = SlimService.parseCommandLine(args);
      SlimService.startWithFactoryAsync(JavaSlimFactory.createJavaSlimFactory(options), options);
      return true;
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

}
