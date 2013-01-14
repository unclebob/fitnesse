package fitnesse.slim;

public class FixtureMethodExecutor extends MethodExecutor {

  private final SlimExecutionContext context;

  public FixtureMethodExecutor(SlimExecutionContext context) {
    this.context = context;
  }

  @Override
  public MethodExecutionResult execute(String instanceName, String methodName, Object[] args)
      throws Throwable {
    Object instance;
    try {
      instance = context.getInstance(instanceName);
    } catch (SlimError e) {
      return MethodExecutionResult.noInstance(instanceName + "." + methodName);
    }
    return findAndInvoke(methodName, args, instance);
  }
}
