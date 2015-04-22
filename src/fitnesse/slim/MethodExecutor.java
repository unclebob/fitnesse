package fitnesse.slim;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import fitnesse.slim.fixtureInteraction.FixtureInteraction;

public abstract class MethodExecutor {
  public MethodExecutor() {
    super();
  }

  public abstract MethodExecutionResult execute(String instanceName, String methodName, Object[] args) throws Throwable;

  protected Method findMatchingMethod(String methodName, Class<?> k, int nArgs) {
    Method[] methods = k.getMethods();

    for (Method method : methods) {
      boolean hasMatchingName = method.getName().equals(methodName);
      boolean hasMatchingArguments = method.getParameterTypes().length == nArgs;
      if (hasMatchingName && hasMatchingArguments) {
        return method;
      }
    }
    return null;
  }

  protected MethodExecutionResult invokeMethod(Object instance, Method method, Object[] args) throws Throwable {
    Object[] convertedArgs = convertArgs(method, args);
    Object retval = callMethod(instance, method, convertedArgs);
    Class<?> retType = method.getReturnType();
    return new MethodExecutionResult(retval, retType);
  }

  protected Object[] convertArgs(Method method, Object[] args) {
    Type[] argumentParameterTypes = method.getGenericParameterTypes();
    return ConverterSupport.convertArgs(args, argumentParameterTypes);
  }

  protected Object callMethod(Object instance, Method method, Object[] convertedArgs) throws Throwable {
    FixtureInteraction interaction = SlimService.getInteraction();
    try {
      return interaction.methodInvoke(method, instance, convertedArgs);
    } catch (InvocationTargetException e) {
      if(e.getCause() != null){
        throw e.getCause();
      }else{
        throw e.getTargetException();
      }
    }
  }

  protected MethodExecutionResult findAndInvoke(String methodName, Object[] args, Object instance) throws Throwable {
    Method method = findMatchingMethod(methodName, instance.getClass(), args.length);
    if (method != null) {
      return this.invokeMethod(instance, method, args);
    }
    return MethodExecutionResult.noMethod(methodName, instance.getClass(), args.length);
  }
}