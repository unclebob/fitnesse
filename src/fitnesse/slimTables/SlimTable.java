// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slimTables;

import static java.lang.Character.isLetterOrDigit;
import static java.lang.Character.toUpperCase;
import static util.ListUtility.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.slimResponder.SlimTestContext;
import fitnesse.wikitext.Utils;

public abstract class SlimTable {
  protected Table table;
  private SlimTestContext testContext;
  protected String id;
  private String tableName;
  private int instructionNumber = 0;
  protected List<Object> instructions;
  protected static final Pattern symbolAssignmentPattern = Pattern.compile("\\A\\s*\\$(\\w+)\\s*=\\s*\\Z");
  private TestSummary testSummary = new TestSummary();
  private SlimTable parent = null;
  private List<SlimTable> children = new ArrayList<SlimTable>();

  public SlimTable(Table table, String id, SlimTestContext testContext) {
    this.id = id;
    this.table = table;
    this.testContext = testContext;
    tableName = getTableType() + "_" + id;
    instructions = new ArrayList<Object>();
  }

  public SlimTable getParent() {
    return parent;
  }

  public void addChildTable(SlimTable table, int row) throws Exception {
    table.id = id + "." + children.size();
    table.tableName = makeInstructionTag(instructionNumber)+"/"+table.tableName;
    instructionNumber++;
    table.parent = this;
    children.add(table);

    Table t = getTable();
    t.appendCellToRow(row, table.getTable());
  }

  public SlimTable getChild(int i) {
    return children.get(i);
  }

  protected void addExpectation(Expectation e) {
    testContext.addExpectation(e);
  }

  public String replaceSymbols(String s) {
    return new SymbolReplacer(s).replace();
  }

  public String replaceSymbolsWithFullExpansion(String s) {
    return new FullExpansionSymbolReplacer(s).replace();
  }


  protected abstract String getTableType();

  public void appendInstructions(List<Object> instructions) {
    try {
      this.instructions = instructions;
      appendInstructions();
    } catch (Throwable e) {
      String tableName = table.getCellContents(0, 0);
      table.setCell(0, 0, fail(String.format("%s: Bad table: <br/><pre>%s</pre>", tableName, Utils.getStackTrace(e))));
    }
  }

  public abstract void appendInstructions();

  protected List<Object> prepareInstruction() {
    List<Object> instruction = new ArrayList<Object>();
    instruction.add(makeInstructionTag(instructionNumber));
    instructionNumber++;
    return instruction;
  }

  protected String makeInstructionTag(int instructionNumber) {
    return String.format("%s_%d", tableName, instructionNumber);
  }

  protected String getInstructionTag() {
    return makeInstructionTag(instructionNumber);
  }

  public String getTableName() {
    return tableName;
  }

  protected void addInstruction(List<Object> instruction) {
    instructions.add(instruction);
  }

  public abstract void evaluateReturnValues(Map<String, Object> returnValues) throws Exception;

  public String getSymbol(String variableName) {
    return testContext.getSymbol(variableName);
  }

  public void setSymbol(String variableName, String value) {
    testContext.setSymbol(variableName, value);
  }

  public Table getTable() {
    return table;
  }

  protected void constructFixture() {
    String fixtureName = getFixtureName();
    constructFixture(fixtureName);
  }

  protected void constructFixture(String fixtureName) {
    constructInstance(getTableName(), fixtureName, 0, 0);
  }

  protected String getFixtureName() {
    String tableHeader = table.getCellContents(0, 0);
    String fixtureName = getFixtureName(tableHeader);
    String disgracedFixtureName = Disgracer.disgraceClassName(fixtureName);
    return disgracedFixtureName;
  }

  private String getFixtureName(String tableHeader) {
    if (tableHeader.indexOf(":") == -1)
      return tableHeader;
    return tableHeader.split(":")[1];
  }

  protected void constructInstance(String instanceName, String className, int classNameColumn, int row) {
    Expectation expectation = new ConstructionExpectation(getInstructionTag(), classNameColumn, row);
    addExpectation(expectation);
    List<Object> makeInstruction = prepareInstruction();
    makeInstruction.add("make");
    makeInstruction.add(instanceName);

    makeInstruction.add(className);
    addArgsToInstruction(makeInstruction, gatherConstructorArgumentsStartingAt(classNameColumn + 1, row));
    addInstruction(makeInstruction);
  }

