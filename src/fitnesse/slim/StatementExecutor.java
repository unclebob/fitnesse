// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/** This is the API for executing a SLIM statement. This class should not know about the syntax of a SLIM statement. */

public class StatementExecutor implements StatementExecutorInterface {
  private static final String SLIM_HELPER_LIBRARY_INSTANCE_NAME = "SlimHelperLibrary";

  private boolean stopRequested = false;
  private SlimExecutionContext context;
  private List<MethodExecutor> executorChain = new ArrayList<MethodExecutor>();

  public StatementExecutor() {
    this(new SlimExecutionContext());
  }

  public StatementExecutor(SlimExecutionContext context) {
    this.context = context;

    executorChain.add(new FixtureMethodExecutor(context));
    executorChain.add(new SystemUnderTestMethodExecutor(context));
    executorChain.add(new LibraryMethodExecutor(context));

    addSlimHelperLibraryToLibraries();
  }

  private void addSlimHelperLibraryToLibraries() {
    SlimHelperLibrary slimHelperLibrary = new SlimHelperLibrary();
    slimHelperLibrary.setStatementExecutor(this);
    context.addLibrary(new Library(SLIM_HELPER_LIBRARY_INSTANCE_NAME, slimHelperLibrary));
  }

  @Override
  public Object getInstance(String instanceName) {
    return context.getInstance(instanceName);
  }

  @Override
  public void setInstance(final String actorInstanceName, final Object actor) {
    context.setInstance(actorInstanceName, actor);
  }

  @Override
  public Object addPath(String path) {
    context.addPath(path);
    return "OK";
  }

  @Override
  public void setVariable(String name, Object value) {
    context.setVariable(name, value);
  }

  @Override
  public Object create(String instanceName, String className, Object[] args) {
    try {
      context.create(instanceName, className, args);
      return "OK";
    } catch (SlimError e) {
      return couldNotInvokeConstructorException(className, args);
    } catch (IllegalArgumentException e) {
      return couldNotInvokeConstructorException(className, args);
    } catch (Throwable e) {
      return exceptionToString(e);
    }
  }

  @Override
  public Object call(String instanceName, String methodName, Object... args) {
    try {
      return getMethodExecutionResult(instanceName, methodName, args)
          .returnValue();
    } catch (Throwable e) {
      return exceptionToString(e);
    }
  }

  @Override
  public Object callAndAssign(String variable, String instanceName,
      String methodName, Object[] args) {
    try {
      MethodExecutionResult result = getMethodExecutionResult(
          instanceName, methodName, args);
      setVariable(variable, result);
      return result.returnValue();
    } catch (Throwable e) {
      return exceptionToString(e);
    }
  }

  private MethodExecutionResult getMethodExecutionResult(String instanceName,
      String methodName, Object... args) throws Throwable {
    MethodExecutionResults results = new MethodExecutionResults();
    for (MethodExecutor executor : executorChain) {
      MethodExecutionResult result = executor.execute(instanceName, methodName, context.replaceSymbols(args));
      if (result.hasResult()) {
        return result;
      }
      results.add(result);
    }
    return results.getFirstResult();
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

  private String couldNotInvokeConstructorException(String className,
      Object[] args) {
    return exceptionToString(new SlimError(String.format(
        "message:<<%s %s[%d]>>", SlimServer.COULD_NOT_INVOKE_CONSTRUCTOR, className,
        args.length)));
  }

  @Override
  public boolean stopHasBeenRequested() {
    return stopRequested;
  }

  @Override
  public void reset() {
    stopRequested = false;
  }
}
