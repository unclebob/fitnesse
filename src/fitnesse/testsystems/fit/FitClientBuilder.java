package fitnesse.testsystems.fit;

import java.util.Map;

import fitnesse.testsystems.ClassPath;
import fitnesse.testsystems.ClientBuilder;
import fitnesse.testsystems.Descriptor;

public class FitClientBuilder extends ClientBuilder<CommandRunningFitClient> {

  public FitClientBuilder(Descriptor descriptor) {
    super(descriptor);
  }

  @Override
  public CommandRunningFitClient build() {
    String testRunner = getTestRunner();
    ClassPath classPath = getClassPath();
    String[] command = buildCommand(getCommandPattern(), testRunner, classPath);
    Map<String, String> environmentVariables = createClasspathEnvironment(classPath);
    CommandRunningFitClient.CommandRunningStrategy runningStrategy =
            new CommandRunningFitClient.OutOfProcessCommandRunner(command, environmentVariables, getExecutionLogListener());

    return buildFitClient(runningStrategy);
  }

  @Override
  protected String defaultTestRunner() {
    return "fit.FitServer";
  }

  protected CommandRunningFitClient buildFitClient(CommandRunningFitClient.CommandRunningStrategy runningStrategy) {
    return new CommandRunningFitClient(runningStrategy);
  }

}
