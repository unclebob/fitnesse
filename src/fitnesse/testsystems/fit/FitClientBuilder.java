package fitnesse.testsystems.fit;

import java.util.Map;

import fitnesse.testsystems.ClientBuilder;
import fitnesse.testsystems.Descriptor;

public class FitClientBuilder extends ClientBuilder<CommandRunningFitClient> {

  public FitClientBuilder(Descriptor descriptor) {
    super(descriptor);
  }

  @Override
  public CommandRunningFitClient build() {
    String testRunner = descriptor.getTestRunner();
    String classPath = descriptor.getClassPath();
    String command = buildCommand(descriptor.getCommandPattern(), testRunner, classPath);
    Map<String, String> environmentVariables = descriptor.createClasspathEnvironment(classPath);
    CommandRunningFitClient.CommandRunningStrategy runningStrategy =
            new CommandRunningFitClient.OutOfProcessCommandRunner(command, environmentVariables);

    return buildFitClient(runningStrategy);
  }

  protected CommandRunningFitClient buildFitClient(CommandRunningFitClient.CommandRunningStrategy runningStrategy) {
    return new CommandRunningFitClient(runningStrategy);
  }

}
