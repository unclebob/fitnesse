// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import fitnesse.slim.MethodExecutionResult;
import fitnesse.slim.SlimExpressionEvaluator;
import fitnesse.slim.SlimSymbol;
import fitnesse.slim.instructions.AssignInstruction;
import fitnesse.slim.instructions.CallAndAssignInstruction;
import fitnesse.slim.instructions.CallInstruction;
import fitnesse.slim.instructions.Instruction;
import fitnesse.slim.instructions.MakeInstruction;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TableCell;
import fitnesse.testsystems.TestExecutionException;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.slim.CustomComparator;
import fitnesse.testsystems.slim.CustomComparatorRegistry;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.results.SlimExceptionResult;
import fitnesse.testsystems.slim.results.SlimTestResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fitnesse.testsystems.slim.tables.ComparatorUtil.approximatelyEqual;

public abstract class SlimTable {

  private String tableName;
  private int instructionNumber = 0;
  private String fixtureName;

  private List<SlimTable> children = new LinkedList<>();
  private SlimTable parent = null;

  private final SlimTestContext testContext;

  protected final Table table;
  protected String id;
  protected CustomComparatorRegistry customComparatorRegistry;

  private final Map<String, String> symbolsToStore = new HashMap<>();

  public SlimTable(Table table, String id, SlimTestContext testContext) {
    this.id = id;
    this.table = table;
    this.testContext = testContext;
    tableName = getTableType() + "_" + id;
  }

  public SlimTable getParent() {
    return parent;
  }

  public void addChildTable(SlimTable slimtable, int row) {
    slimtable.id = id + "." + children.size();
    slimtable.tableName = makeInstructionTag() + "/" + slimtable.tableName;
    slimtable.parent = this;
    children.add(slimtable);

    Table parentTable = getTable();
    Table childTable = slimtable.getTable();
    parentTable.appendChildTable(row, childTable);
  }

  public String replaceSymbols(String s) {
    return new SymbolReplacer(s).replace();
  }

  public String replaceSymbolsWithFullExpansion(String s) {
    return new FullExpansionSymbolReplacer(s).replace();
  }

  protected abstract String getTableType();

  public abstract List<SlimAssertion> getAssertions() throws TestExecutionException;

  protected String makeInstructionTag() {
    return String.format("%s_%d", tableName, instructionNumber++);
  }

  public String getTableName() {
    return tableName;
  }

  public String getSymbol(String variableName) {
    return testContext.getSymbol(variableName);
  }

  public void setSymbol(String variableName, String value) {
    setSymbol(variableName, value, false);
  }

  public void setSymbol(String variableName, String value, boolean toStore) {
    testContext.setSymbol(variableName, value);
    if (toStore) {
      symbolsToStore.put(variableName, value);
    }
  }

  protected String getConfigurationVariable(String variableName,
      String defaultValue) {
    String value = this.getTestContext().getPageToTest()
        .getVariable(variableName);
    return (value == null || value.isEmpty()) ? defaultValue : value;
  }

  public Map<String, String> getSymbolsToStore() {
    return symbolsToStore;
  }

  public Table getTable() {
    return table;
  }

  protected SlimAssertion constructFixture(String fixtureName) {
    return constructInstance(getTableName(), fixtureName, 0, 0);
  }

  public void setFixtureName(String name) {
    fixtureName = name;
  }

  protected String getFixtureName() {
    return fixtureName != null ? Disgracer.disgraceClassName(fixtureName) : "";
  }

  public boolean isTearDown() {
    return table.isTearDown();
  }

  protected String getFixtureName(String tableHeader) {
    if (!tableHeader.contains(":"))
      return tableHeader;
    return tableHeader.split(":")[1];
  }

  protected SlimAssertion constructInstance(String instanceName, String className, int classNameColumn, int row) {
    RowExpectation expectation = new ConstructionExpectation(classNameColumn, row);
    return makeAssertion(new MakeInstruction(makeInstructionTag(), instanceName, className, gatherConstructorArgumentsStartingAt(classNameColumn + 1, row)),
      expectation);
  }

  protected final SlimAssertion makeAssertion(Instruction instruction, SlimExpectation expectation) {
    return new SlimAssertion(instruction, expectation);
  }

