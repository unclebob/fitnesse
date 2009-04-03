// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slimTables;

import fitnesse.responders.run.slimResponder.SlimTestContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TableTable extends SlimTable {
  private String doTableId;

  public TableTable(Table table, String tableId, SlimTestContext slimTestContext) {
    super(table, tableId, slimTestContext);
  }

  protected String getTableType() {
    return ("tableTable");
  }

  public void appendInstructions() {
    constructFixture();
    doTableId = callFunction(getTableName(), "doTable", tableAsList());
  }

  public void evaluateReturnValues(Map<String, Object> returnValues)
    throws Exception {
    if (doTableMethodIsInvalid(returnValues)) {
      table.appendToCell(0, 0, error("Table fixture has no valid doTable method"));
      return;
    }
    resizeTableAndEvaluateRows(returnValues);
  }

  @SuppressWarnings("unchecked")
  private void resizeTableAndEvaluateRows(Map<String, Object> returnValues) throws Exception {
    List<List<Object>> tableResults = (List<List<Object>>) returnValues.get(doTableId);
    extendTable(table, tableResults);
    for (int row = 0; row < tableResults.size(); row++)
      evaluateRow(tableResults, row);
  }

  private boolean doTableMethodIsInvalid(Map<String, Object> returnValues) {
    if (doTableId == null)
      return true;
    else {
      Object tableReturn = returnValues.get(doTableId);
      return tableReturn == null || (tableReturn instanceof String);
    }
  }

  private void extendTable(Table table, List<List<Object>> tableResults) throws Exception {
    addNewRows(table, tableResults);
    extendExistingRows(table, tableResults);
  }

  private void addNewRows(Table table, List<List<Object>> tableResults) throws Exception {
    while (table.getRowCount() - 1 < tableResults.size()) {
      List<String> l = new ArrayList<String>();
      for (Object s : tableResults.get(table.getRowCount() - 1))
        l.add((String) s);
      table.addRow(l);
    }
  }

  private void extendExistingRows(Table table, List<List<Object>> tableResults) throws Exception {
    for (int row = 1; row < tableResults.size(); row++)
      extendRow(table, row, tableResults.get(row - 1));
  }

  private void extendRow(Table table, int row, List<Object> cellList)
    throws Exception {
    while (table.getColumnCountInRow(row) < cellList.size())
      table.appendCellToRow(row,
        (String) cellList.get(table.getColumnCountInRow(row)));
  }

  private void evaluateRow(List<List<Object>> tableResults, int resultRow) {
    List<Object> rowList = tableResults.get(resultRow);
    for (int col = 0; col < rowList.size(); col++) {
      int tableRow = resultRow + 1;
      colorCell(col, tableRow, (String) rowList.get(col));
    }
  }

  private void colorCell(int col, int row, String contents) {
    if (contents.equalsIgnoreCase("no change") || contents.length() == 0)
      ; // do nothing
    else if (contents.equalsIgnoreCase("pass"))
      pass(col, row);
    else if (contents.equalsIgnoreCase("fail"))
      fail(col, row, table.getCellContents(col, row));
    else if (contents.equalsIgnoreCase("ignore"))
      ignore(col, row, table.getCellContents(col, row));
    else if (!colorCellWithMessage(col, row, contents))
      fail(col, row, contents);
  }

  private boolean colorCellWithMessage(int col, int row, String contents) {
    String[] tokens = contents.split(":");
    if (tokens.length != 2)
      return false;

    String code = tokens[0];
    String message = tokens[1];

    if (code.equalsIgnoreCase("error"))
      table.setCell(col, row, error(message));
    else if (code.equalsIgnoreCase("fail"))
      table.setCell(col, row, fail(message));
    else if (code.equalsIgnoreCase("pass"))
      table.setCell(col, row, pass(message));
    else if (code.equalsIgnoreCase("ignore"))
      table.setCell(col, row, ignore(message));
    else if (code.equalsIgnoreCase("report"))
      table.setCell(col, row, message);
    else
      return false;
    return true;
  }
}
