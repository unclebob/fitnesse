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
      failMessage(0, 0, "Table fixture has no valid doTable method");
      return;
    }

    List<List<Object>> tableResults = (List<List<Object>>) returnValues.get(doTableId);
    for (int row = 0; row < tableResults.size(); row++)
      evaluateRow(tableResults, row);
  }

  private void evaluateRow(List<List<Object>> tableResults, int row) {
    List<Object> rowList = tableResults.get(row);
    for (int col = 0; col < rowList.size(); col++) {
      String cellResult = (String) rowList.get(col);
      if (cellResult.equalsIgnoreCase("pass"))
        pass(col, row+1);
      else
        fail(col, row+1, cellResult);
    }
  }
}
