package fitnesse.slim;

import java.util.List;

import fitnesse.slim.converters.ConverterRegistry;
import fitnesse.slim.converters.ElementConverterHelper;

public class MethodExecutionResult {
  private static class NoMethod extends MethodExecutionResult {
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
      throw new SlimError(String.format("message:<<%s %s[%d] %s.>>", SlimServer.NO_METHOD_IN_CLASS, methodName, numberOfArgs,
          clazz.getName()));
    }

  }

  private static class NoInstance extends MethodExecutionResult {

    private final String instanceName;

    NoInstance(String instanceName) {
      super(null, null);
      this.instanceName = instanceName;
    }

    @Override
    public Object returnValue() {
      throw new SlimError(String.format("message:<<%s %s.>>", SlimServer.NO_INSTANCE, instanceName));
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

  public MethodExecutionResult(Object value, Class<?> type) {
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
  
  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public String toString() {
    Converter converter = ConverterRegistry.getConverterForClass(type);
    if (converter != null)
      return converter.toString(value);
    if (value == null)
      return "null";
    else
      return ElementConverterHelper.elementToString(value);
  }

  public boolean hasMethod() {
    return !(this instanceof NoMethod);
  }

  public Object getObject() {
    return value;
  }

}
