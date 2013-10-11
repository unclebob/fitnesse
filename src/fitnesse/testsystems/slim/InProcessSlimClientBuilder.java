package fitnesse.testsystems.slim;

import java.io.IOException;

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
    final String slimArguments = buildArguments();
    createSlimService(slimArguments);

    return new SlimCommandRunningClient(commandRunner, determineSlimHost(), getSlimPort());
  }

}