  protected Object[] gatherConstructorArgumentsStartingAt(int startingColumn, int row) {
    int columnCount = table.getColumnCountInRow(row);
    List<String> arguments = new ArrayList<>();
    for (int col = startingColumn; col < columnCount; col++) {
      arguments.add(table.getCellContents(col, row));
    }
    return arguments.toArray(new String[arguments.size()]);
  }

  protected Instruction callFunction(String instanceName, String functionName, Object... args) {
    return new CallInstruction(makeInstructionTag(), instanceName, Disgracer.disgraceMethodName(functionName), args);
  }

  protected Instruction callAndAssign(String symbolName, String instanceName, String functionName, Object... args) {
    return new CallAndAssignInstruction(makeInstructionTag(), symbolName, instanceName, Disgracer.disgraceMethodName(functionName), args);
  }

  protected Instruction assign(String symbolName, String value) {
    return new AssignInstruction(makeInstructionTag(), symbolName, value);
  }

  protected String isSymbolAssignment(int col, int row) {
    String expected = table.getCellContents(col, row);
    return isSymbolAssignment(expected);
  }

  protected String isSymbolAssignment(String expected) {
    return SlimSymbol.isSymbolAssignment(expected);
  }

  public SlimTestContext getTestContext() {
    return testContext;
  }

  protected List<List<String>> tableAsList() {
    List<List<String>> tableArgument = new ArrayList<>();
    int rows = table.getRowCount();
    for (int row = 1; row < rows; row++)
      tableArgument.add(tableRowAsList(row));
    return tableArgument;
  }

  private List<String> tableRowAsList(int row) {
    List<String> rowList = new ArrayList<>();
    int cols = table.getColumnCountInRow(row);
    for (int col = 0; col < cols; col++)
      rowList.add(table.getCellContents(col, row));
    return rowList;
  }

  public List<SlimTable> getChildren() {
    return children;
  }

  public void setCustomComparatorRegistry(CustomComparatorRegistry customComparatorRegistry) {
    this.customComparatorRegistry = customComparatorRegistry;
  }

  private String getSlimExpressionResult(String variableNameWithDollar) {
    SlimExpressionEvaluator evaluator = new SlimExpressionEvaluator();
    String expr = variableNameWithDollar.substring(2, variableNameWithDollar.length() - 1);
    Map<String, MethodExecutionResult> symbols = new HashMap<>();

    for (Map.Entry<String, String> symbol : testContext.getSymbols().entrySet()) {
      if (symbol.getValue().startsWith("!{")) {
        symbol.setValue(HtmlValueOfSymbol(symbol.getValue()));
      }
      symbols.put(symbol.getKey(), new MethodExecutionResult(symbol.getValue(), Object.class));
    }
    evaluator.setContext(expr, symbols);

    Object value;
    try {
      value = evaluator.evaluate(expr);
    } catch (IllegalArgumentException e) {
      value = e.getMessage();
    }
    return String.valueOf(value);
  }

  private String HtmlValueOfSymbol(String symbol) {
    return new WikiSymbolTranslateUtil().getHtmlFor(symbol);
  }

  /**
   * SlimExpectation base class for row based expectations.
   */
  public abstract class RowExpectation implements SlimExpectation, TableCell {
    private final int col;
    private final int row;
    private final String originalContent;

    public RowExpectation(int col, int row) {
      this(col, row, col >= 0 ? table.getCellContents(col, row) : null);
    }

    public RowExpectation(int col, int row, String originalContent) {
      this.row = row;
      this.col = col;
      this.originalContent = originalContent;
    }

    @Override
    public TestResult evaluateExpectation(Object returnValue) {
      SlimTestResult testResult;
      if (returnValue == null) {
        testResult = SlimTestResult.testNotRun();
      } else {
        String value;
        value = returnValue.toString();
        testResult = evaluationMessage(value, originalContent);
      }
      if (testResult != null) {
        table.updateContent(col, row, testResult);
        if (testResult.doesCount())
          getTestContext().increment(testResult.getExecutionResult());
      }
      return testResult;
    }

    SlimTestResult evaluationMessage(String actual, String expected) {
      return createEvaluationMessage(actual, expected);
    }

