package fitnesse.slim;

import fitnesse.slim.fixtureInteraction.DefaultInteraction;
import fitnesse.slim.fixtureInteraction.FixtureInteraction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
  protected MethodExecutionResult invokeMethod(Object instance, Method method, Object[] args) throws Throwable {
    Object convertedArgs[] = convertArgs(method, args);
    Object retval = callMethod(instance, method, convertedArgs);
    Class<?> retType = method.getReturnType();
    return new MethodExecutionResult(retval, retType);
  }

  protected Object[] convertArgs(Method method, Object args[]) {
    Class<?>[] argumentTypes = method.getParameterTypes();
    return ConverterSupport.convertArgs(args, argumentTypes);
  }

  protected Object callMethod(Object instance, Method method, Object[] convertedArgs) throws Throwable {
    FixtureInteraction interaction = SlimService.getInteractionClass().newInstance();
    try {
      return interaction.methodInvoke(method, instance, convertedArgs);
    } catch (InvocationTargetException e) {
      throw e.getCause();
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