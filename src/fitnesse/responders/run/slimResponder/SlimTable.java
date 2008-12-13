package fitnesse.responders.run.slimResponder;

import fitnesse.responders.run.TestSummary;
import static fitnesse.util.ListUtility.list;
import fitnesse.wikitext.Utils;

import static java.lang.Character.isLetterOrDigit;
import static java.lang.Character.toUpperCase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SlimTable {
  protected Table table;
  private SlimTestContext testContext;
  protected String id;
  protected String tableName;
  private int instructionNumber = 0;
  private List<Object> instructions;
  private List<Expectation> expectations = new ArrayList<Expectation>();
  protected static final Pattern symbolAssignmentPattern = Pattern.compile("\\A\\s*\\$(\\w+)\\s*=\\s*\\Z");
  private TestSummary testSummary = new TestSummary();

  public SlimTable(Table table, String id) {
    this(table, id, new LocalSlimTestContext());
  }

  public SlimTable(Table table, String id, SlimTestContext testContext) {
    this.id = id;
    this.table = table;
    this.testContext = testContext;
    tableName = getTableType() + "_" + id;
    instructions = new ArrayList<Object>();
  }

  protected void addExpectation(Expectation e) {
    expectations.add(e);
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
      table.setCell(0, 0, fail(String.format("!-%s: Bad table: %s-!", tableName, e.getMessage())));
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

  protected int getInstructionNumber() {
    return instructionNumber;
  }

  protected String getTableName() {
    return tableName;
  }

  protected void addInstruction(List<Object> instruction) {
    instructions.add(instruction);
  }

  public void evaluateExpectations(Map<String, Object> returnValues) throws Exception {
    for (Expectation expectation : expectations)
      expectation.evaluateExpectation(returnValues);
    evaluateReturnValues(returnValues);
  }

  protected abstract void evaluateReturnValues(Map<String, Object> returnValues) throws Exception;

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
    String tableHeader = table.getCellContents(0, 0);
    String fixtureName = getFixtureName(tableHeader);
    String disgracedFixtureName = Disgracer.disgraceClassName(fixtureName);
    constructInstance(getTableName(), disgracedFixtureName, 0, 0);
  }

  private String getFixtureName(String tableHeader) {
    if (tableHeader.indexOf(":") == -1)
      return tableHeader;
    return tableHeader.split(":")[1];
  }

  protected void constructInstance(String instanceName, String className, int classNameColumn, int row) {
    Expectation expectation = new ConstructionExpectation(getInstructionNumber(), classNameColumn, row);
    addExpectation(expectation);
    List<Object> makeInstruction = prepareInstruction();
    makeInstruction.add("make");
    makeInstruction.add(instanceName);

    makeInstruction.add(className);
    addArgsToInstruction(makeInstruction, cellsStartingAt(classNameColumn + 1, row));
    addInstruction(makeInstruction);
  }

  protected Object[] cellsStartingAt(int startingColumn, int row) {
    int columnCount = table.getColumnCountInRow(row);
    List<String> arguments = new ArrayList<String>();
    for (int col = startingColumn; col < columnCount; col++)
      arguments.add(table.getUnescapedCellContents(col, row));
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
    String expected, int instructionNumber, int col, int row
  ) {
    return new ReturnedValueExpectation(expected, instructionNumber, col, row);
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
    protected String expectedValue;
    protected int col;
    protected int row;
    protected int instructionNumber;

    public Expectation(String expected, int instructionNumber, int col, int row) {
      expectedValue = expected;
      this.row = row;
      this.instructionNumber = instructionNumber;
      this.col = col;
    }

    protected void evaluateExpectation(Map<String, Object> returnValues) {
      Object returnValue = returnValues.get(makeInstructionTag(instructionNumber));
      String value = returnValue.toString();
      String originalContent = table.getCellContents(col, row);
      String evaluationMessage;
      evaluationMessage = evaluationMessage(value, originalContent);
      if (evaluationMessage != null)
        table.setCell(col, row, evaluationMessage);
    }

    private String evaluationMessage(String value, String originalContent) {
      String evaluationMessage;
      if (isExceptionMessage(value))
        evaluationMessage = originalContent + " " + error(extractExeptionMessage(value));
      else
        evaluationMessage = createEvaluationMessage(value, originalContent);
      return evaluationMessage;
    }

    protected abstract String createEvaluationMessage(String value, String originalValue);
  }

  private static class LocalSlimTestContext implements SlimTestContext {
    private Map<String, String> symbols = new HashMap<String, String>();

    public String getSymbol(String symbolName) {
      return symbols.get(symbolName);
    }

    public void setSymbol(String symbolName, String value) {
      symbols.put(symbolName, value);
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
    public VoidReturnExpectation(int instructionNumber, int col, int row) {
      super(null, instructionNumber, col, row);
    }

    protected String createEvaluationMessage(String value, String originalValue) {
      return replaceSymbolsWithFullExpansion(originalValue);
    }
  }

  class SilentReturnExpectation extends Expectation {
    public SilentReturnExpectation(int instructionNumber, int col, int row) {
      super(null, instructionNumber, col, row);
    }

    protected String createEvaluationMessage(String value, String originalValue) {
      return null;
    }
  }

  class ConstructionExpectation extends Expectation {
    public ConstructionExpectation(int instructionNumber, int col, int row) {
      super(null, instructionNumber, col, row);
    }

    protected String createEvaluationMessage(String value, String originalValue) {
      if ("OK".equalsIgnoreCase(value))
        return pass(originalValue);
      else
        return "!style_error(Unknown construction message:) " + value;
    }
  }

  class SymbolAssignmentExpectation extends Expectation {
    private String symbolName;

    SymbolAssignmentExpectation(String symbolName, int instructionNumber, int col, int row) {
      super(null, instructionNumber, col, row);
      this.symbolName = symbolName;
    }

    protected String createEvaluationMessage(String value, String originalValue) {
      setSymbol(symbolName, value);
      return String.format("$%s<-[%s]", symbolName, value);
    }
  }


  class ReturnedValueExpectation extends Expectation {
    public ReturnedValueExpectation(String expected, int instructionNumber, int col, int row) {
      super(expected, instructionNumber, col, row);
    }

    protected String createEvaluationMessage(String value, String originalValue) {
      String evaluationMessage;
      String replacedValue = Utils.unescapeHTML(replaceSymbols(expectedValue));
      if (value == null)
        evaluationMessage = fail("null"); //todo can't be right message.
      else if (value.equals(replacedValue))
        evaluationMessage = pass(announceBlank(replaceSymbolsWithFullExpansion(originalValue)));
      else if (replacedValue.length() == 0)
        evaluationMessage = ignore(value);
      else {
        String expressionMessage = new Comparator(replacedValue, value, expectedValue).evaluate();
        if (expressionMessage != null)
          evaluationMessage = expressionMessage;
        else if (value.indexOf("Exception:") != -1) {
          evaluationMessage = error(value);
        } else
          evaluationMessage = failMessage(value,
            String.format("expected [%s]", replaceSymbolsWithFullExpansion(originalValue))
          );
      }

      return evaluationMessage;
    }

    private String announceBlank(String originalValue) {
      return originalValue.length() == 0 ? "BLANK" : originalValue;
    }

    class Comparator {
      private String expression;
      private String value;
      private String originalExpression;
      private Pattern simpleComparison = Pattern.compile(
        "\\A\\s*_?\\s*((?:[<>]=?)|(?:[!~]=))\\s*(\\d*\\.?\\d+)\\s*\\Z"
      );
      private Pattern range = Pattern.compile(
        "\\A\\s*(\\d*\\.?\\d+)\\s*<(=?)\\s*_\\s*<(=?)\\s*(\\d*\\.?\\d+)\\s*\\Z"
      );
      private double v;
      private double arg1;
      private double arg2;
      public String operation;
      private String arg1Text;

      private Comparator(String expression, String value, String originalExpression) {
        this.expression = expression;
        this.value = value;
        this.originalExpression = originalExpression;
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
        String[] fragments = originalExpression.replaceAll(" ", "").split("_");
        String message = String.format("%s%s%s", fragments[0], value, fragments[1]);
        message = replaceSymbolsWithFullExpansion(message);
        return pass ? pass(message) : fail(message);

      }

      private boolean canUnpackRange(Matcher matcher) {
        try {
          arg1 = Double.parseDouble(matcher.group(1));
          arg2 = Double.parseDouble(matcher.group(4));
          v = Double.parseDouble(value);
        } catch (NumberFormatException e) {
          return false;
        }
        return true;
      }

      private String doSimpleComparison() {
        if (operation.equals("<"))
          return simpleComparisonMessage(v < arg1);
        else if (operation.equals(">"))
          return simpleComparisonMessage(v > arg1);
        else if (operation.equals(">="))
          return simpleComparisonMessage(v >= arg1);
        else if (operation.equals("<="))
          return simpleComparisonMessage(v <= arg1);
        else if (operation.equals("!="))
          return simpleComparisonMessage(v != arg1);
        else if (operation.equals("~="))
          return simpleComparisonMessage(approximatelyEqual(arg1Text, value));
        else
          return null;
      }

      private String simpleComparisonMessage(boolean pass) {
        String message = String.format("%s%s", value, originalExpression.replaceAll(" ", ""));
        message = replaceSymbolsWithFullExpansion(message);
        return pass ? pass(message) : fail(message);

      }

      private String matchSimpleComparison() {
        Matcher matcher = simpleComparison.matcher(expression);
        if (matcher.matches()) {
          try {
            v = Double.parseDouble(value);
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
}
