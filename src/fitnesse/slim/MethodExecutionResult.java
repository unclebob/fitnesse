package fitnesse.slim;

public class MethodExecutionResult {
  private static class NoMethod extends MethodExecutionResult {
    static final String MESSAGE_NO_METHOD_IN_CLASS = "message:<<NO_METHOD_IN_CLASS %s[%d] %s.>>";

    private final int numberOfArgs;
    private final String methodName;
    private final Class<?> clazz;

    public NoMethod(String methodName, Class<?> clazz, int numberOfArgs) {
      super(null);
      this.methodName = methodName;
      this.clazz = clazz;
      this.numberOfArgs = numberOfArgs;
    }

    @Override
    public Object returnValue() {
      throw new SlimError(String.format(MESSAGE_NO_METHOD_IN_CLASS, methodName, numberOfArgs, clazz
          .getName()));
    }

  }

  private static class NoInstance extends MethodExecutionResult {

    private final String instanceName;

    NoInstance(String instanceName) {
      super(null);
      this.instanceName = instanceName;
    }

    public Object returnValue() {
      throw new SlimError(String.format("message:<<NO_INSTANCE %s.>>", instanceName));
    };

  }

  public static final MethodExecutionResult NO_METHOD_IN_LIBRARIES = new MethodExecutionResult(null);

  private final Object result;

  MethodExecutionResult(Object result) {
    this.result = result;
  }

  public static MethodExecutionResult noMethod(String methodName, Class<?> clazz, int numberOfArgs) {
    return new NoMethod(methodName, clazz, numberOfArgs);
  }

  public static MethodExecutionResult noInstance(String instanceName) {
    return new NoInstance(instanceName);
  }

  public boolean hasResult() {
    return this.result != null;
  }

  public Object returnValue() {
    return result;
  }

  public boolean hasMethod() {
    return !(this instanceof NoMethod);
  }

}
