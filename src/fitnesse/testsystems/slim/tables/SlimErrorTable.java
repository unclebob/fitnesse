// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import java.util.Collections;
import java.util.List;

import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.results.SlimTestResult;

public class SlimErrorTable extends SlimTable {
  public SlimErrorTable(Table table, String id, SlimTestContext testContext) {
    super(table, id, testContext);
  }

  protected String getTableType() {
    return "UnknownTableType";
  }

  public List<SlimAssertion> getAssertions() {
	// No need for Expectations, this is just an errorous table. Put a notification in.
    String tableType = table.getCellContents(0, 0);
    SlimTestResult errorMessage = SlimTestResult.error(String.format("\"%s\" is not a valid table type.", tableType));
    table.updateContent(0, 0, errorMessage);
    getTestContext().incrementErroredTestsCount();
    return Collections.emptyList();
  }
}
