package fitnesse.responders.run.slimResponder;

import static fitnesse.responders.run.slimResponder.SlimTable.Disgracer.disgraceMethodName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DecisionTable extends SlimTable {
  private static final String instancePrefix = "decisionTable";
  private Map<String, Integer> vars;
  private Map<String, Integer> funcs;
  private int headerColumns;

  public DecisionTable(Table table, String id) {
    super(table, id);
    vars = new HashMap<String, Integer>();
    funcs = new HashMap<String, Integer>();
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
    Expectation expectation = new VoidReturnExpectation(getInstructionNumber(), 0, 0);
    addExpectation(expectation);
    List<Object> makeInstruction = prepareInstruction();
    makeInstruction.add("make");
    makeInstruction.add(getTableName());
    String tableHeader = table.getCellContents(0, 0);
    String fixtureName = tableHeader.split(":")[1];
    String disgracedFixtureName = Disgracer.disgraceClassName(fixtureName);
    makeInstruction.add(disgracedFixtureName);
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
        String.format("Table has %d header column(s), but row %d only has %d column(s).",
          headerColumns, row, columns
        )
      );
  }

  private void addCall(List<Object> instruction, String functionName) {
    instruction.add("call");
    instruction.add(getTableName());
    instruction.add(disgraceMethodName(functionName));
  }

  private void callFunctions(int row) {
    Set<String> funcKeys = funcs.keySet();
    for (String func : funcKeys) {
      int col = funcs.get(func);
      setFunctionCallExpectation(col, row);
      callFunction(func);
    }
  }

  private void callFunction(String func) {
    List<Object> callInstruction = prepareInstruction();
    addCall(callInstruction, func);
    addInstruction(callInstruction);
  }

  private void setFunctionCallExpectation(int col, int row) {
    String expectedValue = table.getCellContents(col, row);
    ReturnedValueExpectation expectation = new ReturnedValueExpectation(expectedValue, getInstructionNumber(), col, row
    );
    addExpectation(expectation);
  }

  private void setVariables(int row) {
    Set<String> varKeys = vars.keySet();
    for (String var : varKeys) {
      int col = vars.get(var);
      setVariableExpectation(col, row);
      List<Object> setInstruction = prepareInstruction();
      addCall(setInstruction, "set" + " " + var);
      setInstruction.add(table.getCellContents(col, row));
      addInstruction(setInstruction);
    }
  }

  private void setVariableExpectation(int col, int row) {
    addExpectation(new VoidReturnExpectation(getInstructionNumber(), col, row));
  }

  public Table getTable() {
    return table;
  }

  public static class SyntaxError extends Error {
    public SyntaxError(String message) {
      super(message);
    }
  }

  static class ReturnedValueExpectation extends Expectation {
    public ReturnedValueExpectation(String expected, int instructionNumber, int col, int row) {
      super(expected, instructionNumber, col, row);
    }

    protected String createEvaluationMessage(String value, String literalizedValue, String originalValue) {
      String evaluationMessage;
      if (value.equals(expectedValue))
        evaluationMessage = String.format("!style_pass(%s)", announceBlank(originalValue));
      else if (expectedValue.length() == 0)
        evaluationMessage = String.format("!style_ignore(%s)", literalizedValue);
      else {
        String expressionMessage = Comparator.evaluate(expectedValue, value);
        if (expressionMessage != null)
          evaluationMessage = expressionMessage;
        else
          evaluationMessage = String.format("!style_fail(<%s> expected <%s>)", literalizedValue, originalValue);
      }

      return evaluationMessage;
    }

    private String announceBlank(String originalValue) {
      return originalValue.length() == 0 ? "BLANK" : originalValue;
    }

    static class Comparator {
      private String expression;
      private String value;
      private static Pattern simpleComparison = Pattern.compile("\\A\\s*_?\\s*((?:[<>]=?)|(?:!=))\\s*(\\d*\\.?\\d+)\\s*\\Z");
      private static Pattern range = Pattern.compile(
        "\\A\\s*(\\d*\\.?\\d+)\\s*<(=?)\\s*_\\s*<(=?)\\s*(\\d*\\.?\\d+)\\s*\\Z"
      );
      private double v;
      private double e1;
      private double e2;
      public String operation;

      static String evaluate(String expression, String value) {
        Comparator comparator = new Comparator(expression, value);
        return comparator.evaluate();
      }

      private Comparator(String expression, String value) {
        this.expression = expression;
        this.value = value;
      }

      private String evaluate() {
        operation = matchSimpleComparison();
        if (operation != null)
          return doSimpleComparison();

        Matcher matcher = range.matcher(expression);
        if (matcher.matches() && canUnpackRange(matcher)) {
          return doRange(matcher);
        } else
          return null;
      }

      private String doRange(Matcher matcher) {
        boolean closedLeft = matcher.group(2).equals("=");
        boolean closedRight = matcher.group(3).equals("=");
        boolean pass = (e1 < v && v < e2) || (closedLeft && e1 == v) || (closedRight && e2 == v);
        return rangeMessage(closedLeft, closedRight, pass);
      }

      private String rangeMessage(boolean closedLeft, boolean closedRight, boolean pass) {
        return String.format("!style_%s(%s<%s%s<%s%s)",
          pass ? "pass" : "fail",
          e1,
          closedLeft ? "=" : "",
          v,
          closedRight ? "=" : "",
          e2
        );
      }

      private boolean canUnpackRange(Matcher matcher) {
        try {
          e1 = Double.parseDouble(matcher.group(1));
          e2 = Double.parseDouble(matcher.group(4));
          v = Double.parseDouble(value);
        } catch (NumberFormatException e) {
          return false;
        }
        return true;
      }

      private String doSimpleComparison
        () {
        if (operation.equals("<"))
          return simpleComparisonMessage(v < e1);
        else if (operation.equals(">"))
          return simpleComparisonMessage(v > e1);
        else if (operation.equals(">="))
          return simpleComparisonMessage(v >= e1);
        else if (operation.equals("<="))
          return simpleComparisonMessage(v <= e1);
        else if (operation.equals("!="))
          return simpleComparisonMessage(v != e1);
        else
          return null;
      }

      private String simpleComparisonMessage
        (
          boolean pass
        ) {
        return String.format("!style_%s(%s%s%s)", pass ? "pass" : "fail", v, operation, e1);
      }

      private String matchSimpleComparison
        () {
        Matcher matcher = simpleComparison.matcher(expression);
        if (matcher.matches()) {
          try {
            v = Double.parseDouble(value);
            e1 = Double.parseDouble(matcher.group(2));
            return matcher.group(1);
          } catch (NumberFormatException e1) {
            return null;
          }
        }
        return null;
      }
    }
  }

  static class VoidReturnExpectation extends Expectation {
    public VoidReturnExpectation(int instructionNumber, int col, int row) {
      super(null, instructionNumber, col, row);
    }

    protected String createEvaluationMessage(String value, String literalizedValue, String originalValue) {
      if (value.indexOf("Exception") != -1)
        return String.format("!style_fail(%s)", value);
      else
        return originalValue;
    }
  }
}
