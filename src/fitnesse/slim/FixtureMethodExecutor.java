package fitnesse.slim;

import java.util.Map;

public class FixtureMethodExecutor extends MethodExecutor {

  private final Map<String, Object> instances;
  public FixtureMethodExecutor(Map<String, Object> instances) {
    this.instances = instances;
  }
  @Override
  public MethodExecutionResult execute(String instanceName, String methodName, Object[] args)
      throws Throwable {
    Object instance = instances.get(instanceName);
    if(instance == null) {
      return MethodExecutionResult.noInstance(instanceName);
    }
    return findAndInvoke(methodName, args, instance);
  }

}
