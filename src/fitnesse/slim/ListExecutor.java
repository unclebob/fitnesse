// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import util.ListUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * executes a list of SLIM statements, and returns a list of return values.
 */
public class ListExecutor {
  private StatementExecutorInterface executor;
  private NameTranslator methodNameTranslator;
  private boolean verbose;

  public ListExecutor(SlimFactory slimFactory) {
    this(false, slimFactory);
  }

  protected ListExecutor(boolean verbose, SlimFactory slimFactory) {
    this.verbose = verbose;
    this.executor = slimFactory.getStatementExecutor();
    this.methodNameTranslator = slimFactory.getMethodNameTranslator();
  }

  protected void setVerbose() {
    verbose = true;
  }

  private class Executive {
    public void prepareToExecute() { }

    public List<Object> executeStatements(List<Object> statements) {
      List<Object> result = new ArrayList<Object>();
      for (Object statement : statements)
        if (!executor.stopHasBeenRequested())
          result.add(executeStatement(statement));
      return result;
    }

    public Object executeStatement(Object statement) {
      return new Statement(asStatementList(statement), methodNameTranslator).execute(executor);
    }

    public void finalizeExecution() {
      if (executor.stopHasBeenRequested())
        executor.reset();
    }
  }

  private class LoggingExecutive extends Executive {
    public void prepareToExecute() {
      verboseMessage("!1 Instructions");
    }

    public Object executeStatement(Object statement) {
      List<Object> statementList = asStatementList(statement);
      verboseMessage(statementList + "\n");
      Object retVal = super.executeStatement(statement);
      verboseMessage(retVal);
      verboseMessage("------");
      return retVal;
    }
  }

  public List<Object> execute(List<Object> statements) {
    Executive e = verbose ? new LoggingExecutive() : new Executive();
    e.prepareToExecute();
    List<Object> result = e.executeStatements(statements);
    e.finalizeExecution();
    return result;
  }


  private List<Object> asStatementList(Object statement) {
    return ListUtility.uncheckedCast(Object.class, statement);
  }

  private void verboseMessage(Object message) {
    if (verbose) System.out.println(message);
  }
}
