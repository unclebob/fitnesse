package fitnesse.responders.run.slimResponder;

import java.util.*;

public class DecisionTable extends SlimTable {
  private static final String instancePrefix = "decisionTable";
  private Set<String> dontReportExceptionsInTheseInstructions = new HashSet<String>();

  public DecisionTable(Table table, String id, SlimTestContext context) {
    super(table, id, context);
  }

  protected String getTableType() {
    return instancePrefix;
  }

  public void appendInstructions() {
    if (table.getRowCount() == 2)
      throw new SyntaxError("DecisionTables should have at least three rows.");
    String fixtureName = getFixtureName();
    ScenarioTable scenario = getTestContext().getScenario(fixtureName);
    if (scenario != null) {
      new ScenarioCaller().call(scenario);
    } else {
      new FixtureCaller().call(fixtureName);
    }
  }

  public void evaluateReturnValues(Map<String, Object> returnValues) {
  }

  public boolean shouldIgnoreException(String resultKey, String resultString) {
    boolean shouldNotReport = dontReportExceptionsInTheseInstructions.contains(resultKey);
    boolean isNoSuchMethodException = resultString.indexOf("NO_METHOD_IN_CLASS") != -1;
    return shouldNotReport && isNoSuchMethodException;
  }

  private class DecisionTableCaller {
    protected Map<String, Integer> vars = new HashMap<String, Integer>();
    protected Map<String, Integer> funcs = new HashMap<String, Integer>();
    protected int headerColumns;

    protected void parseColumnHeader() {
      headerColumns = table.getColumnCountInRow(1);
      for (int col = 0; col < headerColumns; col++) {
        String cell = table.getCellContents(col, 1);
        if (cell.endsWith("?"))
          funcs.put(cell.substring(0, cell.length() - 1), col);
        else
          vars.put(cell, col);
      }
    }

    protected void checkRow(int row) {
      int columns = table.getColumnCountInRow(row);
      if (columns < headerColumns)
        throw new SyntaxError(
          String.format("Table has %d header column(s), but row %d only has %d column(s).",
            headerColumns, row, columns
          )
        );
    }
  }

  private class ScenarioCaller extends DecisionTableCaller {
    public void call(ScenarioTable scenario) {
      parseColumnHeader();
      for (int row = 2; row < table.getRowCount(); row++)
        callScenarioForRow(scenario, row);
    }

    private void callScenarioForRow(ScenarioTable scenario, int row) {
      checkRow(row);
      scenario.call(getArgumentsForRow(row), DecisionTable.this, row);
    }

    private Map<String, String> getArgumentsForRow(int row) {
      Map<String, String> scenarioArguments = new HashMap<String, String>();
      for (String var : vars.keySet()) {
        String disgracedVar = Disgracer.disgraceMethodName(var);
        int col = vars.get(var);
        String valueToSet = table.getUnescapedCellContents(col, row);
        scenarioArguments.put(disgracedVar, valueToSet);
      }
      return scenarioArguments;
    }
  }

  private class FixtureCaller extends DecisionTableCaller {
    public void call(String fixtureName) {
      constructFixture(fixtureName);
      if (table.getRowCount() > 2)
        invokeRows();
    }

    private void invokeRows() {
      parseColumnHeader();
      for (int row = 2; row < table.getRowCount(); row++)
        invokeRow(row);
    }

    private void invokeRow(int row) {
      checkRow(row);
      callUnreportedFunction("reset");
      setVariables(row);
      callUnreportedFunction("execute");
      callFunctions(row);
    }

    private void callUnreportedFunction(String functionName) {
      dontReportExceptionsInTheseInstructions.add(callFunction(getTableName(), functionName));
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
        addExpectation(new SymbolAssignmentExpectation(assignedSymbol, getInstructionTag(), col, row));
        callAndAssign(assignedSymbol, functionName);
      } else {
        setFunctionCallExpectation(col, row);
        callFunction(getTableName(), functionName);
      }
    }

    private void setFunctionCallExpectation(int col, int row) {
      String expectedValue = table.getCellContents(col, row);
      addExpectation(new ReturnedValueExpectation(expectedValue, getInstructionTag(), col, row));
    }

    private void setVariables(int row) {
      Set<String> varKeys = vars.keySet();
      for (String var : varKeys) {
        int col = vars.get(var);
        String valueToSet = table.getUnescapedCellContents(col, row);
        setVariableExpectation(col, row);
        List<Object> setInstruction = prepareInstruction();
        addCall(setInstruction, getTableName(), "set" + " " + var);
        setInstruction.add(valueToSet);
        addInstruction(setInstruction);
      }
    }

    private void setVariableExpectation(int col, int row) {
      addExpectation(new VoidReturnExpectation(getInstructionTag(), col, row));
    }
  }
}
