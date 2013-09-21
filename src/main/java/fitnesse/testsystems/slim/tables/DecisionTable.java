// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fitnesse.slim.instructions.CallInstruction;
import fitnesse.slim.instructions.Instruction;
import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;

public class DecisionTable extends SlimTable {
  private static final String instancePrefix = "decisionTable";

  public DecisionTable(Table table, String id, SlimTestContext context) {
    super(table, id, context);
  }

  protected String getTableType() {
    return instancePrefix;
  }

  public List<SlimAssertion> getAssertions() throws SyntaxError {
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
    private class ColumnHeaderStore {
      private Map<String, List<Integer>> columnNumbers = new HashMap<String, List<Integer>>();
      private Map<String, Iterator<Integer>> columnNumberIterator;
      private List<String> leftToRight = new ArrayList<String>();

      public void add(String header, int columnNumber) {
        leftToRight.add(header);
        getColumnNumbers(header).add(columnNumber);
      }

      private List<Integer> getColumnNumbers(String header) {
        if (!columnNumbers.containsKey(header)) {
          columnNumbers.put(header, new ArrayList<Integer>());
        }
        return columnNumbers.get(header);
      }

      public int getColumnNumber(String functionName) {
        return columnNumberIterator.get(functionName).next();
      }

      public List<String> getLeftToRightAndResetColumnNumberIterator() {
        resetColumnNumberIterator();
        return leftToRight;
      }

      private void resetColumnNumberIterator() {
        columnNumberIterator = new HashMap<String, Iterator<Integer>>();
        for (String header : columnNumbers.keySet()) {
          columnNumberIterator.put(header, columnNumbers.get(header).iterator());
        }
      }
    }

    protected ColumnHeaderStore varStore = new ColumnHeaderStore();
    protected ColumnHeaderStore funcStore = new ColumnHeaderStore();
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
        funcStore.add(funcName, col);
      } else {
        varStore.add(cell, col);
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
    public ArrayList<SlimAssertion> call(ScenarioTable scenario) throws SyntaxError {
      gatherFunctionsAndVariablesFromColumnHeader();
      ArrayList<SlimAssertion> assertions = new ArrayList<SlimAssertion>();
      for (int row = 2; row < table.getRowCount(); row++)
        assertions.addAll(callScenarioForRow(scenario, row));
      return assertions;
    }

    private List<SlimAssertion> callScenarioForRow(ScenarioTable scenario, int row) throws SyntaxError {
      checkRow(row);
      return scenario.call(getArgumentsForRow(row), DecisionTable.this, row);
    }

    private Map<String, String> getArgumentsForRow(int row) {
      Map<String, String> scenarioArguments = new HashMap<String, String>();
      for (String var : varStore.getLeftToRightAndResetColumnNumberIterator()) {
        String disgracedVar = Disgracer.disgraceMethodName(var);
        int col = varStore.getColumnNumber(var);
        String valueToSet = table.getCellContents(col, row);
        scenarioArguments.put(disgracedVar, valueToSet);
      }
      return scenarioArguments;
    }
  }

  private class FixtureCaller extends DecisionTableCaller {
    public List<SlimAssertion> call(String fixtureName) throws SyntaxError {
      final List<SlimAssertion> assertions = new ArrayList<SlimAssertion>();
      assertions.add(constructFixture(fixtureName));
      assertions.add(makeAssertion(
              callFunction(getTableName(), "table", tableAsList()),
              new SilentReturnExpectation(0, 0)));
      if (table.getRowCount() > 2)
        assertions.addAll(invokeRows());
      return assertions;
    }

    private List<SlimAssertion> invokeRows() throws SyntaxError {
      List<SlimAssertion> assertions = new ArrayList<SlimAssertion>();
      assertions.add(callUnreportedFunction("beginTable", 0));
      gatherFunctionsAndVariablesFromColumnHeader();
      for (int row = 2; row < table.getRowCount(); row++)
        assertions.addAll(invokeRow(row));
      assertions.add(callUnreportedFunction("endTable", 0));
      return assertions;
    }

    private List<SlimAssertion> invokeRow(int row) throws SyntaxError {
      List<SlimAssertion> assertions = new ArrayList<SlimAssertion>();
      checkRow(row);
      assertions.add(callUnreportedFunction("reset", row));
      assertions.addAll(setVariables(row));
      assertions.add(callUnreportedFunction("execute", row));
      assertions.addAll(callFunctions(row));
      return assertions;
    }

    private SlimAssertion callUnreportedFunction(String functionName, int row) {
      return makeAssertion(callFunction(getTableName(), functionName),
              new SilentReturnExpectation(0, row));
    }

    private List<SlimAssertion> callFunctions(int row) {
      List<SlimAssertion> instructions = new ArrayList<SlimAssertion>();
      for (String functionName : funcStore.getLeftToRightAndResetColumnNumberIterator()) {
        instructions.add(callFunctionInRow(functionName, row));
      }
      return instructions;
    }

    private SlimAssertion callFunctionInRow(String functionName, int row) {
      int col = funcStore.getColumnNumber(functionName);
      String assignedSymbol = ifSymbolAssignment(col, row);
      SlimAssertion assertion;
      if (assignedSymbol != null) {
        assertion = makeAssertion(callAndAssign(assignedSymbol, functionName),
                new SymbolAssignmentExpectation(assignedSymbol, col, row));
      } else {
        assertion = makeAssertion(callFunction(getTableName(), functionName),
                new ReturnedValueExpectation(col, row));
      }
      return assertion;
    }

    private List<SlimAssertion> setVariables(int row) {
      List<SlimAssertion> assertions = new ArrayList<SlimAssertion>();
      for (String var : varStore.getLeftToRightAndResetColumnNumberIterator()) {
        int col = varStore.getColumnNumber(var);
        String valueToSet = table.getCellContents(col, row);
        Instruction setInstruction = new CallInstruction(makeInstructionTag(), getTableName(), Disgracer.disgraceMethodName("set " + var), new Object[] {valueToSet});
        assertions.add(makeAssertion(setInstruction,
                new VoidReturnExpectation(col, row)));
      }
      return assertions;
    }
  }
}
