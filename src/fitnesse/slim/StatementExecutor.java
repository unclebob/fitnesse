// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import fitnesse.slim.converters.*;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * This is the API for executing a SLIM statement. This class should not know
 * about the syntax of a SLIM statement.
 */

public class StatementExecutor implements StatementExecutorInterface {
  static final String MESSAGE_NO_METHOD_IN_CLASS = "message:<<NO_METHOD_IN_CLASS %s[%d] %s.>>";
  
  private Map<String, Object> instances = new HashMap<String, Object>();
  private VariableStore variables = new VariableStore();
  private List<String> paths = new ArrayList<String>();

  private boolean stopRequested = false;

  public StatementExecutor() {
    Slim.addConverter(void.class, new VoidConverter());
    Slim.addConverter(String.class, new StringConverter());
    Slim.addConverter(int.class, new IntConverter());
    Slim.addConverter(double.class, new DoubleConverter());
    Slim.addConverter(Integer.class, new IntConverter());
    Slim.addConverter(Double.class, new DoubleConverter());
    Slim.addConverter(char.class, new CharConverter());
    Slim.addConverter(boolean.class, new BooleanConverter());
    Slim.addConverter(Boolean.class, new BooleanConverter());
    Slim.addConverter(Date.class, new DateConverter());
    Slim.addConverter(List.class, new ListConverter());
    Slim.addConverter(Integer[].class, new IntegerArrayConverter());
    Slim.addConverter(int[].class, new IntegerArrayConverter());
    Slim.addConverter(String[].class, new StringArrayConverter());
    Slim.addConverter(boolean[].class, new BooleanArrayConverter());
    Slim.addConverter(Boolean[].class, new BooleanArrayConverter());
    Slim.addConverter(double[].class, new DoubleArrayConverter());
    Slim.addConverter(Double[].class, new DoubleArrayConverter());
    PropertyEditorManager.registerEditor(Map.class, MapEditor.class);
  }

  public void setVariable(String name, Object value) {
    variables.setVariable(name, value);
  }

  public Object addPath(String path) {
    paths.add(path);
    return "OK";
  }

  public Object getInstance(String instanceName) {
    Object instance = instances.get(instanceName);
    if (instance != null)
      return instance;
    throw new SlimError(String.format("message:<<NO_INSTANCE %s.>>", instanceName));
  }

  public Converter getConverter(Class<?> k) {
    Converter c = Slim.converters.get(k);
    if (c != null)
      return c;
    PropertyEditor pe = PropertyEditorManager.findEditor(k);
    if (pe != null) {
      return new PropertyEditorConverter(pe);
    }
    return null;
  }

  public Object create(String instanceName, String className, Object[] args) {
    try {
      Object instance = createInstanceOfConstructor(className, replaceVariables(args));
      instances.put(instanceName, instance);
      return "OK";
    } catch (SlimError e) {
      return couldNotInvokeConstructorException(className, args);
    } catch (IllegalArgumentException e) {
      return couldNotInvokeConstructorException(className, args);
    } catch (Throwable e) {
      return exceptionToString(e);
    }
  }

  private String couldNotInvokeConstructorException(String className, Object[] args) {
    return exceptionToString(new SlimError(String.format(
        "message:<<COULD_NOT_INVOKE_CONSTRUCTOR %s[%d]>>", className, args.length)));
  }

  private Object createInstanceOfConstructor(String className, Object[] args) throws Exception {
    Class<?> k = searchPathsForClass(className);
    Constructor<?> constructor = getConstructor(k.getConstructors(), args);
    if (constructor == null)
      throw new SlimError(String.format("message:<<NO_CONSTRUCTOR %s>>", className));

    return constructor.newInstance(convertArgs(args, constructor.getParameterTypes()));
  }

  private Class<?> searchPathsForClass(String className) {
    Class<?> k = getClass(className);
    if (k != null)
      return k;
    List<String> reversedPaths = new ArrayList<String>(paths);
    Collections.reverse(reversedPaths);
    for (String path : reversedPaths) {
      k = getClass(path + "." + className);
      if (k != null)
        return k;
    }
    throw new SlimError(String.format("message:<<NO_CLASS %s>>", className));
  }

