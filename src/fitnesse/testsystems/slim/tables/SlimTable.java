// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.slim.instructions.AssignInstruction;
import fitnesse.slim.instructions.CallAndAssignInstruction;
import fitnesse.slim.instructions.CallInstruction;
import fitnesse.slim.instructions.Instruction;
import fitnesse.slim.instructions.MakeInstruction;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TableCell;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.slim.CustomComparator;
import fitnesse.testsystems.slim.CustomComparatorRegistry;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.results.SlimExceptionResult;
import fitnesse.testsystems.slim.results.SlimTestResult;

import static fitnesse.slim.VariableStore.SYMBOL_PATTERN;

import static fitnesse.testsystems.slim.tables.ComparatorUtil.approximatelyEqual;
import static java.lang.Character.isLetterOrDigit;
import static java.lang.Character.toUpperCase;

public abstract class SlimTable {
  private static final Pattern SYMBOL_ASSIGNMENT_PATTERN = Pattern.compile("\\A\\s*\\$(\\w+)\\s*=\\s*\\Z");

  private String tableName;
  private int instructionNumber = 0;

  private List<SlimTable> children = new LinkedList<SlimTable>();
  private SlimTable parent = null;

  private final SlimTestContext testContext;

  protected final Table table;
  protected String id;
  protected CustomComparatorRegistry customComparatorRegistry;

  private final Map<String, String> symbolsToStore = new HashMap<String, String>();

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
    parentTable.appendChildTable(row, childTable);
  }

  public String replaceSymbols(String s) {
    return new SymbolReplacer(s).replace();
  }

  public String replaceSymbolsWithFullExpansion(String s) {
    return new FullExpansionSymbolReplacer(s).replace();
  }


  protected abstract String getTableType();

  public abstract List<SlimAssertion> getAssertions() throws SyntaxError;

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
    setSymbol(variableName, value, false);
  }

  public void setSymbol(String variableName, String value, boolean toStore) {
    testContext.setSymbol(variableName, value);
    if (toStore) {
      symbolsToStore.put(variableName, value);
    }
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

  protected String getFixtureName() {
    String tableHeader = table.getCellContents(0, 0);
    String fixtureName = getFixtureName(tableHeader);
    return Disgracer.disgraceClassName(fixtureName);
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
    List<String> arguments = new ArrayList<String>();
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

  protected String ifSymbolAssignment(int col, int row) {
    String expected = table.getCellContents(col, row);
    return ifSymbolAssignment(expected);
  }

  protected String ifSymbolAssignment(String expected) {
    Matcher matcher = SYMBOL_ASSIGNMENT_PATTERN.matcher(expected);
    return matcher.find() ? matcher.group(1) : null;
  }

  public SlimTestContext getTestContext() {
    return testContext;
  }

  protected List<List<String>> tableAsList() {
    List<List<String>> tableArgument = new ArrayList<List<String>>();
    int rows = table.getRowCount();
    for (int row = 1; row < rows; row++)
      tableArgument.add(tableRowAsList(row));
    return tableArgument;
  }

  private List<String> tableRowAsList(int row) {
    List<String> rowList = new ArrayList<String>();
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

  /** SlimExpectation base class for row based expectations. */
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

  class SymbolReplacer {
    protected String replacedString;
    private Matcher symbolMatcher;
    private int startingPosition;

    SymbolReplacer(String s) {
      this.replacedString = s;
      symbolMatcher = SYMBOL_PATTERN.matcher(s);
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
      symbolMatcher = SYMBOL_PATTERN.matcher(replacedString);
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

  }

  class ReturnedSymbolExpectation extends ReturnedValueExpectation {
	  private String symbolName;
	  private String assignToName = null;
	  public ReturnedSymbolExpectation(int col, int row, String symbolName) {
	      super(col, row);
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
	      return super.evaluateExpectation(value);
	    }
	    
	    @Override
	    protected SlimTestResult createEvaluationMessage(String actual, String expected) {
	      if (assignToName != null){	
	        setSymbol(assignToName, actual);
	        return SlimTestResult.plain(String.format("$%s<-[%s]", assignToName, actual));
	      }else{
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
    private Pattern customComparatorPattern = Pattern.compile("\\s*(\\w*):(.*)");
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
