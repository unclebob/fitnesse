package fitnesse.responders.run.slimResponder;

import java.util.*;

public class DecisionTable {
  private static final String instancePrefix = "decisionTable";
  private String instanceName;
  private Table table;
  private String id;
  private List<Object> instructions;
  private Map<String, Integer> vars;
  private Map<String, Integer> funcs;
  private int headerColumns;
  private int instructionNumber = 0;
  List<Expectation> expectations = new ArrayList<Expectation>();
  public boolean isLiteralTable;

  public DecisionTable(Table table, String id) {
    this.table = table;
    this.id = id;
    vars = new HashMap<String, Integer>();
    funcs = new HashMap<String, Integer>();
    instanceName = instancePrefix + "_" + id;
    isLiteralTable = table.isLiteralTable();
  }

  public List<Object> appendInstructionsTo(List<Object> instructions) {
    if (table.getRowCount() < 3)
      throw new SyntaxError("DecisionTables should have at least three rows.");
    this.instructions = instructions;
    constructFixture();
    invokeRows();
    return instructions;
  }

  private void constructFixture() {
    List<Object> makeInstruction = prepareInstruction();
    makeInstruction.add("make");
    makeInstruction.add(instanceName);
    int columnCount = table.getColumnCountInRow(0);
    for (int col = 0; col < columnCount; col++)
      makeInstruction.add(table.getCellContents(col, 0));
    instructions.add(makeInstruction);
  }

  private List<Object> prepareInstruction() {
    List<Object> instruction = new ArrayList<Object>();
    instruction.add(makeInstructionTag(instructionNumber));
    instructionNumber++;
    return instruction;
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

  private void invokeRow(int row) {
    checkRow(row);
    setVariables(row);
    callFunction("execute");
    callFunctions(row);
  }

  private void checkRow(int row) {
    int columns = table.getColumnCountInRow(row);
    if (columns < headerColumns)
      throw new SyntaxError(
        String.format("Table has %d header columns, but row %d only has %d columns.",
          headerColumns, row, columns
        )
      );
  }

  private void addCall(List<Object> instruction, String functionName) {
    instruction.add("call");
    instruction.add(instanceName);
    instruction.add(functionName);
  }

  private String makeInstructionTag(int instructionNumber) {
    return String.format("%s_%d", instanceName, instructionNumber);
  }

  private void callFunctions(int row) {
    Set<String> funcKeys = funcs.keySet();
    for (String func : funcKeys) {
      setExpectation(row, func);
      callFunction(func);
    }
  }

  private void callFunction(String func) {
    List<Object> callInstruction = prepareInstruction();
    addCall(callInstruction, func);
    instructions.add(callInstruction);
  }

  private void setExpectation(int row, String func) {
    int col = funcs.get(func);
    String expectedValue = table.getCellContents(col, row);
    Expectation expectation = new Expectation(expectedValue, instructionNumber, col, row);
    expectations.add(expectation);
  }

  private void setVariables(int row) {
    Set<String> varKeys = vars.keySet();
    for (String var : varKeys) {
      List<Object> setInstruction = prepareInstruction();
      addCall(setInstruction, "set" + var);
      setInstruction.add(table.getCellContents(vars.get(var), row));
      instructions.add(setInstruction);
    }
  }

  public void evaluateExpectations(Map<String, Object> returnValues) {
    literalizeTable();
    for (Expectation expectation : expectations)
      evaluateExpectation(expectation, returnValues);
  }

  private void literalizeTable() {
    if (isLiteralTable) {
      table.setAsNotLiteralTable();
      for (int row = 0; row < table.getRowCount(); row++) {
        for (int col = 0; col < table.getColumnCountInRow(row); col++) {
          table.setCell(col, row, literalize(table.getCellContents(col, row)));
        }
      }
    }
  }

  private String literalize(String contents) {
    return isLiteralTable ? String.format("!-%s-!", contents) : contents;
  }

  private void evaluateExpectation(Expectation expectation, Map<String, Object> returnValues) {
    String value = (String) returnValues.get(makeInstructionTag(expectation.instructionNumber));
    String expectedValue = expectation.expectedValue;
    String evaluationMessage = createEvaluationMessage(value, expectedValue);

    table.setCell(expectation.col, expectation.row, evaluationMessage);
  }

  private String createEvaluationMessage(String value, String expectedValue) {
    String resultString;
    if (value.equals(expectedValue))
      resultString = String.format("!style_pass(%s)", literalize(value));
    else {
      resultString = String.format("!style_fail(<%s> expected <%s>)", literalize(value), literalize(expectedValue));
    }
    return resultString;
  }

  public Table getTable() {
    return table;
  }

  public static class SyntaxError extends Error {
    public SyntaxError(String message) {
      super(message);
    }
  }

  private static class Expectation {
    public String expectedValue;
    public int col;
    public int row;
    public int instructionNumber;

    public Expectation(String expected, int instructionNumber, int col, int row) {
      this.instructionNumber = instructionNumber;
      this.expectedValue = expected;
      this.col = col;
      this.row = row;
    }
  }
}
