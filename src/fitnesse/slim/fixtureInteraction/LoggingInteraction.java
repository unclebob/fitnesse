package fitnesse.slim.fixtureInteraction;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LoggingInteraction extends DefaultInteraction {
  @Override
  public Object methodInvoke(Method method, Object instance, Object... convertedArgs) throws InvocationTargetException, IllegalAccessException {
    long startTime = System.nanoTime();
    Object o = super.methodInvoke(method, instance, convertedArgs);
    System.out.println("methodInvoke : " + method.getName() + " = " + (System.nanoTime() - startTime));
    return o;
  }

  @Override
  public Object newInstance(Constructor<?> constructor, Object... initargs) throws InvocationTargetException, InstantiationException, IllegalAccessException {
    long startTime = System.nanoTime();
    Object o = super.newInstance(constructor, initargs);
    System.out.println("newInstance : " + (System.nanoTime() - startTime));
    return o;
  }
}
