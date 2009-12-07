// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slimTables;

import static fitnesse.responders.run.slimResponder.SlimTestSystem.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fitnesse.responders.run.slimResponder.SlimTestContext;

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

  public void evaluateReturnValues(Map<String, Object> returnValues) throws Exception {
    if (doTableId == null || returnValues.get(doTableId) == null) {
      table.appendToCell(0, 0, error("Table fixture has no valid doTable method"));
      return;
    }

    Object tableReturn = returnValues.get(doTableId);
    if (tableReturn instanceof String) {
      String value = (String) tableReturn;
      if (isTestCaseErrorMessage(value)) {
        table.appendToCell(0, 0, error("Table fixture has no valid doTable method"));
      } else if (isExceptionFailureMessage(value)) {
        table.appendToCell(0, 0, error(value));
      }
      return;
    }

    resizeTableAndEvaluateRows(returnValues);
  }

  private boolean isExceptionFailureMessage(String value) {
    return value.startsWith("Exception: ");
  }

  private boolean isTestCaseErrorMessage(String value) {
    return value.startsWith(MESSAGE_ERROR);
  }

  @SuppressWarnings("unchecked")
  private void resizeTableAndEvaluateRows(Map<String, Object> returnValues) throws Exception {
    List<List<Object>> tableResults = (List<List<Object>>) returnValues.get(doTableId);
    extendTable(table, tableResults);
    for (int row = 0; row < tableResults.size(); row++)
      evaluateRow(tableResults, row);
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

  private void extendRow(Table table, int row, List<Object> cellList) throws Exception {
    while (table.getColumnCountInRow(row) < cellList.size())
      table.appendCellToRow(row, (String) cellList.get(table.getColumnCountInRow(row)));
  }

  private void evaluateRow(List<List<Object>> tableResults, int resultRow) {
    List<Object> rowList = tableResults.get(resultRow);
    for (int col = 0; col < rowList.size(); col++) {
      int tableRow = resultRow + 1;
      String contents = table.getCellContents(col, tableRow);
      String replacedContents = replaceSymbolsWithFullExpansion(contents);
      table.setCell(col, tableRow, replacedContents);
      String result = (String) rowList.get(col);
      colorCell(col, tableRow, result);
    }
  }

  private void colorCell(int col, int row, String result) {
    if (result.equalsIgnoreCase("no change") || result.length() == 0)
      return; // do nothing
    else if (result.equalsIgnoreCase("pass"))
      pass(col, row);
    else if (result.equalsIgnoreCase("fail"))
      fail(col, row, table.getCellContents(col, row));
    else if (result.equalsIgnoreCase("ignore"))
      ignore(col, row, table.getCellContents(col, row));
    else if (!colorCellWithMessage(col, row, result))
      fail(col, row, result);
  }

  private boolean colorCellWithMessage(int col, int row, String contents) {
    int colon = contents.indexOf(":");
    if (colon == -1)
      return false;
    String code = contents.substring(0, colon);
    String message = contents.substring(colon + 1);

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