    protected abstract SlimTestResult createEvaluationMessage(String actual, String expected);

    @Override
    public SlimExceptionResult evaluateException(SlimExceptionResult exceptionResult) {
      table.updateContent(col, row, exceptionResult);
      getTestContext().incrementErroredTestsCount();
      return exceptionResult;
    }

    @Override
    public int getCol() {
      return col;
    }

    @Override
    public int getRow() {
      return row;
    }

    // Used only by TestXmlFormatter.SlimTestXmlFormatter
    public String getExpected() {
      return originalContent;
    }
  }

  class SymbolReplacer extends SlimSymbol {
    private String toReplace;

    public SymbolReplacer(String s) {
      super();
      toReplace = s;
    }

    //TODO: This is only implemented in the SlimServer but not in the Slim Client so it can't work properly :(
    // Should be removed. Would this breaks other SLIM Client implementations .Net ... ?
    @Override
    protected String getSymbolValue(String symbolName) {
      if (symbolName.endsWith("`")) {
        String symbolNameWithDollar = symbolName.startsWith("$`") ? symbolName : "$" + symbolName;
        return getSlimExpressionResult(symbolNameWithDollar);
      }

      String value = getSymbol(symbolName);
      if (value == null) {
        for (int i = symbolName.length() - 1; i > 0; i--) {
          String str = symbolName.substring(0, i);
          if ((value = getSymbol(str)) != null)
            return value + symbolName.substring(i);
        }

        return null;
      } else
        return value;
    }

    public String replace() {
      return replace(toReplace);
    }
  }

  class FullExpansionSymbolReplacer extends SymbolReplacer {
    FullExpansionSymbolReplacer(String s) {
      super(s);
    }

    @Override
    protected String formatSymbolValue(String name, String value) {
      return String.format("$%s->[%s]", name, value);
    }
  }

  class VoidReturnExpectation extends RowExpectation {
    public VoidReturnExpectation(int col, int row) {
      super(col, row);
    }

    @Override
    protected SlimTestResult createEvaluationMessage(String actual, String expected) {
      table.substitute(getCol(), getRow(), replaceSymbolsWithFullExpansion(expected));
      return SlimTestResult.plain();
    }
  }

  class SilentReturnExpectation implements SlimExpectation {
    private final int col;
    private final int row;

    public SilentReturnExpectation(int col, int row) {
      this.col = col;
      this.row = row;
    }

    @Override
    public TestResult evaluateExpectation(Object returnValue) {
      return null;
    }

    @Override
    public SlimExceptionResult evaluateException(SlimExceptionResult exceptionResult) {
      if (exceptionResult.isNoMethodInClassException() || exceptionResult.isNoInstanceException()) {
        return null;
      }
      table.updateContent(-1, row, exceptionResult);
      getTestContext().incrementErroredTestsCount();
      return exceptionResult;
    }
  }

  class ConstructionExpectation extends RowExpectation {
    public ConstructionExpectation(int col, int row) {
      super(col, row);
    }

    @Override
    protected SlimTestResult createEvaluationMessage(String actual, String expected) {
      if ("OK".equalsIgnoreCase(actual))
        return SlimTestResult.ok(replaceSymbolsWithFullExpansion(expected));
      else
        return SlimTestResult.error("Unknown construction message", actual);
    }
  }

  class SymbolAssignmentExpectation extends RowExpectation {
    private String symbolName;

    SymbolAssignmentExpectation(String symbolName, int col, int row) {
      super(col, row);
      this.symbolName = symbolName;
    }

    @Override
    protected SlimTestResult createEvaluationMessage(String actual, String expected) {
      setSymbol(symbolName, actual);
      return SlimTestResult.plain(String.format("$%s<-[%s]", symbolName, actual));
    }
  }

  class ReturnedValueExpectation extends RowExpectation {
    public ReturnedValueExpectation(int col, int row) {
      super(col, row, table.getCellContents(col, row));
    }

    public ReturnedValueExpectation(int col, int row, String expected) {
      super(col, row, expected);
    }

