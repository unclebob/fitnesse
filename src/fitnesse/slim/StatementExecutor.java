// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import java.beans.PropertyEditorManager;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fitnesse.slim.converters.BooleanArrayConverter;
import fitnesse.slim.converters.BooleanConverter;
import fitnesse.slim.converters.CharConverter;
import fitnesse.slim.converters.DateConverter;
import fitnesse.slim.converters.DoubleArrayConverter;
import fitnesse.slim.converters.DoubleConverter;
import fitnesse.slim.converters.IntConverter;
import fitnesse.slim.converters.IntegerArrayConverter;
import fitnesse.slim.converters.ListConverter;
import fitnesse.slim.converters.MapEditor;
import fitnesse.slim.converters.StringArrayConverter;
import fitnesse.slim.converters.StringConverter;
import fitnesse.slim.converters.VoidConverter;

/**
 * This is the API for executing a SLIM statement. This class should not know
 * about the syntax of a SLIM statement.
 */

public class StatementExecutor implements StatementExecutorInterface {

  private Map<String, Object> instances = new HashMap<String, Object>();
  private List<Library> libraries = new ArrayList<Library>();

  private List<MethodExecutor> executorChain = new ArrayList<MethodExecutor>();

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

    executorChain.add(new FixtureMethodExecutor(instances));
    executorChain.add(new SystemUnderTestMethodExecutor(instances));
    executorChain.add(new LibraryMethodExecutor(libraries));
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
    if (instance != null) {
      return instance;
    }

    for (Library library : libraries) {
      if (library.instanceName.equals(instanceName)) {
        return library.instance;
      }
    }
    throw new SlimError(String.format("message:<<NO_INSTANCE %s.>>", instanceName));
  }

  public Converter getConverter(Class<?> k) {
    return ConverterSupport.getConverter(k);
  }

  public Object create(String instanceName, String className, Object[] args) {
    try {
      Object instance = createInstanceOfConstructor(className, replaceVariables(args));
      if (isLibrary(instanceName)) {
        libraries.add(new Library(instanceName, instance));
      } else {
        instances.put(instanceName, instance);
      }
      return "OK";
    } catch (SlimError e) {
      return couldNotInvokeConstructorException(className, args);
    } catch (IllegalArgumentException e) {
      return couldNotInvokeConstructorException(className, args);
    } catch (Throwable e) {
      return exceptionToString(e);
    }
  }

  private boolean isLibrary(String instanceName) {
    return instanceName.startsWith("library");
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

    return constructor.newInstance(ConverterSupport.convertArgs(args, constructor
        .getParameterTypes()));
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
      MethodExecutionResults results = new MethodExecutionResults();
      for (int i = 0; i < executorChain.size(); i++) {
        MethodExecutionResult result = executorChain.get(i).execute(instanceName, methodName,
            replaceVariables(args));
        if (result.hasResult()) {
          return result.returnValue();
        }
        results.add(result);
      }
      return results.returnValue();
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

  public boolean stopHasBeenRequested() {
    return stopRequested;
  }

  public void reset() {
    stopRequested = false;
  }
}
