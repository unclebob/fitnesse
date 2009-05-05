// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slimTables;

import fitnesse.responders.run.slimResponder.SlimTestContext;

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
    String scenarioName = getScenarioName();
    ScenarioTable scenario = getTestContext().getScenario(scenarioName);
    if (scenario != null) {
      new ScenarioCaller().call(scenario);
    } else {
      new FixtureCaller().call(getFixtureName());
    }
  }

  private String getScenarioName() {
    StringBuffer nameBuffer = new StringBuffer();
    for (int nameCol = 0; nameCol < table.getColumnCountInRow(0); nameCol += 2) {
      if (nameCol == 0)
        nameBuffer.append(getFixtureName(table.getCellContents(nameCol, 0)));
      else
        nameBuffer.append(table.getCellContents(nameCol, 0));
      nameBuffer.append(" ");
    }
    return Disgracer.disgraceClassName(nameBuffer.toString().trim());
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
    protected List<String> varsLeftToRight = new ArrayList<String>();
    protected List<String> funcsLeftToRight = new ArrayList<String>();
    protected int columnHeaders;

    protected void gatherFunctionsAndVariablesFromColumnHeader() {
      columnHeaders = table.getColumnCountInRow(1);
      for (int col = 0; col < columnHeaders; col++)
        putColumnHeaderInFunctionOrVariableList(col);
    }

    private void putColumnHeaderInFunctionOrVariableList(int col) {
      String cell = table.getCellContents(col, 1);
      if (cell.endsWith("?")) {
        String funcName = cell.substring(0, cell.length() - 1);
        funcsLeftToRight.add(funcName);
        funcs.put(funcName, col);
      } else {
        varsLeftToRight.add(cell);
        vars.put(cell, col);
      }
    }

    protected void checkRow(int row) {
      int columns = table.getColumnCountInRow(row);
      if (columns < columnHeaders)
        throw new SyntaxError(
          String.format("Table has %d header column(s), but row %d only has %d column(s).",
            columnHeaders, row, columns
          )
        );
    }
  }

  private class ScenarioCaller extends DecisionTableCaller {
    public void call(ScenarioTable scenario) {
      gatherFunctionsAndVariablesFromColumnHeader();
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
      dontReportExceptionsInTheseInstructions.add(callFunction(getTableName(), "table", tableAsList()));
      if (table.getRowCount() > 2)
        invokeRows();
    }

    private void invokeRows() {
      gatherFunctionsAndVariablesFromColumnHeader();
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
      for (String functionName : funcsLeftToRight) {
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
      table.getCellContents(col, row);
      addExpectation(new ReturnedValueExpectation(getInstructionTag(), col, row));
    }

    private void setVariables(int row) {
      for (String var : varsLeftToRight) {
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
