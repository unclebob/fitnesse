package fitnesse.testsystems.slim.tables;

import java.util.ArrayList;
import java.util.List;

import fitnesse.slim.instructions.CallInstruction;
import fitnesse.slim.instructions.Instruction;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;

public class DynamicDecisionTable extends SlimTable {
  private static final String TABLE_TYPE = "dynamicDecisionTable";

  public DynamicDecisionTable(Table table, String id, SlimTestContext testContext) {
    super(table, id, testContext);
  }

  @Override
  protected String getTableType() {
    return TABLE_TYPE;
  }

  @Override
  public List<SlimAssertion> getAssertions() throws SyntaxError {
    if (table.getRowCount() == 2)
      throw new SyntaxError("DynamicDecisionTables should have at least three rows.");
    return new FixtureCaller().call(getFixtureName());
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
      assertions.addAll(callSetForVariables(row));
      assertions.add(callUnreportedFunction("execute", row));
      assertions.addAll(callGetForFunctions(row));
      return assertions;
    }

    private SlimAssertion callUnreportedFunction(String functionName, int row) {
      return makeAssertion(callFunction(getTableName(), functionName),
              new SilentReturnExpectation(0, row));
    }

    private List<SlimAssertion> callGetForFunctions(int row) {
      List<SlimAssertion> instructions = new ArrayList<SlimAssertion>();
      for (String functionName : funcStore.getLeftToRightAndResetColumnNumberIterator()) {
        instructions.add(callGetForFunctionInRow(functionName, row));
      }
      return instructions;
    }

    private SlimAssertion callGetForFunctionInRow(String functionName, int row) {
      int col = funcStore.getColumnNumber(functionName);
      String assignedSymbol = ifSymbolAssignment(col, row);
      SlimAssertion assertion;
      if (assignedSymbol != null) {
        assertion = makeAssertion(callAndAssign(assignedSymbol, getTableName(), "get", functionName),
                new SymbolAssignmentExpectation(assignedSymbol, col, row));
      } else {
        assertion = makeAssertion(callFunction(getTableName(), "get", functionName),
                new ReturnedValueExpectation(col, row));
      }
      return assertion;
    }

    private List<SlimAssertion> callSetForVariables(int row) {
      List<SlimAssertion> assertions = new ArrayList<SlimAssertion>();
      for (String var : varStore.getLeftToRightAndResetColumnNumberIterator()) {
        int col = varStore.getColumnNumber(var);
        String valueToSet = table.getCellContents(col, row);
        Instruction setInstruction = new CallInstruction(makeInstructionTag(), getTableName(), "set", new Object[] {var, valueToSet});
        assertions.add(makeAssertion(setInstruction, new VoidReturnExpectation(col, row)));
      }
      return assertions;
    }
  }
  
}
