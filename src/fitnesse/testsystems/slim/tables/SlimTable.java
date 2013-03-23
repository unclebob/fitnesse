// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import static fitnesse.testsystems.slim.tables.ComparatorUtil.approximatelyEqual;
import static java.lang.Character.isLetterOrDigit;
import static java.lang.Character.toUpperCase;
import static util.ListUtility.list;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.slim.instructions.CallAndAssignInstruction;
import fitnesse.slim.instructions.CallInstruction;
import fitnesse.slim.instructions.Instruction;
import fitnesse.slim.instructions.MakeInstruction;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.TestResult;

public abstract class SlimTable {
  private static final Pattern SYMBOL_ASSIGNMENT_PATTERN = Pattern.compile("\\A\\s*\\$(\\w+)\\s*=\\s*\\Z");

  private String tableName;
  private int instructionNumber = 0;

  private List<SlimTable> children = new LinkedList<SlimTable>();
  private SlimTable parent = null;

  private SlimTestContext testContext;

  protected final Table table;
  protected String id;

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
    slimtable.tableName = makeInstructionTag(instructionNumber) + "/" + slimtable.tableName;
    instructionNumber++;
    slimtable.parent = this;
    children.add(slimtable);

    Table parentTable = getTable();
    Table childTable = slimtable.getTable();
    //childTable.setName(slimtable.tableName);
    parentTable.appendChildTable(row, childTable);
  }

  public String replaceSymbols(String s) {
    return new SymbolReplacer(s).replace();
  }

  public String replaceSymbolsWithFullExpansion(String s) {
    return new FullExpansionSymbolReplacer(s).replace();
  }


  protected abstract String getTableType();

  public abstract List<Assertion> getAssertions() throws SyntaxError;

  protected String makeInstructionTag() {
    return makeInstructionTag(instructionNumber++);
  }

  protected String makeInstructionTag(int instructionNumber) {
    return String.format("%s_%d", tableName, instructionNumber);
  }

  public String getTableName() {
    return tableName;
  }

  public String getSymbol(String variableName) {
    return testContext.getSymbol(variableName);
  }

  public void setSymbol(String variableName, String value) {
    testContext.setSymbol(variableName, value);
  }

  public Table getTable() {
    return table;
  }

  protected Assertion constructFixture(String fixtureName) {
    return constructInstance(getTableName(), fixtureName, 0, 0);
  }

  protected String getFixtureName() {
    String tableHeader = table.getCellContents(0, 0);
    String fixtureName = getFixtureName(tableHeader);
    String disgracedFixtureName = Disgracer.disgraceClassName(fixtureName);
    return disgracedFixtureName;
  }

  protected String getFixtureName(String tableHeader) {
    if (!tableHeader.contains(":"))
      return tableHeader;
    return tableHeader.split(":")[1];
  }

  protected Assertion constructInstance(String instanceName, String className, int classNameColumn, int row) {
    RowExpectation expectation = new ConstructionExpectation(classNameColumn, row);
    return makeAssertion(new MakeInstruction(makeInstructionTag(), instanceName, className, gatherConstructorArgumentsStartingAt(classNameColumn + 1, row)),
            expectation);
  }

  protected final Assertion makeAssertion(Instruction instruction, Expectation expectation) {
    return new Assertion(instruction, expectation);
  }

  protected Object[] gatherConstructorArgumentsStartingAt(int startingColumn, int row) {
    int columnCount = table.getColumnCountInRow(row);
    List<String> arguments = new ArrayList<String>();
    for (int col = startingColumn; col < columnCount; col++) {
      arguments.add(table.getUnescapedCellContents(col, row));
    }
    return arguments.toArray(new String[arguments.size()]);
  }

  protected Instruction callFunction(String instanceName, String functionName, Object... args) {
    return new CallInstruction(makeInstructionTag(), instanceName, Disgracer.disgraceMethodName(functionName), args);
  }

  protected Instruction callAndAssign(String symbolName, String instanceName, String functionName, String... args) {
    return new CallAndAssignInstruction(makeInstructionTag(), symbolName, instanceName, Disgracer.disgraceMethodName(functionName), args);
  }

  @Deprecated
  public boolean shouldIgnoreException(String resultKey, String resultString) {
    return false;
  }

  protected String ifSymbolAssignment(int col, int row) {
    String expected = table.getCellContents(col, row);
    Matcher matcher = SYMBOL_ASSIGNMENT_PATTERN.matcher(expected);
    return matcher.find() ? matcher.group(1) : null;
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

  public List<SlimTable> getChildren() {
    return children;
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
      if (nameHasDotsBeforeEnd() || nameHasDollars())
        return name;
      else if (isGraceful()) {
        return disgraceClassName();
      } else {
        return name;
      }
    }

    private boolean nameHasDollars() {
      return name.indexOf("$") != -1;
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

  /** Expectation base class for row based expectations. */
  public abstract class RowExpectation implements Expectation {
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

    /* (non-Javadoc)
     * @see fitnesse.testsystems.slim.tables.Expectation#evaluateExpectation(java.util.Map)
     */
    @Override
    public TestResult evaluateExpectation(Object returnValue) {
      TestResult testResult;
      if (returnValue == null) {
        testResult = TestResult.ignore("Test not run");
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

    TestResult evaluationMessage(String actual, String expected) {
      return createEvaluationMessage(actual, expected);
    }

    protected abstract TestResult createEvaluationMessage(String actual, String expected);

    @Override
    public ExceptionResult evaluateException(ExceptionResult exceptionResult) {
      table.updateContent(col, row, exceptionResult);
      getTestContext().incrementErroredTestsCount();
      return exceptionResult;
    }

    public int getCol() {
      return col;
    }

    public int getRow() {
      return row;
    }

    // Used only by XmlFormatter.SlimTestXmlFormatter
    public String getExpected() {
      return originalContent;
    }
  }

  class SymbolReplacer {
    protected String replacedString;
    private Matcher symbolMatcher;
    private final Pattern symbolPattern = Pattern.compile("\\$([a-zA-Z]\\w*)");
    private int startingPosition;

    SymbolReplacer(String s) {
      this.replacedString = s;
      symbolMatcher = symbolPattern.matcher(s);
    }

    String replace() {
      replaceAllSymbols();
      return replacedString;
    }

    private void replaceAllSymbols() {
      startingPosition = 0;
      while (symbolFound())
        replaceSymbol();
    }

    private void replaceSymbol() {
      String symbolName = symbolMatcher.group(1);
      String value = formatSymbol(symbolName);
      String prefix = replacedString.substring(0, symbolMatcher.start());
      String suffix = replacedString.substring(symbolMatcher.end());
      replacedString = prefix + value + suffix;
      int replacementEnd = symbolMatcher.start() + value.length();
      startingPosition = Math.min(replacementEnd, replacedString.length());
    }

    private String formatSymbol(String symbolName) {
      String value = getSymbol(symbolName);
      if (value == null) {
        for (int i = symbolName.length() - 1; i > 0; i--) {
          String str = symbolName.substring(0, i);
          if ((value = getSymbol(str)) != null)
            return formatSymbolValue(str, value) + symbolName.substring(i, symbolName.length());
        }

        return "$" + symbolName;
      } else
        return formatSymbolValue(symbolName, value);
    }


    private boolean symbolFound() {
      symbolMatcher = symbolPattern.matcher(replacedString);
      return symbolMatcher.find(startingPosition);
    }

    protected String formatSymbolValue(String name, String value) {
      return value;
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
    protected TestResult createEvaluationMessage(String actual, String expected) {
      return TestResult.plain(replaceSymbolsWithFullExpansion(expected));
    }
  }

  class SilentReturnExpectation implements Expectation {
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
    public ExceptionResult evaluateException(ExceptionResult exceptionResult) {
      if (exceptionResult.isNoMethodInClassException()) {
        return null;
      }
      table.updateContent(col, row, exceptionResult);
      getTestContext().incrementErroredTestsCount();
      return exceptionResult;
    }
  }

  class ConstructionExpectation extends RowExpectation {
    public ConstructionExpectation(int col, int row) {
      super(col, row);
    }

    @Override
    protected TestResult createEvaluationMessage(String actual, String expected) {
      if ("OK".equalsIgnoreCase(actual))
        return TestResult.ok(replaceSymbolsWithFullExpansion(expected));
      else
        return TestResult.error("Unknown construction message", actual);
    }
  }

  class SymbolAssignmentExpectation extends RowExpectation {
    private String symbolName;

    SymbolAssignmentExpectation(String symbolName, int col, int row) {
      super(col, row);
      this.symbolName = symbolName;
    }

    // TODO: make something useful for substitution
    @Override
    protected TestResult createEvaluationMessage(String actual, String expected) {
      setSymbol(symbolName, actual);
      return TestResult.plain(String.format("$%s<-[%s]", symbolName, actual));
    }
  }

  class ReturnedValueExpectation extends RowExpectation {
    public ReturnedValueExpectation(int col, int row) {
      super(col, row, table.getUnescapedCellContents(col, row));
    }

    @Override
    protected TestResult createEvaluationMessage(String actual, String expected) {
      TestResult testResult;
      String replacedExpected = replaceSymbols(expected);

      if (actual == null)
        testResult = TestResult.fail("null", replacedExpected); //todo can't be right message.
      else if (actual.equals(replacedExpected))
        testResult = TestResult.pass(announceBlank(replaceSymbolsWithFullExpansion(expected)));
      else if (replacedExpected.length() == 0)
        testResult = TestResult.ignore(actual);
      else {
        testResult = new Comparator(replacedExpected, actual, expected).evaluate();
        if (testResult == null)
          testResult = TestResult.fail(actual, replaceSymbolsWithFullExpansion(expected));
      }

      return testResult;
    }

    private String announceBlank(String originalValue) {
      return originalValue.length() == 0 ? "BLANK" : originalValue;
    }

  }

  class RejectedValueExpectation extends ReturnedValueExpectation {
    public RejectedValueExpectation(int col, int row) {
      super(col, row);
    }

    @Override
    protected TestResult createEvaluationMessage(String actual, String expected) {
      TestResult testResult = super.createEvaluationMessage(actual, expected);
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

    public TestResult evaluate() {
      TestResult message = evaluateRegularExpressionIfPresent();
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

    private TestResult evaluateRegularExpressionIfPresent() {
      Matcher regexMatcher = regexPattern.matcher(expression);
      TestResult message = null;
      if (regexMatcher.matches()) {
        String pattern = regexMatcher.group(1);
        message = evaluateRegularExpression(pattern);
      }
      return message;
    }

    private TestResult evaluateRegularExpression(String pattern) {
      TestResult message;
      Matcher patternMatcher = Pattern.compile(pattern).matcher(actual);
      if (patternMatcher.find()) {
        message = TestResult.pass(String.format("/%s/ found in: %s", pattern, actual));
      } else {
        message = TestResult.fail(String.format("/%s/ not found in: %s", pattern, actual));
      }
      return message;
    }

    private TestResult doRange(Matcher matcher) {
      boolean closedLeft = matcher.group(2).equals("=");
      boolean closedRight = matcher.group(3).equals("=");
      boolean pass = (arg1 < v && v < arg2) || (closedLeft && arg1 == v) || (closedRight && arg2 == v);
      return rangeMessage(pass);
    }

    private TestResult rangeMessage(boolean pass) {
      String[] fragments = expected.replaceAll(" ", "").split("_");
      String message = String.format("%s%s%s", fragments[0], actual, fragments[1]);
      message = replaceSymbolsWithFullExpansion(message);
      return pass ? TestResult.pass(message) : TestResult.fail(message);
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

    private TestResult doSimpleComparison() {
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

    private TestResult simpleComparisonMessage(boolean pass) {
      String message = String.format("%s%s", actual, expected.replaceAll(" ", ""));
      message = replaceSymbolsWithFullExpansion(message);
      return pass ? TestResult.pass(message) : TestResult.fail(message);

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
