// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import fitnesse.slim.instructions.CallInstruction;
import fitnesse.slim.instructions.Instruction;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DecisionTable extends SlimTable {
  private static final String instancePrefix = "decisionTable";

  public DecisionTable(Table table, String id, SlimTestContext context) {
    super(table, id, context);
  }

  protected String getTableType() {
    return instancePrefix;
  }

  public List<Assertion> getAssertions() throws SyntaxError {
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

  protected Instruction callAndAssign(String symbolName, String functionName) {
    return callAndAssign(symbolName, getTableName(), functionName);
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
    public ArrayList<Assertion> call(ScenarioTable scenario) throws SyntaxError {
      gatherFunctionsAndVariablesFromColumnHeader();
      ArrayList<Assertion> assertions = new ArrayList<Assertion>();
      for (int row = 2; row < table.getRowCount(); row++)
        assertions.addAll(callScenarioForRow(scenario, row));
      return assertions;
    }

    private List<Assertion> callScenarioForRow(ScenarioTable scenario, int row) throws SyntaxError {
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
    public List<Assertion> call(String fixtureName) throws SyntaxError {
      final List<Assertion> assertions = new ArrayList<Assertion>();
      assertions.add(constructFixture(fixtureName));
      assertions.add(makeAssertion(
              callFunction(getTableName(), "table", tableAsList()),
              new SilentReturnExpectation(0, 0)));
      if (table.getRowCount() > 2)
        assertions.addAll(invokeRows());
      return assertions;
    }

    private List<Assertion> invokeRows() throws SyntaxError {
      List<Assertion> assertions = new ArrayList<Assertion>();
      assertions.add(callUnreportedFunction("beginTable", 0));
      gatherFunctionsAndVariablesFromColumnHeader();
      for (int row = 2; row < table.getRowCount(); row++)
        assertions.addAll(invokeRow(row));
      assertions.add(callUnreportedFunction("endTable", 0));
      return assertions;
    }

    private List<Assertion> invokeRow(int row) throws SyntaxError {
      List<Assertion> assertions = new ArrayList<Assertion>();
      checkRow(row);
      assertions.add(callUnreportedFunction("reset", row));
      assertions.addAll(setVariables(row));
      assertions.add(callUnreportedFunction("execute", row));
      assertions.addAll(callFunctions(row));
      return assertions;
    }

    private Assertion callUnreportedFunction(String functionName, int row) {
      return makeAssertion(callFunction(getTableName(), functionName),
              new SilentReturnExpectation(0, row));
    }

    private List<Assertion> callFunctions(int row) {
      List<Assertion> instructions = new ArrayList<Assertion>();
      for (String functionName : funcsLeftToRight) {
        instructions.add(callFunctionInRow(functionName, row));
      }
      return instructions;
    }

    private Assertion callFunctionInRow(String functionName, int row) {
      int col = funcs.get(functionName);
      String assignedSymbol = ifSymbolAssignment(col, row);
      Assertion assertion;
      if (assignedSymbol != null) {
        assertion = makeAssertion(callAndAssign(assignedSymbol, functionName),
                new SymbolAssignmentExpectation(assignedSymbol, col, row));
      } else {
        assertion = makeAssertion(callFunction(getTableName(), functionName),
                new ReturnedValueExpectation(col, row));
      }
      return assertion;
    }

    private List<Assertion> setVariables(int row) {
      List<Assertion> assertions = new ArrayList<Assertion>();
      for (String var : varsLeftToRight) {
        int col = vars.get(var);
        String valueToSet = table.getUnescapedCellContents(col, row);
        Instruction setInstruction = new CallInstruction(makeInstructionTag(), getTableName(), Disgracer.disgraceMethodName("set " + var), new Object[] {valueToSet});
        assertions.add(makeAssertion(setInstruction,
                new VoidReturnExpectation(col, row)));
      }
      return assertions;
    }
  }
}
