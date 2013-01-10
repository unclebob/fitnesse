// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slimTables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fitnesse.testsystems.slim.SlimTestContext;

public class DecisionTable extends SlimTable {
  private static final String instancePrefix = "decisionTable";
  private Set<String> dontReportExceptionsInTheseInstructions = new HashSet<String>();

  public DecisionTable(Table table, String id, SlimTestContext context) {
    super(table, id, context);
  }

  protected String getTableType() {
    return instancePrefix;
  }

  public List<Object> getInstructions() throws SyntaxError {
    if (table.getRowCount() == 2)
      throw new SyntaxError("DecisionTables should have at least three rows.");
    String scenarioName = getScenarioName();
    ScenarioTable scenario = getTestContext().getScenario(scenarioName);
    if (scenario != null) {
      return new ScenarioCaller().call(scenario);
    } else {
      return new FixtureCaller().call(getFixtureName());
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

  protected List<Object> callAndAssign(String symbolName, String functionName) {
    return callAndAssign(symbolName, getTableName(), functionName);
  }

  public void evaluateReturnValues(Map<String, Object> returnValues) {
  }

  public boolean shouldIgnoreException(String resultKey, String resultString) {
    boolean shouldNotReport = dontReportExceptionsInTheseInstructions.contains(resultKey);
    boolean isNoSuchMethodException = resultString.contains("NO_METHOD_IN_CLASS");
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
      if (cell.endsWith("?") || cell.endsWith("!")) {
        String funcName = cell.substring(0, cell.length() - 1);
        funcsLeftToRight.add(funcName);
        funcs.put(funcName, col);
      } else {
        varsLeftToRight.add(cell);
        vars.put(cell, col);
      }
    }

    protected void checkRow(int row) throws SyntaxError {
      int columns = table.getColumnCountInRow(row);
      if (columns < columnHeaders)
        throw new SyntaxError(
          String.format("Table has %d header column%s, but row %d only has %d column%s.",
            columnHeaders, plural(columnHeaders), row, columns, plural(columns)));
    }

    private String plural(int n) {
      return n == 1 ? "" : "s";
    }
  }

  private class ScenarioCaller extends DecisionTableCaller {
    public ArrayList<Object> call(ScenarioTable scenario) throws SyntaxError {
      gatherFunctionsAndVariablesFromColumnHeader();
      ArrayList<Object> instructions = new ArrayList<Object>();
      for (int row = 2; row < table.getRowCount(); row++)
        instructions.addAll(callScenarioForRow(scenario, row));
      return instructions;
    }

    private List<Object> callScenarioForRow(ScenarioTable scenario, int row) throws SyntaxError {
      checkRow(row);
      return scenario.call(getArgumentsForRow(row), DecisionTable.this, row);
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
    public List<Object> call(String fixtureName) throws SyntaxError {
      final List<Object> instructions = new ArrayList<Object>();
      instructions.add(constructFixture(fixtureName));
      final List<Object> callTable = callFunction(getTableName(), "table", tableAsList());
      instructions.add(callTable);
      dontReportExceptionsInTheseInstructions.add(getInstructionId(callTable));
      if (table.getRowCount() > 2)
        instructions.addAll(invokeRows());
      return instructions;
    }

    private List<Object> invokeRows() throws SyntaxError {
      List<Object> instructions = new ArrayList<Object>();
      instructions.add(callUnreportedFunction("beginTable"));
      gatherFunctionsAndVariablesFromColumnHeader();
      for (int row = 2; row < table.getRowCount(); row++)
        instructions.addAll(invokeRow(row));
      instructions.add(callUnreportedFunction("endTable"));
      return instructions;
    }

    private List<Object> invokeRow(int row) throws SyntaxError {
      List<Object> instructions = new ArrayList<Object>();
      checkRow(row);
      instructions.add(callUnreportedFunction("reset"));
      instructions.addAll(setVariables(row));
      instructions.add(callUnreportedFunction("execute"));
      instructions.addAll(callFunctions(row));
      return instructions;
    }

    private List<Object> callUnreportedFunction(String functionName) {
      final List<Object> functionCall = callFunction(getTableName(), functionName);
      dontReportExceptionsInTheseInstructions.add(getInstructionId(functionCall));
      return functionCall;
    }

    private List<Object> callFunctions(int row) {
      List<Object> instructions = new ArrayList<Object>();
      for (String functionName : funcsLeftToRight) {
        instructions.add(callFunctionInRow(functionName, row));
      }
      return instructions;
    }

    private List<Object> callFunctionInRow(String functionName, int row) {
      int col = funcs.get(functionName);
      String assignedSymbol = ifSymbolAssignment(col, row);
      List<Object> instruction;
      if (assignedSymbol != null) {
        addExpectation(new SymbolAssignmentExpectation(assignedSymbol, getInstructionTag(), col, row));
        instruction = callAndAssign(assignedSymbol, functionName);
      } else {
        setFunctionCallExpectation(col, row);
        instruction = callFunction(getTableName(), functionName);
      }
      return instruction;
    }

    private void setFunctionCallExpectation(int col, int row) {
      table.getCellContents(col, row);
      addExpectation(new ReturnedValueExpectation(getInstructionTag(), col, row));
    }

    private List<Object> setVariables(int row) {
      List<Object> instructions = new ArrayList<Object>();
      for (String var : varsLeftToRight) {
        int col = vars.get(var);
        String valueToSet = table.getUnescapedCellContents(col, row);
        setVariableExpectation(col, row);
        List<Object> setInstruction = prepareInstruction();
        addCall(setInstruction, getTableName(), "set" + " " + var);
        setInstruction.add(valueToSet);
        instructions.add(setInstruction);
      }
      return instructions;
    }

    private void setVariableExpectation(int col, int row) {
      addExpectation(new VoidReturnExpectation(getInstructionTag(), col, row));
    }
  }
}
