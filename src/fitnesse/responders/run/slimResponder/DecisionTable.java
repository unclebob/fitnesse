package fitnesse.responders.run.slimResponder;

import java.util.*;

public class DecisionTable extends SlimTable {
  private static final String instancePrefix = "decisionTable";
  private Map<String, Integer> vars;
  private Map<String, Integer> funcs;
  private int headerColumns;
  List<Expectation> expectations = new ArrayList<Expectation>();
  public boolean isLiteralTable;

  public DecisionTable(Table table, String id) {
    super(table, id);
    vars = new HashMap<String, Integer>();
    funcs = new HashMap<String, Integer>();
    isLiteralTable = table.isLiteralTable();
  }

  protected String getTableType() {
    return instancePrefix;
  }

  public void appendInstructions() {
    if (table.getRowCount() < 3)
      throw new SyntaxError("DecisionTables should have at least three rows.");
    constructFixture();
    invokeRows();
  }

  private void constructFixture() {
    Expectation expectation = new Expectation("OK", getInstructionNumber(), 0,0);
    expectations.add(expectation);
    List<Object> makeInstruction = prepareInstruction();
    makeInstruction.add("make");
    makeInstruction.add(getTableName());
    String tableHeader = table.getCellContents(0,0);
    String fixtureName = tableHeader.split(":")[1];
    makeInstruction.add(fixtureName);
    int columnCount = table.getColumnCountInRow(0);
    for (int col = 1; col < columnCount; col++)
      makeInstruction.add(table.getCellContents(col, 0));
    addInstruction(makeInstruction);
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
    instruction.add(getTableName());
    instruction.add(functionName);
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
    addInstruction(callInstruction);
  }

  private void setExpectation(int row, String func) {
    int col = funcs.get(func);
    String expectedValue = table.getCellContents(col, row);
    Expectation expectation = new Expectation(expectedValue, getInstructionNumber(), col, row);
    expectations.add(expectation);
  }

  private void setVariables(int row) {
    Set<String> varKeys = vars.keySet();
    for (String var : varKeys) {
      List<Object> setInstruction = prepareInstruction();
      addCall(setInstruction, "set" + var);
      setInstruction.add(table.getCellContents(vars.get(var), row));
      addInstruction(setInstruction);
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
    String originalContent = table.getCellContents(expectation.col, expectation.row);
    String evaluationMessage = createEvaluationMessage(value, expectedValue, originalContent);
    table.setCell(expectation.col, expectation.row, evaluationMessage);
  }

  private String createEvaluationMessage(String value, String expectedValue, String originalContent) {
    String resultString;
    if (value.equals(expectedValue)) {
      resultString = String.format("!style_pass(%s)", originalContent);
    }
    else {
      resultString = String.format("!style_fail(<%s> expected <%s>)", literalize(value), originalContent);
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
