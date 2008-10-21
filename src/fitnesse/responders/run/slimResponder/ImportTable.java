package fitnesse.responders.run.slimResponder;

import static fitnesse.util.ListUtility.list;

import java.util.List;
import java.util.Map;

public class ImportTable extends SlimTable {
  public ImportTable(Table table, String tableId) {
    super(table, tableId);
  }

  protected String getTableType() {
    return "import";
  }

  public void appendInstructions() {
    int rows = table.getRowCount();
    if (rows < 2)
      throw new SlimTable.SyntaxError("Import tables must have at least two rows.");

    for (int row = 1; row < rows; row++) {
      String importString = table.getCellContents(0, row);
      if (importString.length() > 0) {
        List<Object> importInstruction = prepareInstruction();
        importInstruction.addAll(list("import", importString));
        addInstruction(importInstruction);
      }
    }

  }

  protected void evaluateReturnValues(Map<String, Object> returnValues) {
  }

}