  private Class<?> getClass(String className) {
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  private Constructor<?> getConstructor(Constructor<?>[] constructors, Object[] args) {
    for (Constructor<?> constructor : constructors) {
      Class<?> arguments[] = constructor.getParameterTypes();
      if (arguments.length == args.length)
        return constructor;
    }
    return null;
  }

  public Object call(String instanceName, String methodName, Object... args) {
    try {
      Object instance = getInstance(instanceName);
      return tryToInvokeMethod(instance, methodName, replaceVariables(args));
    } catch (Throwable e) {
      return exceptionToString(e);
    }
  }

  private Object[] replaceVariables(Object[] args) {
    return variables.replaceVariables(args);
  }

  private String exceptionToString(Throwable exception) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter pw = new PrintWriter(stringWriter);
    exception.printStackTrace(pw);
    if (exception.getClass().toString().contains("StopTest")) {
      stopRequested = true;
      return SlimServer.EXCEPTION_STOP_TEST_TAG + stringWriter.toString();
    } else {
      return SlimServer.EXCEPTION_TAG + stringWriter.toString();
    }
  }

  private Object tryToInvokeMethod(Object instance, String methodName, Object args[])
      throws Throwable {
    Class<?> k = instance.getClass();
    Method method = findMatchingMethod(methodName, k, args.length);
    if (method == null) {
      return tryInvokeMethodOnSystemUnderTest(instance, methodName, args, k);
    }
    return internalInvokeMethod(instance, method, args);
  }

  @SuppressWarnings("unchecked")
  private Object internalInvokeMethod(Object instance, Method method, Object[] args)
      throws Throwable {
    Object convertedArgs[] = convertArgs(method, args);
    Object retval = callMethod(instance, method, convertedArgs);
    Class<?> retType = method.getReturnType();
    if (retType == List.class && retval instanceof List)
      return retval;
    return convertToString(retval, retType);
  }

  private Object tryInvokeMethodOnSystemUnderTest(Object instance, String methodName,
      Object[] args, Class<?> k) throws IllegalAccessException, Throwable, SlimError {
    Field field = findSystemUnderTest(k);
    if (field != null) {
      Object systemUnderTest = field.get(instance);
      Method method = findMatchingMethod(methodName, systemUnderTest.getClass(), args.length);
      if (method != null) {
        return internalInvokeMethod(systemUnderTest, method, args);
      }
    }
    throw noMethodInClass(methodName, k, args.length);
  }

  private Object callMethod(Object instance, Method method, Object[] convertedArgs)
      throws Throwable {
    Object retval = null;
    try {
      retval = method.invoke(instance, convertedArgs);
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
    return retval;
  }

  private Method findMatchingMethod(String methodName, Class<? extends Object> k, int nArgs) {
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

  private SlimError noMethodInClass(String methodName, Class<? extends Object> k, int nArgs) {
    return new SlimError(String.format(MESSAGE_NO_METHOD_IN_CLASS, methodName, nArgs, k.getName()));
  }

  private Field findSystemUnderTest(Class<?> k) {
    Field[] fields = k.getDeclaredFields();
    for (Field field : fields) {
      if (isSystemUnderTest(field)) {
        return field;
      }
    }
    return null;
  }

  private boolean isSystemUnderTest(Field field) {
    return "systemUnderTest".equals(field.getName()) || field.getAnnotation(SystemUnderTest.class) != null;
  }

  private Object[] convertArgs(Method method, Object args[]) {
    Class<?>[] argumentTypes = method.getParameterTypes();
    Object[] convertedArgs = convertArgs(args, argumentTypes);
    return convertedArgs;
  }

  // todo refactor this mess
  @SuppressWarnings("unchecked")
  private Object[] convertArgs(Object[] args, Class<?>[] argumentTypes) {
    Object[] convertedArgs = new Object[args.length];
    for (int i = 0; i < argumentTypes.length; i++) {
      Class<?> argumentType = argumentTypes[i];
      if (argumentType == List.class && args[i] instanceof List) {
        convertedArgs[i] = args[i];
      } else {
        Converter converter = getConverter(argumentType);
        if (converter != null)
          convertedArgs[i] = converter.fromString((String) args[i]);
        else
          throw new SlimError(String.format("message:<<NO_CONVERTER_FOR_ARGUMENT_NUMBER %s.>>",
              argumentType.getName()));
      }
    }
    return convertedArgs;
  }

  private Object convertToString(Object retval, Class<?> retType) {
    Converter converter = getConverter(retType);
    if (converter != null)
      return converter.toString(retval);
    if (retval == null)
      return "null";
    else
      return retval.toString();
  }

  public boolean stopHasBeenRequested() {
    return stopRequested;
  }

  public void reset() {
    stopRequested = false;
  }
}
