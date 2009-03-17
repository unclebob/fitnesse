// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import java.util.ArrayList;
import java.util.List;

import util.ListUtility;

/**
 * executes a list of SLIM statements, and returns a list of return values.
 */
public class ListExecutor {
  private StatementExecutor executor;
  private boolean verbose;

  public ListExecutor() {
    this(false);
  }

  public ListExecutor(boolean verbose) {
    this.verbose = verbose;
    this.executor = new StatementExecutor();
  }

  public List<Object> execute(List<Object> statements) {
    String message = "!1 Instructions";
    verboseMessage(message);

    List<Object> result = new ArrayList<Object>();
    for (Object statement : statements) {
      List<Object> statementList = ListUtility.uncheckedCast(Object.class, statement);
      verboseMessage(statementList + "\n");
      Object retVal = new Statement(statementList).execute(executor);
      verboseMessage(retVal);
      verboseMessage("------");
      result.add(retVal);
    }
    return result;
  }

  private void verboseMessage(Object message) {
    if (verbose) System.out.println(message);
  }
}
