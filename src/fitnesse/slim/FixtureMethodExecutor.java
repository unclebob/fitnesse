package fitnesse.slim;

public class FixtureMethodExecutor extends MethodExecutor {

  public FixtureMethodExecutor(SlimExecutionContext context) {
    super(context);
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