    @Override
    protected SlimTestResult createEvaluationMessage(String actual, String expected) {
      SlimTestResult testResult;
      String replacedExpected = replaceSymbols(expected);

      if (actual == null)
        testResult = SlimTestResult.fail("null", replacedExpected); //todo can't be right message.
      else if (actual.equals(replacedExpected))
        testResult = SlimTestResult.pass(announceBlank(replaceSymbolsWithFullExpansion(expected)));
      else if (replacedExpected.isEmpty())
        testResult = SlimTestResult.ignore(actual);
      else {
        testResult = new Comparator(replacedExpected, actual, expected).evaluate();
        if (testResult == null)
          testResult = SlimTestResult.fail(actual, replaceSymbolsWithFullExpansion(expected));
      }

      return testResult;
    }

    private String announceBlank(String originalValue) {
      return originalValue.isEmpty() ? "BLANK" : originalValue;
    }

    @Override
    public SlimExceptionResult evaluateException(
        SlimExceptionResult exceptionResult) {
      final String exceptionComparatorPrefix = getConfigurationVariable(
          "SLIM_EXCEPTION_COMPARATOR",
          SlimExceptionResult.DEFAULT_SLIM_EXCEPTION_COMPARATOR);
      if (this.getExpected().startsWith(exceptionComparatorPrefix)) {
        TestResult testResult = new ReturnedValueExpectation(this.getCol(),
            this.getRow(), this.getExpected().replaceFirst(
                exceptionComparatorPrefix, ""))
            .evaluateExpectation(exceptionResult.getException());
        exceptionResult.setCatchException(testResult);
      } else {
        exceptionResult = super.evaluateException(exceptionResult);
      }
      return exceptionResult;
    }
  }
  class SilentAssignExpectation implements SlimExpectation {
    private final String symbolName;

    public SilentAssignExpectation( String symbolName) {
      this.symbolName = symbolName;
    }

    @Override
    public TestResult evaluateExpectation(Object returnValue) {
      setSymbol(symbolName, returnValue == null ? null : returnValue.toString());
      return null;
    }

    @Override
    public SlimExceptionResult evaluateException(
        SlimExceptionResult exceptionResult) {
      // TODO Auto-generated method stub
      return null;
    }
  }

  class ReturnedSymbolExpectation extends ReturnedValueExpectation {
    private String symbolName;
    private String assignToName = null;

    public ReturnedSymbolExpectation(int col, int row, String symbolName) {
      super(col, row);
      this.symbolName = symbolName;
    }

    public ReturnedSymbolExpectation(String expected, int col, int row, String symbolName) {
      super(col, row, expected);
      this.symbolName = symbolName;
    }

    public ReturnedSymbolExpectation(int col, int row, String symbolName, String assignToName) {
      super(col, row);
      this.symbolName = symbolName;
      this.assignToName = assignToName;
    }

    @Override
    public TestResult evaluateExpectation(Object returnValue) {
      String value = getSymbol(this.symbolName);
      // if value == null 'test not run' will be reported
      // this is good for this handles the case one of the methods
      // of the scenario threw a stop test exception before the symbol
      // was assigned
      return super.evaluateExpectation(value);
    }

    @Override
    protected SlimTestResult createEvaluationMessage(String actual, String expected) {
      if (assignToName != null) {
        setSymbol(assignToName, actual);
        return SlimTestResult.plain(String.format("$%s<-[%s]", assignToName, actual));
      } else {
        return super.createEvaluationMessage(actual, expected);
      }
    }
  }

  class RejectedValueExpectation extends ReturnedValueExpectation {
    public RejectedValueExpectation(int col, int row) {
      super(col, row);
    }

    @Override
    protected SlimTestResult createEvaluationMessage(String actual, String expected) {
      SlimTestResult testResult = super.createEvaluationMessage(actual, expected);
      if (testResult != null)
        return testResult.negateTestResult();
      return null;
    }
  }

  class Comparator {
    private final String expression;
    private final String actual;
    private final String expected;
    private final Pattern simpleComparison = Pattern.compile(
      "\\A\\s*_?\\s*(!?(?:(?:[<>]=?)|(?:[~]?=)))\\s*(-?\\d*\\.?\\d+)\\s*\\Z"
    );
    private final Pattern range = Pattern.compile(
      "\\A\\s*(-?\\d*\\.?\\d+)\\s*<(=?)\\s*_\\s*<(=?)\\s*(-?\\d*\\.?\\d+)\\s*\\Z"
    );

