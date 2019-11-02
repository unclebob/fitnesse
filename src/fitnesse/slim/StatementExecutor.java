// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static fitnesse.slim.SlimException.*;
import static java.lang.String.format;

/**
 * This is the API for executing a SLIM statement. This class should not know about the syntax of a SLIM statement.
 */

public class StatementExecutor implements StatementExecutorInterface {
  private static final String SLIM_HELPER_LIBRARY_INSTANCE_NAME = "SlimHelperLibrary";
  public static final String SLIM_AGENT_FIXTURE_HANDLES_SYMBOLS = "SLIM_AGENT_FIXTURE_HANDLES_SYMBOLS";

  private boolean stopRequested = false;
  private SlimExecutionContext context;
  private List<MethodExecutor> executorChain = new ArrayList<>();
  private Pattern patternOfFixturesHandlingSymbols = null;

  public StatementExecutor() {
    this(new SlimExecutionContext(JavaSlimFactory.createInteraction(null)));
  }

  public StatementExecutor(SlimExecutionContext context) {
    this.context = context;

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
    checkForPatternOfFixturesHandlingSymbols(name);
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
  public Object getSymbolObject(String symbolName) {
    MethodExecutionResult result = context.getVariable(symbolName);
    if (result == null) {
      return null;
    }
    return result.getObject();
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
      checkForPatternOfFixturesHandlingSymbols(variable);
      return result.returnValue();
    } catch (Throwable e) { // NOSONAR
      checkExceptionForStop(e);
      throw new SlimException(e);
    }
  }

  private MethodExecutionResult getMethodExecutionResult(String instanceName, String methodName, Object... args) throws Throwable {
    MethodExecutionResults results = new MethodExecutionResults();
    Boolean ignoreSymbols = ignoreSymbols( instanceName,  methodName);
	if (!ignoreSymbols){
		args = context.replaceSymbols(args);
	}
    for (MethodExecutor anExecutorChain : executorChain) {
      MethodExecutionResult result = anExecutorChain.execute(instanceName, methodName, args);
      if (result.hasResult()) {
        return result;
      }
      results.add(result);
    }
    return results.getFirstResult();
  }

  /**
   *
   * @return true is the fixture will handles symbols assignments and lookups itself.
   *         This should be a rare exception and is not recommended
   *
   *  Would be nice to use the classname but we don't know it at this point.
   *
   */

  private Boolean ignoreSymbols(String instanceName, String methodName){
	  try{

	    if (this.patternOfFixturesHandlingSymbols == null) return false;
	    return patternOfFixturesHandlingSymbols.matcher(instanceName + "." + methodName).matches();

	  }catch (Exception e){
		  return false;
	  }
  }

  private void checkForPatternOfFixturesHandlingSymbols(String symbolName){
    if(!SLIM_AGENT_FIXTURE_HANDLES_SYMBOLS.equals(symbolName)) return;
    // Special Symbol Name need to update
    try{
      MethodExecutionResult mer = context.getVariable(SLIM_AGENT_FIXTURE_HANDLES_SYMBOLS);
      if (mer == null) return;

      try{
        if(mer.returnValue() == null){
          patternOfFixturesHandlingSymbols = null;
        }else{
          patternOfFixturesHandlingSymbols = Pattern.compile(mer.returnValue().toString());
        }
      }catch (Exception e){
        patternOfFixturesHandlingSymbols = null;
      }

    }catch (Exception e){
      return;
    }


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
