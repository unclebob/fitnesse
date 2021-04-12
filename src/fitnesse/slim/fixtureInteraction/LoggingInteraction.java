package fitnesse.slim.fixtureInteraction;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import fitnesse.slim.MethodExecutionResult;

public class LoggingInteraction extends SimpleInteraction {
  @Override
  public Object methodInvoke(Method method, Object instance, Object... convertedArgs) throws Throwable {
    long startTime = System.nanoTime();
    Exception anyException = null;
    Object o = null;
    try{
      o = super.methodInvoke(method, instance, convertedArgs);
    } catch (Exception e) {
      anyException = e;
    }
    String methodName = method.getDeclaringClass().getName() + "." + MethodExecutionResult.methodToString(method)
            + "." + ((instance == null) ? "" : " On instance of: " + instance.getClass().getName());
    System.out.println("methodInvoke : " + ((anyException != null) ? "EX" : "OK") + " : " + methodName + " = " + (System.nanoTime() - startTime));
    if (anyException == null) {
      return o;
    } else {
      throw anyException;
    }
  }

  @Override
  public Object newInstance(Constructor<?> constructor, Object... initargs) throws InvocationTargetException, InstantiationException, IllegalAccessException {
    long startTime = System.nanoTime();
    Object o = super.newInstance(constructor, initargs);
    System.out.println("newInstance : " + (System.nanoTime() - startTime));
    return o;
  }
}
