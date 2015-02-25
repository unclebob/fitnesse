package fitnesse.slim.fixtureInteraction;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fitnesse.slim.ConverterSupport;
import fitnesse.slim.SlimError;
import fitnesse.slim.SlimServer;

public class DefaultInteraction implements FixtureInteraction {

  @Override
  public Object createInstance(List<String> paths, String className, Object[] args)
          throws IllegalArgumentException, InstantiationException,
          IllegalAccessException, InvocationTargetException {
    Class<?> k = searchPathsForClass(paths, className);
    Constructor<?> constructor = getConstructor(k.getConstructors(), args);
    if (constructor == null) {
      throw new SlimError(String.format("message:<<%s %s>>",
              SlimServer.NO_CONSTRUCTOR, className));
    }

    return newInstance(args, constructor);
  }

  private Object newInstance(Object[] args, Constructor<?> constructor)
          throws IllegalAccessException, InstantiationException,
          InvocationTargetException {
    Object[] initargs = ConverterSupport.convertArgs(args,
            constructor.getParameterTypes());

    return newInstance(constructor, initargs);
  }

  protected Class<?> searchPathsForClass(List<String> paths, String className) {
    Class<?> k = getClass(className);
    if (k != null) {
      return k;
    }
    List<String> reversedPaths = new ArrayList<String>(paths);
    Collections.reverse(reversedPaths);
    for (String path : reversedPaths) {
      k = getClass(path + "." + className);
      if (k != null) {
        return k;
      }
    }
    throw new SlimError(String.format("message:<<%s %s>>", SlimServer.NO_CLASS, className));
  }

  private Class<?> getClass(String className) {
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  private Constructor<?> getConstructor(Constructor<?>[] constructors,
                                        Object[] args) {
    for (Constructor<?> constructor : constructors) {
      Class<?> arguments[] = constructor.getParameterTypes();
      if (arguments.length == args.length) {
        return constructor;
      }
    }
    return null;
  }

  protected Object newInstance(Constructor<?> constructor, Object... initargs) throws InvocationTargetException, InstantiationException, IllegalAccessException {
    return constructor.newInstance(initargs);
  }

  @Override
  public Object methodInvoke(Method method, Object instance, Object... convertedArgs) throws InvocationTargetException, IllegalAccessException {
      return method.invoke(instance, convertedArgs);
  }
}
