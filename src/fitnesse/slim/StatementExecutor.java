// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import fitnesse.slim.converters.*;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is the API for executing a SLIM statement.  This class should not know about
 * the syntax of a SLIM statement.
 */

public class StatementExecutor {
  private Map<String, Object> instances = new HashMap<String, Object>();
  private Map<String, Object> variables = new HashMap<String, Object>();
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

  }

  public void setVariable(String name, Object value) {
    variables.put(name, value);
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
    }  catch (IllegalArgumentException e) {
      return couldNotInvokeConstructorException(className, args);
    }
    catch (Throwable e) {
      return exceptionToString(e);
    }
  }

  private String couldNotInvokeConstructorException(String className, Object[] args) {
    return exceptionToString(
        new SlimError(
        String.format("message:<<COULD_NOT_INVOKE_CONSTRUCTOR %s[%d]>>", className, args.length)
      )
    );
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

  private String exceptionToString(Throwable exception) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter pw = new PrintWriter(stringWriter);
    exception.printStackTrace(pw);
    if (exception.getClass().toString().contains("StopTest")) {
      stopRequested = true;
      return SlimServer.EXCEPTION_STOP_TEST_TAG + stringWriter.toString();
    }
    else {
      return SlimServer.EXCEPTION_TAG + stringWriter.toString();    
    }
  }

  private Object[] replaceVariables(Object[] args) {
    Object result[] = new Object[args.length];
    for (int i = 0; i < args.length; i++)
      result[i] = replaceVariable(args[i]);

    return result;
  }

  private List<Object> replaceArgsInList(List<Object> objects) {
    List<Object> result = new ArrayList<Object>();
    for (Object object : objects)
      result.add(replaceVariable(object));

    return result;
  }

  @SuppressWarnings("unchecked")
  private Object replaceVariable(Object object) {
    if (object instanceof List)
      return (replaceArgsInList((List<Object>) object));
    else
      return (replaceVariablesInString((String) object));
  }

  private Object replaceVariablesInString(String arg) {
    Pattern symbolPattern = Pattern.compile("\\$([a-zA-Z]\\w*)");
    int startingPosition = 0;
    while (true) {
      if ("".equals(arg))
        break;
      Matcher symbolMatcher = symbolPattern.matcher(arg.substring(startingPosition));
      if (symbolMatcher.find()) {
        String symbolName = symbolMatcher.group(1);
        arg = replaceSymbolInArg(arg, symbolName);
        startingPosition += symbolMatcher.start(1);
      } else
        break;
    }
    return arg;
  }

  private String replaceSymbolInArg(String arg, String symbolName) {
    if (variables.containsKey(symbolName)) {
      String replacement = (String) variables.get(symbolName);
      if (replacement == null)
        replacement = "null";
      arg = arg.replace("$" + symbolName, replacement);
    }
    return arg;
  }

  private Object tryToInvokeMethod(Object instance, String methodName, Object args[]) throws Throwable {
    Class<?> k = instance.getClass();
    Method method = findMatchingMethod(methodName, k, args.length);
    Object convertedArgs[] = convertArgs(method, args);
    Object retval = callMethod(instance, method, convertedArgs);
    Class<?> retType = method.getReturnType();
    if (retType == List.class && retval instanceof List)
      return retval;
    return convertToString(retval, retType);
  }

  private Object callMethod(Object instance, Method method, Object[] convertedArgs) throws Throwable {
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
      if (hasMatchingName && hasMatchingArguments)
        return method;
    }
    throw new SlimError(String.format("message:<<NO_METHOD_IN_CLASS %s[%d] %s.>>", methodName, nArgs, k.getName()));
  }

  private Object[] convertArgs(Method method, Object args[]) {
    Class<?>[] argumentTypes = method.getParameterTypes();
    Object[] convertedArgs = convertArgs(args, argumentTypes);
    return convertedArgs;
  }

  //todo refactor this mess
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
          throw
            new SlimError(String.format("message:<<NO_CONVERTER_FOR_ARGUMENT_NUMBER %s.>>", argumentType.getName()));
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
