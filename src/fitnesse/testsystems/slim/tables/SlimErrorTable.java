// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import java.util.Collections;
import java.util.List;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.results.Result;

public class SlimErrorTable extends SlimTable {
  public SlimErrorTable(Table table, String id, SlimTestContext testContext) {
    super(table, id, testContext);
  }

  protected String getTableType() {
    return "UnknownTableType";
  }

  public List<Object> getInstructions() {
    addExpectation(new SlimErrorTableExpectation(getInstructionTag()));
    return Collections.emptyList();
  }

  public class SlimErrorTableExpectation implements Expectation {

    private final String instructionTag;

    public SlimErrorTableExpectation(String instructionTag) {
      this.instructionTag = instructionTag;
    }

    @Override
    public void evaluateExpectation(Object returnValues) {
      String tableType = table.getCellContents(0, 0);
      Result errorMessage = fail(String.format("\"%s\" is not a valid table type.", tableType));
      table.setCell(0, 0, errorMessage);
    }

    @Override
    public String getInstructionTag() {
      return instructionTag;
    }

  }
}
