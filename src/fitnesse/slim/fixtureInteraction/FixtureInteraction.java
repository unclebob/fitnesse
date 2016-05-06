// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.fixtureInteraction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import fitnesse.slim.MethodExecutionResult;

public interface FixtureInteraction {
  Object createInstance(List<String> paths, String className, Object[] args)
          throws IllegalArgumentException, InstantiationException,
          IllegalAccessException, InvocationTargetException;

  MethodExecutionResult methodInvoke(String method, Object instance, Object... args) throws Throwable;
}
