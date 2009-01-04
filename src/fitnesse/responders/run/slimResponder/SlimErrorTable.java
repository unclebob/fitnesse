package fitnesse.responders.run.slimResponder;

import java.util.Map;

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
