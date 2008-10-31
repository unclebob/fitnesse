package fitnesse.responders.run.slimResponder;

import static fitnesse.util.ListUtility.list;

import java.util.List;
import java.util.Map;

public class TableTable extends SlimTable {
  private String doTableId;

  public TableTable(Table table, String id) {
    super(table, id);
  }

  public TableTable(Table table, String tableId, SlimTestContext slimTestContext) {
    super(table, tableId, slimTestContext);
  }

  protected String getTableType() {
    return ("tableTable");
  }

  public void appendInstructions() {
    if (table.getRowCount() < 2)
      throw new SlimTable.SyntaxError("Table tables must have at least two rows.");
    constructFixture();
    doTableId = callFunction(getTableName(), "doTable", tableAsList());
  }

  private List<Object> tableAsList() {
    List<Object> tableArgument = list();
    int rows = table.getRowCount();
    for (int row = 1; row < rows; row++)
      tableArgument.add(tableRowAsList(row));
    return tableArgument;
  }

  private List<Object> tableRowAsList(int row) {
    List<Object> rowList = list();
    int cols = table.getColumnCountInRow(row);
    for (int col = 0; col < cols; col++)
      rowList.add(table.getCellContents(col, row));
    return rowList;
  }

  protected void evaluateReturnValues(Map<String, Object> returnValues) throws Exception {
    Object tableReturn = returnValues.get(doTableId);
    if (doTableId == null || tableReturn == null || (tableReturn instanceof String)) {
      table.appendToCell(0, 0, error("Table fixture has no valid doTable method"));
      return;
    }

    List<List<Object>> tableResults = (List<List<Object>>) returnValues.get(doTableId);
    for (int row = 0; row < tableResults.size(); row++)
      evaluateRow(tableResults, row);
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
    else {
      String[] resultTokens = contents.split(":");
      if (resultTokens[0].equalsIgnoreCase("error")) {
        table.setCell(col, row, error(resultTokens[1]));
      } else
        fail(col, row, contents);
    }
  }
}
