// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static fitnesse.slim.SlimException.isStopSuiteException;
import static fitnesse.slim.SlimException.isStopTestException;
import static java.lang.String.format;

/**
 * This is the API for executing a SLIM statement. This class should not know about the syntax of a SLIM statement.
 */

public class StatementExecutor implements StatementExecutorInterface {
  private static final String SLIM_HELPER_LIBRARY_INSTANCE_NAME = "SlimHelperLibrary";

  private boolean stopRequested = false;
  private SlimExecutionContext context;
  private List<MethodExecutor> executorChain = new ArrayList<MethodExecutor>();

  public StatementExecutor() {
    this(null);
  }

  public StatementExecutor(SlimExecutionContext context) {
    if (context == null) {
      this.context = new SlimExecutionContext();
    } else {
      this.context = context;
    }

    executorChain.add(new FixtureMethodExecutor(this.context));
    executorChain.add(new SystemUnderTestMethodExecutor(this.context));
    executorChain.add(new LibraryMethodExecutor(this.context));

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
  public void addPath(String path) throws SlimException {
    context.addPath(path);
  }

  @Override
  public void assign(String name, Object value) {
    context.setVariable(name, value);
  }
  
  @Override
  public Object getSymbol(String symbolName) {
    MethodExecutionResult result = context.getVariable(symbolName);
    if (result == null) {
      return null;
    }
    return result.returnValue();
  }

  @Override
  public void create(String instanceName, String className, Object... args) throws SlimException {
    try {
      context.create(instanceName, className, args);
      // TODO Hack for supporting SlimHelperLibrary, please remove.
      Object newInstance = context.getInstance(instanceName);
      if (newInstance instanceof StatementExecutorConsumer) {
        ((StatementExecutorConsumer) newInstance).setStatementExecutor(this);
      }
    } catch (SlimError e) {
      throw new SlimException(format("%s[%d]", className, args.length), e, SlimServer.COULD_NOT_INVOKE_CONSTRUCTOR,
          true);
    } catch (IllegalArgumentException e) {
      throw new SlimException(format("%s[%d]", className, args.length), e, SlimServer.COULD_NOT_INVOKE_CONSTRUCTOR,
          true);
    } catch (InvocationTargetException e) {
      checkExceptionForStop(e.getTargetException());
      throw new SlimException(e.getTargetException(), true);
    } catch (Throwable e) { // NOSONAR
      checkExceptionForStop(e);
      throw new SlimException(e);
    }
  }

  @Override
  public Object call(String instanceName, String methodName, Object... args) throws SlimException {
    try {
      return getMethodExecutionResult(instanceName, methodName, args).returnValue();
    } catch (Throwable e) { // NOSONAR
      checkExceptionForStop(e);
      throw new SlimException(e);
    }
  }

  @Override
  public Object callAndAssign(String variable, String instanceName, String methodName, Object... args) throws SlimException {
    try {
      MethodExecutionResult result = getMethodExecutionResult(instanceName, methodName, args);
      context.setVariable(variable, result);
      return result.returnValue();
    } catch (Throwable e) { // NOSONAR
      checkExceptionForStop(e);
      throw new SlimException(e);
    }
  }

  private MethodExecutionResult getMethodExecutionResult(String instanceName, String methodName, Object... args) throws Throwable {
    MethodExecutionResults results = new MethodExecutionResults();
    for (int i = 0; i < executorChain.size(); i++) {
      MethodExecutionResult result = executorChain.get(i).execute(instanceName, methodName, context.replaceSymbols(args));
      if (result.hasResult()) {
        return result;
      }
      results.add(result);
    }
    return results.getFirstResult();
  }

  private void checkExceptionForStop(Throwable exception) {
    if (isStopTestException(exception) || isStopSuiteException(exception)) {
      stopRequested = true;
    }
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
