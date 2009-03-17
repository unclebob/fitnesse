// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import java.lang.reflect.Method;
import java.util.Stack;

import fit.ActionFixture;
import fit.Parse;

public class LoopingActionFixture extends ActionFixture {

  Stack<Parse> loopContexts = new Stack<Parse>();
  Parse rows;

  boolean isSpecialName(String name) {
    return name.equals("do") || name.equals("while");
  }

  Method getAction(String name) throws SecurityException, NoSuchMethodException {
    String methodName = isSpecialName(name) ? ("action_" + name) : name;
    return getClass().getMethod(methodName, empty);
  }

  public void doRows(Parse rows) {
    this.rows = rows;
    while (this.rows != null) {
      doRow(this.rows);
      this.rows = this.rows.more;
    }
  }

  public void doCells(Parse cells) {
    this.cells = cells;
    try {
      Method action = getAction(cells.text());
      action.invoke(this);
    }
    catch (Exception e) {
      exception(cells, e);
    }
  }

  public void action_do() {
    loopContexts.push(rows);
  }

  public void action_while() throws Exception {
    String methodName = cells.more.text();
    Method action = actor.getClass().getMethod(methodName);
    Boolean result = (Boolean) action.invoke(actor);
    if (result.booleanValue()) {
      rows = loopContexts.peek();
    } else {
      loopContexts.pop();
    }
  }
}