  protected Object[] gatherConstructorArgumentsStartingAt(int startingColumn, int row) {
    int columnCount = table.getColumnCountInRow(row);
    List<String> arguments = new ArrayList<String>();
    for (int col = startingColumn; col < columnCount; col++) {
      arguments.add(table.getUnescapedCellContents(col, row));
      addExpectation(new VoidReturnExpectation(getInstructionTag(), col, row));
    }
    return arguments.toArray(new String[0]);
  }

  protected void addCall(List<Object> instruction, String instanceName, String functionName) {
    String disgracedFunctionName = Disgracer.disgraceMethodName(functionName);
    List<String> callHeader = list("call", instanceName, disgracedFunctionName);
    instruction.addAll(callHeader);
  }

  protected String callFunction(String instanceName, String functionName, Object... args) {
    List<Object> callInstruction = prepareInstruction();
    addCall(callInstruction, instanceName, functionName);
    addArgsToInstruction(callInstruction, args);
    addInstruction(callInstruction);
    return (String) callInstruction.get(0);
  }

  private void addArgsToInstruction(List<Object> instruction, Object... args) {
    for (Object arg : args)
      instruction.add(arg);
  }

  protected String callAndAssign(String symbolName, String instanceName, String functionName, String... args) {
    List<Object> callAndAssignInstruction = prepareInstruction();
    String disgracedFunctionName = Disgracer.disgraceMethodName(functionName);
    List<String> callAndAssignHeader = list("callAndAssign", symbolName, instanceName, disgracedFunctionName);
    callAndAssignInstruction.addAll(callAndAssignHeader);
    addArgsToInstruction(callAndAssignInstruction, (Object[]) args);
    addInstruction(callAndAssignInstruction);
    return (String) callAndAssignInstruction.get(0);
  }

  protected void failMessage(int col, int row, String failureMessage) {
    String contents = table.getCellContents(col, row);
    String failingContents = failMessage(contents, failureMessage);
    table.setCell(col, row, failingContents);
  }

  protected void fail(int col, int row, String value) {
    String failingContents = fail(value);
    table.setCell(col, row, failingContents);
  }

  protected void ignore(int col, int row, String value) {
    String content = ignore(value);
    table.setCell(col, row, content);
  }

  protected void pass(int col, int row) {
    String contents = table.getCellContents(col, row);
    String passingContents = pass(contents);
    table.setCell(col, row, passingContents);
  }

  protected void expected(int col, int tableRow, String actual) {
    String contents = table.getCellContents(col, tableRow);
    String failureMessage = failMessage(actual, String.format("expected [%s]", contents));
    table.setCell(col, tableRow, failureMessage);
  }

  protected String fail(String value) {
    testSummary.wrong++;
    return table.fail(value);
  }

  protected String failMessage(String value, String message) {
    return String.format("[%s] %s", value, fail(message));
  }

  protected String pass(String value) {
    testSummary.right++;
    return table.pass(value);
  }

  protected String error(String value) {
    testSummary.exceptions++;
    return table.error(value);
  }

  protected String ignore(String value) {
    return table.ignore(value);
  }

  protected ReturnedValueExpectation makeReturnedValueExpectation(
    String instructionTag, int col, int row) {
    return new ReturnedValueExpectation(instructionTag, col, row);
  }

