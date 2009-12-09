package fitnesse.slim;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public abstract class MethodExecutor {

  public MethodExecutor() {
    super();
  }
  
  public abstract MethodExecutionResult execute(String instanceName, String methodName, Object[] args) throws Throwable;

  protected Method findMatchingMethod(String methodName, Class<? extends Object> k, int nArgs) {
    Method methods[] = k.getMethods();
  
    for (Method method : methods) {
      boolean hasMatchingName = method.getName().equals(methodName);
      boolean hasMatchingArguments = method.getParameterTypes().length == nArgs;
      if (hasMatchingName && hasMatchingArguments) {
        return method;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  protected Object invokeMethod(Object instance, Method method, Object[] args) throws Throwable {
    Object convertedArgs[] = convertArgs(method, args);
    Object retval = callMethod(instance, method, convertedArgs);
    Class<?> retType = method.getReturnType();
    if (retType == List.class && retval instanceof List)
      return retval;
    return convertToString(retval, retType);
  }

  private Object[] convertArgs(Method method, Object args[]) {
    Class<?>[] argumentTypes = method.getParameterTypes();
    Object[] convertedArgs = convertArgs(args, argumentTypes);
    return convertedArgs;
  }

  private Object callMethod(Object instance, Method method, Object[] convertedArgs) throws Throwable {
    Object retval = null;
    try {
      retval = method.invoke(instance, convertedArgs);
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
    return retval;
  }

  @SuppressWarnings("unchecked")
  private Object[] convertArgs(Object[] args, Class<?>[] argumentTypes) {
    Object[] convertedArgs = new Object[args.length];
    for (int i = 0; i < argumentTypes.length; i++) {
      Class<?> argumentType = argumentTypes[i];
      if (argumentType == List.class && args[i] instanceof List) {
        convertedArgs[i] = args[i];
      } else {
        
        Converter converter = ConverterSupport.getConverter(argumentType);
        if (converter != null)
          convertedArgs[i] = converter.fromString((String) args[i]);
        else
          throw new SlimError(String.format("message:<<NO_CONVERTER_FOR_ARGUMENT_NUMBER %s.>>",
              argumentType.getName()));
      }
    }
    return convertedArgs;
  }

  private Object convertToString(Object retval, Class<?> retType) {
    Converter converter = ConverterSupport.getConverter(retType);
    if (converter != null)
      return converter.toString(retval);
    if (retval == null)
      return "null";
    else
      return retval.toString();
  }

  protected MethodExecutionResult findAndInvoke(String methodName, Object[] args, Object instance) throws Throwable {
    Method method = findMatchingMethod(methodName, instance.getClass(), args.length);
    if(method != null) {
      return new MethodExecutionResult(this.invokeMethod(instance, method, args));
    }
    return MethodExecutionResult.noMethod(methodName, instance.getClass(), args.length);
  }

}