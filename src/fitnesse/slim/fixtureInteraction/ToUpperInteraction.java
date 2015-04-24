package fitnesse.slim.fixtureInteraction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ToUpperInteraction extends DefaultInteraction {
  @Override
  public Object methodInvoke(Method method, Object instance, Object... convertedArgs) throws InvocationTargetException, IllegalAccessException {
    Object result = super.methodInvoke(method, instance, convertedArgs);
    if (result instanceof String) {
      result = ((String) result).toUpperCase();
    }
    return result;
  }
}
