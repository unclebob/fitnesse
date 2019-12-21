package fitnesse.slim.fixtureInteraction;

import fitnesse.slim.ConverterSupport;
import fitnesse.slim.MethodExecutionResult;
import fitnesse.slim.SlimError;
import fitnesse.slim.SlimException;
import fitnesse.slim.SlimServer;
import fitnesse.slim.StackTraceEnricher;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static fitnesse.util.StringUtils.swapCaseOfFirstLetter;

public class SimpleInteraction implements FixtureInteraction {

  private static final Method AROUND_METHOD;

  static {
    try {
      AROUND_METHOD = InteractionAwareFixture.class.getMethod("aroundSlimInvoke", FixtureInteraction.class, Method.class, Object[].class);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }
  
// path cache is required for invoking static methods
private List<String> pathsCache = new ArrayList<>();

  @Override
  public Object createInstance(List<String> paths, String className, Object[] args)
          throws IllegalArgumentException, InstantiationException,
          IllegalAccessException, InvocationTargetException{
    pathsCache = paths;
    Class<?> k = null;
    try{
      k = searchPathsForClass(paths, className);
    }
    catch (SlimError errorClassNotFound) {
      try {
        MethodExecutionResult mER = invokeStaticMethod(className, paths, args);
        if (mER != null) {
          if (mER.hasResult()) {
            return mER.getObject();
          } else {
            // Error occurred, throw or return it
            return mER.returnValue();
          }
        }
      } catch (Throwable e) {
        throw new InstantiationException(new StringBuilder().append("Failed to call static method '").append(className).append("': ")
          .append("\nCaused by: ").append(e.getClass().getName()).append(": ")
          .append(e.getMessage()).append( new StackTraceEnricher().getStackTraceAsString(e)).toString());
      }
      throw errorClassNotFound;
    }

    Constructor<?> constructor = getConstructor(k, args);
    if (constructor == null) {
      throw new SlimError(String.format("message:<<%s %s>>", SlimServer.NO_CONSTRUCTOR, className));
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

    if (k == null)
      k = findClassInPaths(paths, className, swapCaseOfFirstLetter(className));

    if (k != null)
      return k;
    else
      throw new SlimError(String.format("message:<<%s %s>>", SlimServer.NO_CLASS, className));
  }

  private Class<?> findClassInPaths(List<String> paths, String... classNames) {
    Class<?> k = null;

    for (int i = 0; i < classNames.length && k == null; i++)
      k = findClassInPaths(paths, classNames[i]);

    return k;
  }

  private Class<?> findClassInPaths(List<String> paths, String className) {
    Class<?> k = null;
    
    if (paths == null) {
      return null;
    }
    for (int i = 0; i < paths.size() && k == null; i++)
      k = getClass(paths.get(i) + "." + className);

    return k;
  }

  protected Class<?> getClass(String className) {
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException e) {
      return null;
    } catch (NoClassDefFoundError e) {
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
  protected Constructor<?> getConstructor(Class<?> clazz, Object... args) {
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
          //the constructor with most conversion changes should be more eligible for picking-up
          currentDiffCounter = getConstructorArgsConvertionDiff(args, convertedArgs);
          //pick-up the constructor with the most explicit args types
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
    } catch (Throwable ex) {
      // Conversion failed, either no converter or data is not correctly formatted
      //swallow the exception silently, as for this step it's not relevant
      // return the not converted args list, this ensures this constructor will only be picked if no better constructor is found
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
    } else {
      MethodExecutionResult mER = invokeStaticMethod(methodName, pathsCache, args );
      if (mER != null) {
        return mER;
      }
    }
    return MethodExecutionResult.noMethod(methodName, instance.getClass(), args.length);
  }

  private MethodExecutionResult invokeStaticMethod(String methodName, List<String> paths, Object... args) throws Throwable {
    Method method;
    int i = methodName.lastIndexOf('.');
    if (i >=0) {
      // Static Method
      String className = methodName.substring(0, i);
      String staticMethodName = methodName.substring(i+1);
      Class<?> clazz = searchPathsForClass(paths, className);
      if (clazz != null) {
        method = findMatchingMethod(new String[]{staticMethodName}, clazz.getMethods(), args.length);
        if (method != null) {
          return this.invokeMethod(null, method, args);
        } else {
            return MethodExecutionResult.noMethod(staticMethodName + "(static)", clazz, args.length);
        }
      }
    }
    return null;
}

  protected Method findMatchingMethod(String methodName, Object instance, Object... args) {
    String[] methodNames = new String[]{methodName, swapCaseOfFirstLetter(methodName)};

    return findMatchingMethod(methodNames, instance.getClass().getMethods(), args.length);
  }

  private Method findMatchingMethod(String[] methodNames, Method[] methods, int nArgs) {
    Method method = null;

    for (int i = 0; i < methodNames.length && method == null; i++)
      method = findMatchingMethod(methodNames[i], methods, nArgs);

    return method;
  }

  private Method findMatchingMethod(String methodName, Method[] methods, int nArgs) {
    Method method = null;

    for (int i = 0; i < methods.length && method == null; i++)
      if (isMatchingMethod(methods[i], methodName, nArgs))
        method = methods[i];

    return method;
  }


  private boolean isMatchingMethod(Method method, String methodName, int nArgs) {
    boolean hasMatchingName = method.getName().equals(methodName);
    boolean hasMatchingArguments = method.getParameterTypes().length == nArgs;
    return hasMatchingName && hasMatchingArguments;
  }

  protected MethodExecutionResult invokeMethod(Object instance, Method method, Object[] args) throws Throwable {
      Object[] convertedArgs = null;
      Object retval = null;
      Class<?> retType = method.getReturnType();
      try {
        convertedArgs = convertArgs(method, args);
      } catch (Exception e) {
        String methodName = method.getDeclaringClass().getName() + "." + MethodExecutionResult.methodToString(method)
          + "." + ((instance == null) ? "" : " On instance of: " + instance.getClass().getName());
    	return new MethodExecutionResult.InvalidParameters(methodName, e);
      }
      retval = callMethod(instance, method, convertedArgs);
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
              + "." + ((instance == null) ? "" : " On instance of: " + instance.getClass().getName()), e);
    }
    catch (Exception e){
      String methodName = MethodExecutionResult.methodToString(method);
      throw new RuntimeException("Exception when invoking: " + method.getDeclaringClass().getName() + "." + methodName
              + "." + ((instance == null) ? "" : " On instance of: " + instance.getClass().getName()), e);
      
    }
  }

  private int getConstructorArgsConvertionDiff(Object[] args, Object[] convertedArgs) {
    int diffCounter = 0;
    for (int i = 0; i < args.length; i++) {
      if (args[i] != null) {
        //this implies that a conversion was made
        if (!args[i].equals(convertedArgs[i])) {
          diffCounter++;
        }
      }
    }
    return diffCounter;
  }
}
