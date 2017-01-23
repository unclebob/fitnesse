package fitnesse.slim.fixtureInteraction;

import fitnesse.slim.ConverterSupport;
import fitnesse.slim.MethodExecutionResult;
import fitnesse.slim.SlimError;
import fitnesse.slim.SlimServer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

public class DefaultInteraction implements FixtureInteraction {

  private static final Method AROUND_METHOD;

  static {
    try {
      AROUND_METHOD = InteractionAwareFixture.class.getMethod("aroundSlimInvoke", FixtureInteraction.class, Method.class, Object[].class);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Object createInstance(List<String> paths, String className, Object[] args)
          throws IllegalArgumentException, InstantiationException,
          IllegalAccessException, InvocationTargetException {
    Class<?> k = searchPathsForClass(paths, className);
    Constructor<?> constructor = getConstructor(k, args);
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
    for (String path : paths) {
      k = getClass(path + "." + className);
      if (k != null) {
        return k;
      }
    }
    throw new SlimError(String.format("message:<<%s %s>>", SlimServer.NO_CLASS, className));
  }

  protected Class<?> getClass(String className) {
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  /**
   *
   * @param clazz the class to be executed
   * @param args the class constructor arguments values
   * @return the class constructor with the arguments types equals with
   * {@code  args}, otherwise return the first constructor which matches as
   * arguments number
   */
  protected Constructor<?> getConstructor(Class<?> clazz,
          Object... args) {
    /*in case no constructor has the args types, then return the first constructor which
    * matches as args number, and not as args types
     */
    Constructor<?> defaultConstructor = null;
    Constructor<?> constructorSelectedByType = null;
    int diffCounter = 0;
    
    for (Constructor<?> constructor : clazz.getConstructors()) {
      int currentDiffCounter;
      Class<?>[] constructorArgs = constructor.getParameterTypes();
      if (constructorArgs.length == args.length) {
        if (defaultConstructor == null) {
          defaultConstructor = constructor;
        }
        
        final Object[] convertedArgs = getConvertedConstructorArgsTypes(constructor, args);
        final boolean matchedConstructorArgsTypes = hasConstructorArgsTypes(constructorArgs, convertedArgs);
        if (matchedConstructorArgsTypes) {
          //the constructor with most convertion changes should be more eligible for picking-up
          currentDiffCounter = getConstructorArgsConvertionDiff(args, convertedArgs);
          //pick-up the constructor with the most explicitly args typess
          if (currentDiffCounter >= diffCounter) {
            diffCounter = currentDiffCounter;
            constructorSelectedByType = constructor;
          }
        }
      }
    }

    return constructorSelectedByType == null ? defaultConstructor : constructorSelectedByType;
  }

  private boolean hasConstructorArgsTypes(final Class<?>[] constructorArgs, final Object[] convertedArgs) {
    boolean constructorMatched = true;
    for (int i = 0; i < constructorArgs.length; i++) {
      if (convertedArgs[i] == null) {
        continue;
      }
      
      Class<?> classArg = constructorArgs[i];
      Class<?> classInputArg = convertedArgs[i].getClass();
      if (!constructorArgs[i].isAssignableFrom(convertedArgs[i].getClass())) {
        if (constructorArgs[i].isPrimitive()) {
          constructorMatched = isWrapper(classArg, classInputArg);
        } else {
          constructorMatched = false;
        }
      }
      if (!constructorMatched) {
        break;
      }
    }
    return constructorMatched;
  }

  Object[] getConvertedConstructorArgsTypes(final Constructor<?> constructor, final Object[] args) {
    Type[] argumentTypes = constructor.getGenericParameterTypes();
    try {
      //from fixture call we get only String values
      return ConverterSupport.convertArgs(args, argumentTypes);
    } catch (SlimError ex) {
      //swallow the exception silently, as for this step it's not relevant
      return args;
    }
  }

  private boolean isWrapper(Class<?> classArg, Class<?> classInputArg) {
    return ((Long.TYPE.equals(classArg)) && (Long.class.equals(classInputArg)))
            || ((Double.TYPE.equals(classArg)) && (Double.class.equals(classInputArg)))
            || ((Float.TYPE.equals(classArg)) && (Float.class.equals(classInputArg)))
            || ((Integer.TYPE.equals(classArg)) && (Integer.class.equals(classInputArg)))
            || ((Character.TYPE.equals(classArg)) && (Character.class.equals(classInputArg)))
            || ((Short.TYPE.equals(classArg)) && (Short.class.equals(classInputArg)))
            || ((Byte.TYPE.equals(classArg)) && (Byte.class.equals(classInputArg)))
            || ((Boolean.TYPE.equals(classArg)) && (Boolean.class.equals(classInputArg)));
  }

  protected Object newInstance(Constructor<?> constructor, Object... initargs) throws InvocationTargetException, InstantiationException, IllegalAccessException {
    return constructor.newInstance(initargs);
  }

  @Override
  public MethodExecutionResult findAndInvoke(String methodName, Object instance, Object... args) throws Throwable {
    Method method = findMatchingMethod(methodName, instance, args);
    if (method != null) {
      return this.invokeMethod(instance, method, args);
    }
    return MethodExecutionResult.noMethod(methodName, instance.getClass(), args.length);
  }

  protected Method findMatchingMethod(String methodName, Object instance, Object... args) {
    Class<?> k = instance.getClass();
    Method[] methods = k.getMethods();

    int nArgs = args.length;
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
    try {
      Object result;
      if (instance instanceof InteractionAwareFixture) {
        // invoke via interaction, so it can also do its thing on the aroundMethod invocation
        Object[] args = {this, method, convertedArgs};
        result = methodInvoke(AROUND_METHOD, instance, args);
      } else {
        result = methodInvoke(method, instance, convertedArgs);
      }
      return result;
    } catch (InvocationTargetException e) {
      if (e.getCause() != null) {
        throw e.getCause();
      } else {
        throw e.getTargetException();
      }
    }
  }

  @Override
  public Object methodInvoke(Method method, Object instance, Object... convertedArgs) throws Throwable {
    try {
      return method.invoke(instance, convertedArgs);
    } catch (InvocationTargetException e) {
      if (e.getCause() != null) {
        throw e.getCause();
      } else {
        throw e.getTargetException();
      }
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("Bad call of: " + method.getDeclaringClass().getName() + "." + method.getName()
              + ". On instance of: " + instance.getClass().getName(), e);
    }
  }

  private int getConstructorArgsConvertionDiff(Object[] args, Object[] convertedArgs) {
    int diffCounter = 0;
    for (int i = 0; i < args.length; i++) {
      if (args[i] != null) {
        //this implies that a conversation was made
        if (!args[i].equals(convertedArgs[i])) {
          diffCounter++;
        }
      }
    }
    return diffCounter;
  }
}
