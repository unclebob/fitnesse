package fitnesse.slim;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import fitnesse.slim.converters.ConverterRegistry;
import fitnesse.slim.converters.ElementConverterHelper;

public class MethodExecutionResult {
  public static final String MESSAGE_S_NO_METHOD_S_D_IN_CLASS_S_AVAILABLE_METHODS_S = "No Method %s[%d] in class %s.\n Available methods:\n%s";
  private static class NoMethod extends MethodExecutionResult {
    private final int numberOfArgs;
    private final String methodName;
    private final Class<?> clazz;
    private final String description;

    @Override
    public boolean hasResult() {
      return false;
    }

    public NoMethod(String methodName, Class<?> clazz, int numberOfArgs) {
      super(null, null);
      this.methodName = methodName;
      this.clazz = clazz;
      this.numberOfArgs = numberOfArgs;
      this.description = methodsToString(clazz.getMethods());
    }

    @Override
    public Object returnValue() {
      throw new SlimError(String.format(MESSAGE_S_NO_METHOD_S_D_IN_CLASS_S_AVAILABLE_METHODS_S, methodName, numberOfArgs,
          clazz.getName(), description), SlimServer.NO_METHOD_IN_CLASS, true);
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

  public static class InvalidParameters extends MethodExecutionResult {

	    private final String methodName;
	    private final Exception conversionException;

	    public InvalidParameters(String methodName, Exception conversonException) {
	      super(null, null);
	      this.methodName = methodName;
	      this.conversionException = conversonException;
	    }

	    @Override
	    public Object returnValue() {
	      throw new SlimError(SlimError.extractSlimMessage(conversionException.getMessage())+ "\nTried to invoke: " + methodName, conversionException, "", true);
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

  public static String methodsToString(Method[] methods) {
      String result = "";
      result = Arrays.stream(methods).map(m -> methodToString(m))
	      .sorted()
	      .collect(Collectors.joining("\n"));
      return result;
    }

  public static String methodToString(Method m) {
      return m.getName() + "(" + parametersToString(m.getParameterTypes()) + ") -> " + m.getReturnType().toString();
  }

  public static String parametersToString(Class<?>[] parameters) {
      String result;
      result =  Arrays.stream(parameters).map(Class::getTypeName).collect(Collectors.joining(", "));
      return result;
    }
  
}