    private Pattern regexPattern = Pattern.compile("\\s*=~/(.*)/");
    private Pattern customComparatorPattern = Pattern.compile("\\s*(\\w*):(.*)", Pattern.DOTALL);
    private double v;
    private double arg1;
    private double arg2;
    public String operation;
    private String arg1Text;

    public Comparator(String actual, String expected) {
      this.expression = replaceSymbols(expected);
      this.actual = actual;
      this.expected = expected;
    }

    public Comparator(String expression, String actual, String expected) {
      this.expression = expression;
      this.actual = actual;
      this.expected = expected;
    }

    public boolean matches() {
      TestResult testResult = evaluate();
      return testResult != null && testResult.getExecutionResult() == ExecutionResult.PASS;
    }

    public SlimTestResult evaluate() {
      SlimTestResult message = evaluateRegularExpressionIfPresent();
      if (message != null)
        return message;

      message = evaluateCustomComparatorIfPresent();
      if (message != null)
        return message;

      operation = matchSimpleComparison();
      if (operation != null)
        return doSimpleComparison();

      Matcher matcher = range.matcher(expression);
      if (matcher.matches() && canUnpackRange(matcher)) {
        return doRange(matcher);
      } else
        return null;
    }

    private SlimTestResult evaluateCustomComparatorIfPresent() {
      SlimTestResult message = null;
      if (customComparatorRegistry == null) {
        return null;
      }
      Matcher customComparatorMatcher = customComparatorPattern.matcher(expression);
      if (customComparatorMatcher.matches()) {
        String prefix = customComparatorMatcher.group(1);
        CustomComparator customComparator = customComparatorRegistry.getCustomComparatorForPrefix(prefix);
        if (customComparator != null) {
          String expectedString = customComparatorMatcher.group(2);
          try {
            if (customComparator.matches(actual, expectedString)) {
              message = SlimTestResult.pass(expectedString + " matches " + actual);
            } else {
              message = SlimTestResult.fail(expectedString + " doesn't match " + actual);
            }
          } catch (Exception e) {
            message = SlimTestResult.fail(expectedString + " doesn't match " + actual + ":\n" + e.getMessage());
          }
        }
      }
      return message;
    }

    private SlimTestResult evaluateRegularExpressionIfPresent() {
      Matcher regexMatcher = regexPattern.matcher(expression);
      SlimTestResult message = null;
      if (regexMatcher.matches()) {
        String pattern = regexMatcher.group(1);
        message = evaluateRegularExpression(pattern);
      }
      return message;
    }

    private SlimTestResult evaluateRegularExpression(String pattern) {
      SlimTestResult message;
      Matcher patternMatcher = Pattern.compile(pattern).matcher(actual);
      if (patternMatcher.find()) {
        message = SlimTestResult.pass(String.format("/%s/ found in: %s", pattern, actual));
      } else {
        message = SlimTestResult.fail(String.format("/%s/ not found in: %s", pattern, actual));
      }
      return message;
    }

    private SlimTestResult doRange(Matcher matcher) {
      boolean closedLeft = matcher.group(2).equals("=");
      boolean closedRight = matcher.group(3).equals("=");
      boolean pass = (arg1 < v && v < arg2) || (closedLeft && arg1 == v) || (closedRight && arg2 == v);
      return rangeMessage(pass);
    }

    private SlimTestResult rangeMessage(boolean pass) {
      String[] fragments = expected.trim().replaceAll("( )+", " ").split("_");
      String message = String.format("%s%s%s", fragments[0], actual, fragments[1]);
      message = replaceSymbolsWithFullExpansion(message);
      return pass ? SlimTestResult.pass(message) : SlimTestResult.fail(message);
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

    private SlimTestResult doSimpleComparison() {
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

    private SlimTestResult simpleComparisonMessage(boolean pass) {
      String message = String.format("%s%s", actual, expected.trim().replaceAll("( )+", " "));
      message = replaceSymbolsWithFullExpansion(message);
      return pass ? SlimTestResult.pass(message) : SlimTestResult.fail(message);

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
