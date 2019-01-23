// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.test;

import java.util.List;

import fitnesse.slim.SlimError;

public class DecisionTableExecuteThrows {
  private int x = 1;
  private String whichMethodsFail = "execute";
  private final String totalDisaster = "Total Disaster";

  public DecisionTableExecuteThrows() {
  }

  public DecisionTableExecuteThrows(String fail) {
    whichMethodsFail = fail;
  }

  public void setWhichMethodsFail(String fail) {
    whichMethodsFail = fail;
  }

  public int x() {
    if (whichMethodsFail.contains("getX")
        || totalDisaster.equalsIgnoreCase(whichMethodsFail))
      throw new SlimError("GETX_THROWS");
    return x;
  }

  public void setX(int x) {
    if (whichMethodsFail.contains("setX")
        || totalDisaster.equalsIgnoreCase(whichMethodsFail))
      throw new SlimError("SETX_THROWS");
    this.x = x;
  }

  public void execute() {
    if (whichMethodsFail.contains("execute")
        || totalDisaster.equalsIgnoreCase(whichMethodsFail))
      throw new SlimError("EXECUTE_THROWS");
  }

  public void reset() {
    if (whichMethodsFail.contains("reset")
        || totalDisaster.equalsIgnoreCase(whichMethodsFail))
      throw new SlimError("RESET_THROWS");
  }

  public void beginTable() {
    if (whichMethodsFail.contains("beginTable")
        || totalDisaster.equalsIgnoreCase(whichMethodsFail))
      throw new SlimError("BEGINTABLE_THROWS");
  }

  public void endTable() {
    if (whichMethodsFail.contains("endTable")
        || totalDisaster.equalsIgnoreCase(whichMethodsFail))
      throw new SlimError("ENDTABLE_THROWS");
  }

  public void table(List<List<String>> table) {
    if (whichMethodsFail.contains("table")
        || totalDisaster.equalsIgnoreCase(whichMethodsFail))
      throw new SlimError("DOTABLE_THROWS");
  }
}
