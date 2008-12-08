package fitnesse.responders.run.slimResponder;

import java.util.*;

public class DecisionTable extends SlimTable {
  private static final String instancePrefix = "decisionTable";
  private Map<String, Integer> vars = new HashMap<String, Integer>();
  private Map<String, Integer> funcs = new HashMap<String, Integer>();
  private int headerColumns;
  private Set<String> executeInstructions = new HashSet<String>();

  public DecisionTable(Table table, String id) {
    super(table, id);
  }

  public DecisionTable(Table table, String id, SlimTestContext context) {
    super(table, id, context);
  }

  protected String getTableType() {
    return instancePrefix;
  }

  public void appendInstructions() {
    if (table.getRowCount() == 2)
      throw new SyntaxError("DecisionTables should have at least three rows.");
    constructFixture();
    if (table.getRowCount() > 2)
      invokeRows();
  }

  protected void evaluateReturnValues(Map<String, Object> returnValues) {
  }

  private void invokeRows() {
    parseColumnHeader();
    for (int row = 2; row < table.getRowCount(); row++)
      invokeRow(row);
  }

  private void parseColumnHeader() {
    headerColumns = table.getColumnCountInRow(1);
    for (int col = 0; col < headerColumns; col++) {
      String cell = table.getCellContents(col, 1);
      if (cell.endsWith("?"))
        funcs.put(cell.substring(0, cell.length() - 1), col);
      else
        vars.put(cell, col);
    }
  }

  public boolean shouldIgnoreException(String resultKey, String resultString) {
    boolean isExecuteInstruction = executeInstructions.contains(resultKey);
    boolean isNoSuchMethodException = resultString.indexOf("NO_METHOD_IN_CLASS") != -1;
    return isExecuteInstruction && isNoSuchMethodException;
  }

  private void invokeRow(int row) {
    checkRow(row);
    setVariables(row);
    callExecute(row);
    callFunctions(row);
  }

  private void callExecute(int row) {
    executeInstructions.add(callFunction(getTableName(), "execute"));
  }

  private void checkRow(int row) {
    int columns = table.getColumnCountInRow(row);
    if (columns < headerColumns)
      throw new SyntaxError(
        String.format("Table has %d header column(s), but row %d only has %d column(s).",
          headerColumns, row, columns
        )
      );
  }

  private void callFunctions(int row) {
    Set<String> funcKeys = funcs.keySet();
    for (String functionName : funcKeys) {
      callFunctionInRow(functionName, row);
    }
  }

  private void callFunctionInRow(String functionName, int row) {
    int col = funcs.get(functionName);
    String assignedSymbol = ifSymbolAssignment(row, col);
    if (assignedSymbol != null) {
      addExpectation(new SymbolAssignmentExpectation(assignedSymbol, getInstructionNumber(), col, row));
      callAndAssign(assignedSymbol, functionName);
    } else {
      setFunctionCallExpectation(col, row);
      callFunction(getTableName(), functionName);
    }
  }

  private void setFunctionCallExpectation(int col, int row) {
    String expectedValue = table.getCellContents(col, row);
    addExpectation(new ReturnedValueExpectation(expectedValue, getInstructionNumber(), col, row));
  }

  private void setVariables(int row) {
    Set<String> varKeys = vars.keySet();
    for (String var : varKeys) {
      int col = vars.get(var);
      String valueToSet = table.getCellContents(col, row);
      setVariableExpectation(col, row);
      List<Object> setInstruction = prepareInstruction();
      addCall(setInstruction, getTableName(), "set" + " " + var);
      setInstruction.add(valueToSet);
      addInstruction(setInstruction);
    }
  }

  private void setVariableExpectation(int col, int row) {
    addExpectation(new VoidReturnExpectation(getInstructionNumber(), col, row));
  }
}