  public static boolean approximatelyEqual(String standard, String candidate) {
    try {
      double candidateValue = Double.parseDouble(candidate);
      double standardValue = Double.parseDouble(standard);
      int point = standard.indexOf(".");
      int precision = 0;
      if (point != -1)
        precision = standard.length() - point - 1;
      double roundingFactor = 0.5;
      while (precision-- > 0)
        roundingFactor /= 10;
      return Math.abs(candidateValue - standardValue) <= roundingFactor;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public TestSummary getTestSummary() {
    return testSummary;
  }


  protected String extractExeptionMessage(String value) {
    return value.substring(2);
  }

  protected boolean isExceptionMessage(String value) {
    return value != null && value.startsWith("!:");
  }

  public boolean shouldIgnoreException(String resultKey, String resultString) {
    return false;
  }

  protected String ifSymbolAssignment(int row, int col) {
    String expected = table.getCellContents(col, row);
    Matcher matcher = symbolAssignmentPattern.matcher(expected);
    return matcher.find() ? matcher.group(1) : null;
  }

  protected void callAndAssign(String symbolName, String functionName) {
    List<Object> callAndAssignInstruction = prepareInstruction();
    callAndAssignInstruction.add("callAndAssign");
    callAndAssignInstruction.add(symbolName);
    callAndAssignInstruction.add(getTableName());
    callAndAssignInstruction.add(Disgracer.disgraceMethodName(functionName));
    addInstruction(callAndAssignInstruction);
  }

  public SlimTestContext getTestContext() {
    return testContext;
  }

  protected List<Object> tableAsList() {
    List<Object> tableArgument = list();
    int rows = table.getRowCount();
    for (int row = 1; row < rows; row++)
      tableArgument.add(tableRowAsList(row));
    return tableArgument;
  }

  private List<Object> tableRowAsList(int row) {
    List<Object> rowList = list();
    int cols = table.getColumnCountInRow(row);
    for (int col = 0; col < cols; col++)
      rowList.add(table.getCellContents(col, row));
    return rowList;
  }

  static class Disgracer {
    public boolean capitalizeNextWord;
    public StringBuffer disgracedName;
    private String name;

    public Disgracer(String name) {
      this.name = name;
    }

    public static String disgraceClassName(String name) {
      return new Disgracer(name).disgraceClassNameIfNecessary();
    }

    public static String disgraceMethodName(String name) {
      return new Disgracer(name).disgraceMethodNameIfNecessary();
    }

    private String disgraceMethodNameIfNecessary() {
      if (isGraceful()) {
        return disgraceMethodName();
      } else {
        return name;
      }
    }

    private String disgraceMethodName() {
      capitalizeNextWord = false;
      return disgraceName();
    }

    private String disgraceClassNameIfNecessary() {
      if (nameHasDotsBeforeEnd())
        return name;
      else if (isGraceful()) {
        return disgraceClassName();
      } else {
        return name;
      }
    }

    private String disgraceClassName() {
      capitalizeNextWord = true;
      return disgraceName();
    }

    private boolean nameHasDotsBeforeEnd() {
      int dotIndex = name.indexOf(".");
      return dotIndex != -1 && dotIndex != name.length() - 1;
    }

    private String disgraceName() {
      disgracedName = new StringBuffer();
      for (char c : name.toCharArray())
        appendCharInProperCase(c);

      return disgracedName.toString();
    }

    private void appendCharInProperCase(char c) {
      if (isGraceful(c)) {
        capitalizeNextWord = true;
      } else {
        appendProperlyCapitalized(c);
      }
    }

    private void appendProperlyCapitalized(char c) {
      disgracedName.append(capitalizeNextWord ? toUpperCase(c) : c);
      capitalizeNextWord = false;
    }

    private boolean isGraceful() {
      boolean isGraceful = false;
      for (char c : name.toCharArray()) {
        if (isGraceful(c))
          isGraceful = true;
      }
      return isGraceful;
    }

    private boolean isGraceful(char c) {
      return !(isLetterOrDigit(c) || c == '_');
    }
  }

  public abstract class Expectation {
    private int col;
    private int row;
    private String instructionTag;
    private String actual;
    private String expected;
    private String evaluationMessage;

    public Expectation(String instructionTag, int col, int row) {
      this.row = row;
      this.instructionTag = instructionTag;
      this.col = col;
    }

    public void evaluateExpectation(Map<String, Object> returnValues) {
      Object returnValue = returnValues.get(instructionTag);
      String value;
      if (returnValue == null)
        value = "null";
      else
        value = returnValue.toString();
      String originalContent = table.getCellContents(col, row);
      String evaluationMessage;
      evaluationMessage = evaluationMessage(value, originalContent);
      if (evaluationMessage != null)
        table.setCell(col, row, evaluationMessage);
    }

    String evaluationMessage(String actual, String expected) {
      this.actual = actual;
      this.expected = expected;
      String evaluationMessage;
      if (isExceptionMessage(actual))
        evaluationMessage = expected + " " + error(extractExeptionMessage(actual));
      else
        evaluationMessage = createEvaluationMessage(actual, expected);
      this.evaluationMessage = HtmlTable.colorize(evaluationMessage);
      return evaluationMessage;
    }

    protected abstract String createEvaluationMessage(String actual, String expected);

    public int getCol() {
      return col;
    }

    public int getRow() {
      return row;
    }

    public String getInstructionTag() {
      return instructionTag;
    }

    public String getActual() {
      return actual;
    }

    public String getExpected() {
      return expected;
    }

    public String getEvaluationMessage() {
      return evaluationMessage == null ? "" : evaluationMessage;
    }
  }

  class SymbolReplacer {
    protected String stringToReplace;

    SymbolReplacer(String s) {
      this.stringToReplace = s;
    }

    String replace() {
      Pattern symbolPattern = Pattern.compile("\\$([a-zA-Z]\\w*)");
      int startingPosition = 0;
      while (true) {
        Matcher symbolMatcher = symbolPattern.matcher(stringToReplace.substring(startingPosition));
        if (symbolMatcher.find()) {
          startingPosition += replaceSymbol(symbolMatcher);
        } else
          break;
      }
      return stringToReplace;
    }

    private int replaceSymbol(Matcher symbolMatcher) {
      String symbolName = symbolMatcher.group(1);
      if (getSymbol(symbolName) != null)
        stringToReplace = stringToReplace.replace("$" + symbolName, translate(symbolName));
      return symbolMatcher.start(1);
    }

    protected String translate(String symbolName) {
      return getSymbol(symbolName);
    }

  }

  class FullExpansionSymbolReplacer extends SymbolReplacer {
    FullExpansionSymbolReplacer(String s) {
      super(s);
    }

    protected String translate(String symbolName) {
      return String.format("$%s->[%s]", symbolName, getSymbol(symbolName));
    }
  }

  public static class SyntaxError extends Error {
    private static final long serialVersionUID = 1L;

    public SyntaxError(String message) {
      super(message);
    }
  }

  class VoidReturnExpectation extends Expectation {
    public VoidReturnExpectation(String instructionTag, int col, int row) {
      super(instructionTag, col, row);
    }

    protected String createEvaluationMessage(String actual, String expected) {
      return replaceSymbolsWithFullExpansion(expected);
    }
  }

  class SilentReturnExpectation extends Expectation {
    public SilentReturnExpectation(String instructionTag, int col, int row) {
      super(instructionTag, col, row);
    }

    protected String createEvaluationMessage(String actual, String expected) {
      return null;
    }
  }

  class ConstructionExpectation extends Expectation {
    public ConstructionExpectation(String instructionTag, int col, int row) {
      super(instructionTag, col, row);
    }

    protected String createEvaluationMessage(String actual, String expected) {
      if ("OK".equalsIgnoreCase(actual))
        return pass(expected);
      else
        return "!style_error(Unknown construction message:) " + actual;
    }
  }

  class SymbolAssignmentExpectation extends Expectation {
    private String symbolName;

    SymbolAssignmentExpectation(String symbolName, String instructionTag, int col, int row) {
      super(instructionTag, col, row);
      this.symbolName = symbolName;
    }

    protected String createEvaluationMessage(String actual, String expected) {
      setSymbol(symbolName, actual);
      return String.format("$%s<-[%s]", symbolName, actual);
    }
  }


  class ReturnedValueExpectation extends Expectation {
    public ReturnedValueExpectation(String instructionTag, int col, int row) {
      super(instructionTag, col, row);
    }

    protected String createEvaluationMessage(String actual, String expected) {
      String evaluationMessage;
      String replacedExpected = Utils.unescapeHTML(replaceSymbols(expected));
      if (actual == null)
        evaluationMessage = fail("null"); //todo can't be right message.
      else if (actual.equals(replacedExpected))
        evaluationMessage = pass(announceBlank(replaceSymbolsWithFullExpansion(expected)));
      else if (replacedExpected.length() == 0)
        evaluationMessage = ignore(actual);
      else {
        String expressionMessage = new Comparator(this, replacedExpected, actual, expected).evaluate();
        if (expressionMessage != null)
          evaluationMessage = expressionMessage;
        else if (actual.indexOf("Exception:") != -1) {
          evaluationMessage = error(actual);
        } else
          evaluationMessage = failMessage(actual,
            String.format("expected [%s]", replaceSymbolsWithFullExpansion(expected))
          );
      }

      return evaluationMessage;                                     
    }

    private String announceBlank(String originalValue) {
      return originalValue.length() == 0 ? "BLANK" : originalValue;
    }

    protected String pass(String message) {
      return SlimTable.this.pass(message);
    }

    protected String fail(String message) {
      return SlimTable.this.fail(message);
    }

    protected String failMessage(String value, String message) {
      return String.format("[%s] %s", value, fail(message));
    }
  }

  class RejectedValueExpectation extends ReturnedValueExpectation {
    public RejectedValueExpectation(String instructionTag, int col, int row) {
      super(instructionTag, col, row);
    }

    protected String pass(String message) {
      return super.fail(message);
    }

    protected String fail(String message) {
      return super.pass(message);
    }
  }

  class Comparator {
    private String expression;
    private String actual;
    private String expected;
    private Pattern simpleComparison = Pattern.compile(
      "\\A\\s*_?\\s*(!?(?:(?:[<>]=?)|(?:[~]?=)))\\s*(\\d*\\.?\\d+)\\s*\\Z"
    );
    private Pattern range = Pattern.compile(
      "\\A\\s*(\\d*\\.?\\d+)\\s*<(=?)\\s*_\\s*<(=?)\\s*(\\d*\\.?\\d+)\\s*\\Z"
    );
    private double v;
    private double arg1;
    private double arg2;
    public String operation;
    private String arg1Text;
    private ReturnedValueExpectation returnedValueExpectation;

    private Comparator(ReturnedValueExpectation returnedValueExpectation, String expression, String actual, String expected) {
      this.returnedValueExpectation = returnedValueExpectation;
      this.expression = expression;
      this.actual = actual;
      this.expected = expected;
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
      boolean pass = (arg1 < v && v < arg2) || (closedLeft && arg1 == v) || (closedRight && arg2 == v);
      return rangeMessage(pass);
    }

    private String rangeMessage(boolean pass) {
      String[] fragments = expected.replaceAll(" ", "").split("_");
      String message = String.format("%s%s%s", fragments[0], actual, fragments[1]);
      message = replaceSymbolsWithFullExpansion(message);
      return pass ? returnedValueExpectation.pass(message) : returnedValueExpectation.fail(message);

    }

    private boolean canUnpackRange(Matcher matcher) {
      try {
        arg1 = Double.parseDouble(matcher.group(1));
        arg2 = Double.parseDouble(matcher.group(4));
        v = Double.parseDouble(actual);
      } catch (NumberFormatException e) {
        return false;
      }
      return true;
    }

    private String doSimpleComparison() {
      if (operation.equals("<") || operation.equals("!>="))
        return simpleComparisonMessage(v < arg1);
      else if (operation.equals(">") || operation.equals("!<="))
        return simpleComparisonMessage(v > arg1);
      else if (operation.equals(">=") || operation.equals("!<"))
        return simpleComparisonMessage(v >= arg1);
      else if (operation.equals("<=") || operation.equals("!>"))
        return simpleComparisonMessage(v <= arg1);
      else if (operation.equals("!="))
        return simpleComparisonMessage(v != arg1);
      else if (operation.equals("="))
        return simpleComparisonMessage(v == arg1);
      else if (operation.equals("~="))
        return simpleComparisonMessage(approximatelyEqual(arg1Text, actual));
      else if (operation.equals("!~="))
        return simpleComparisonMessage(!approximatelyEqual(arg1Text, actual));
      else
        return null;
    }

    private String simpleComparisonMessage(boolean pass) {
      String message = String.format("%s%s", actual, expected.replaceAll(" ", ""));
      message = replaceSymbolsWithFullExpansion(message);
      return pass ? returnedValueExpectation.pass(message) : returnedValueExpectation.fail(message);

    }

    private String matchSimpleComparison() {
      Matcher matcher = simpleComparison.matcher(expression);
      if (matcher.matches()) {
        try {
          v = Double.parseDouble(actual);
          arg1Text = matcher.group(2);
          arg1 = Double.parseDouble(arg1Text);
          return matcher.group(1);
        } catch (NumberFormatException e1) {
          return null;
        }
      }
      return null;
    }
  }
}
