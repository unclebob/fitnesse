// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slimTables;

import java.util.Map;

import fitnesse.responders.run.slimResponder.SlimTestContext;

public class SlimErrorTable extends SlimTable {
  public SlimErrorTable(Table table, String id, SlimTestContext testContext) {
    super(table, id, testContext);
  }

  protected String getTableType() {
    return "UnknownTableType";
  }

  public void appendInstructions() {
  }

  public void evaluateExpectations(Map<String, Object> returnValues) {
    String tableType = table.getCellContents(0, 0);
    String errorMessage = fail(String.format("\"%s\" is not a valid table type.", tableType));
    table.setCell(0, 0, errorMessage);
  }

  public void evaluateReturnValues(Map<String, Object> returnValues) {
  }
}
