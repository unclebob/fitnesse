package fitnesse.slim;

import java.util.List;

public class MethodExecutionResult {
  private static class NoMethod extends MethodExecutionResult {
    static final String MESSAGE_NO_METHOD_IN_CLASS = "message:<<NO_METHOD_IN_CLASS %s[%d] %s.>>";

    private final int numberOfArgs;
    private final String methodName;
    private final Class<?> clazz;

    @Override
    public boolean hasResult() {
      return false;
    }

    public NoMethod(String methodName, Class<?> clazz, int numberOfArgs) {
      super(null, null);
      this.methodName = methodName;
      this.clazz = clazz;
      this.numberOfArgs = numberOfArgs;
    }

    @Override
    public Object returnValue() {
      throw new SlimError(String.format(MESSAGE_NO_METHOD_IN_CLASS, methodName, numberOfArgs,
          clazz.getName()));
    }

  }

  private static class NoInstance extends MethodExecutionResult {

    private final String instanceName;

    NoInstance(String instanceName) {
      super(null, null);
      this.instanceName = instanceName;
    }

    public Object returnValue() {
      throw new SlimError(String.format("message:<<NO_INSTANCE %s.>>", instanceName));
    }

    @Override
    public boolean hasResult() {
      return false;
    }
  }

  public static final MethodExecutionResult NO_METHOD_IN_LIBRARIES = new MethodExecutionResult(null, null) {
    @Override
    public boolean hasResult() {
      return false;
    }
  };

  private final Object value;
  private final Class<?> type;

  MethodExecutionResult(Object value, Class<?> type) {
    this.value = value;
    this.type = type;
  }

  public static MethodExecutionResult noMethod(String methodName, Class<?> clazz, int numberOfArgs) {
    return new NoMethod(methodName, clazz, numberOfArgs);
  }

  public static MethodExecutionResult noInstance(String instanceName) {
    return new NoInstance(instanceName);
  }

  public boolean hasResult() {
    return true;
  }

  public Object returnValue() {
    if (type == List.class && value instanceof List) {
      return value;
    } else {
      return toString();
    }
  }
  
  public String toString() {
    Converter converter = ConverterSupport.getConverter(type);
    if (converter != null)
      return converter.toString(value);
    if (value == null)
      return "null";
    else
      return value.toString();
  }

  public boolean hasMethod() {
    return !(this instanceof NoMethod);
  }

  public Object getObject() {
    return value;
  }

}
