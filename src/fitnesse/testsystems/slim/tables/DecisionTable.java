// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fitnesse.slim.instructions.CallInstruction;
import fitnesse.slim.instructions.Instruction;
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

  private class ScenarioCaller extends DecisionTableCaller {
    public ScenarioCaller() {
      super(table);
    }

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
    public FixtureCaller() {
      super(table);
    }

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
