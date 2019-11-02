package fitnesse.testsystems.fit;

import fitnesse.testsystems.Descriptor;

import java.lang.reflect.Method;

/**
 * In-process version of a Fit test system, mainly for testing FitNesse itself.
 */
public class InProcessFitClientBuilder extends FitClientBuilder {

  private final ClassLoader classLoader;

  public InProcessFitClientBuilder(Descriptor descriptor, ClassLoader classLoader) {
    super(descriptor);
    this.classLoader = classLoader;
  }

  @Override
  public CommandRunningFitClient build() {
    Method testRunnerMethod = getTestRunnerMainMethod();

    CommandRunningFitClient.CommandRunningStrategy runningStrategy =
            new CommandRunningFitClient.InProcessCommandRunner(testRunnerMethod, getExecutionLogListener(), classLoader);

    return buildFitClient(runningStrategy);
  }

  protected Method getTestRunnerMainMethod() {
    try {
      Class<?> testRunnerClass = classLoader.loadClass(getTestRunner());
      return testRunnerClass.getDeclaredMethod("main", String[].class);
    } catch (ClassNotFoundException|NoSuchMethodException e) {
      throw new RuntimeException("Can not find test runner main", e);
    }
  }


}
