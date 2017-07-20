package fitnesse.slim.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import fitnesse.slim.fixtureInteraction.FixtureInteraction;
import fitnesse.slim.fixtureInteraction.InteractionAwareFixture;

public class TestSlimInvocationAware extends TestSlim implements InteractionAwareFixture {
  private static final List<String> METHODS = new ArrayList<>();

  @Override
  public Object aroundSlimInvoke(FixtureInteraction fixtureInteraction, Method method, Object... arguments) throws Throwable {
    METHODS.add(method.getName());
    return fixtureInteraction.methodInvoke(method, this, arguments);
  }

  public static void resetMethodsCalled() {
    METHODS.clear();
  }

  public static List<String> getMethodsCalled() {
    return METHODS;
  }
}
