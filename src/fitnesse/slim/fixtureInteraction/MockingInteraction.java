package fitnesse.slim.fixtureInteraction;

import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MockingInteraction implements FixtureInteraction {
  @Override
  public Object newInstance(Constructor<?> constructor, Object... initargs) throws InvocationTargetException, InstantiationException, IllegalAccessException {
    return Mockito.mock(constructor.getDeclaringClass());
  }

  @Override
  public Object methodInvoke(Method method, Object instance, Object... convertedArgs) throws InvocationTargetException, IllegalAccessException {
    return "----mockingOnly----";
  }
}
