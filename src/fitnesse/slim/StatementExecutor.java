// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import fitnesse.slim.converters.*;

import java.beans.PropertyEditorManager;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * This is the API for executing a SLIM statement. This class should not know
 * about the syntax of a SLIM statement.
 */

public class StatementExecutor implements StatementExecutorInterface {

  private static final String SLIM_HELPER_LIBRARY_INSTANCE_NAME = "SlimHelperLibrary";
  private Map<String, Object> instances = new HashMap<String, Object>();
  private List<Library> libraries = new ArrayList<Library>();

  private List<MethodExecutor> executorChain = new ArrayList<MethodExecutor>();

  private VariableStore variables = new VariableStore();
  private List<String> paths = new ArrayList<String>();

  private boolean stopRequested = false;
  private String lastActor;

  public StatementExecutor() {
    PropertyEditorManager.registerEditor(Map.class, MapEditor.class);

    executorChain.add(new FixtureMethodExecutor(instances));
    executorChain.add(new SystemUnderTestMethodExecutor(instances));
    executorChain.add(new LibraryMethodExecutor(libraries));
    
    addSlimHelperLibraryToLibraries();
  }

  private void addSlimHelperLibraryToLibraries() {
    SlimHelperLibrary slimHelperLibrary = new SlimHelperLibrary();
    slimHelperLibrary.setStatementExecutor(this);
    libraries.add(new Library(SLIM_HELPER_LIBRARY_INSTANCE_NAME, slimHelperLibrary));
  }

  public void setVariable(String name, Object value) {
    variables.setSymbol(name, new MethodExecutionResult(value, Object.class));
  }
  
  private void setVariable(String name, MethodExecutionResult value) {
    variables.setSymbol(name, value);
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
      if (hasStoredActor(className)) {
        addToInstancesOrLibrary(instanceName, getStoredActor(className));
      } else {
        String replacedClassName = variables.replaceSymbolsInString(className);
        Object instance = createInstanceOfConstructor(replacedClassName, replaceSymbols(args));
        addToInstancesOrLibrary(instanceName, instance);
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

  private void addToInstancesOrLibrary(String instanceName, Object instance) {
    if (isLibrary(instanceName)) {
      libraries.add(new Library(instanceName, instance));
    } else {
      setInstance(instanceName, instance);
    }
  }

  public void setInstance(String instanceName, Object instance) {
    instances.put(instanceName, instance);
  }

  private boolean hasStoredActor(String nameWithDollar) {
    if (!variables.containsValueFor(nameWithDollar)) {
      return false;
    }
    Object potentialActor = getStoredActor(nameWithDollar);
    return potentialActor != null && !(potentialActor instanceof String);
  }

  private Object getStoredActor(String nameWithDollar) {
    return variables.getStored(nameWithDollar);
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

    Object newInstance = constructor.newInstance(ConverterSupport.convertArgs(args, constructor
        .getParameterTypes()));
    if (newInstance instanceof StatementExecutorConsumer) {
      ((StatementExecutorConsumer) newInstance).setStatementExecutor(this);
    }
    return newInstance;
    
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
      return getMethodExecutionResult(instanceName, methodName, args).returnValue();
    } catch (Throwable e) {
      return exceptionToString(e);
    }
  }

  private MethodExecutionResult getMethodExecutionResult(String instanceName, String methodName, Object... args)
      throws Throwable {
    MethodExecutionResults results = new MethodExecutionResults();
    for (int i = 0; i < executorChain.size(); i++) {
      MethodExecutionResult result = executorChain.get(i).execute(instanceName, methodName,
          replaceSymbols(args));
      if (result.hasResult()) {
        return result;
      }
      results.add(result);
    }
    return results.getFirstResult();
  }

  public Object callAndAssign(String variable, String instanceName, String methodName, Object[] args) {
    try {
      MethodExecutionResult result = getMethodExecutionResult(instanceName, methodName, args);
      setVariable(variable, result);
      return result.returnValue();
    } catch (Throwable e) {
      return exceptionToString(e);
    }
  }

  private Object[] replaceSymbols(Object[] args) {
    return variables.replaceSymbols(args);
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
