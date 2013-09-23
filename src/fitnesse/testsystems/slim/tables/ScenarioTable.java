// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.slim.instructions.Instruction;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.results.SlimTestResult;
import util.StringUtil;


public class ScenarioTable extends SlimTable {
  private static final String instancePrefix = "scenarioTable";
  private static final String underscorePattern = "\\W_(?:\\W|$)";
  private String name;
  private List<String> inputs = new ArrayList<String>();
  private Set<String> outputs = new HashSet<String>();
  private final int colsInHeader = table.getColumnCountInRow(0);
  private boolean parameterized = false;

  public ScenarioTable(Table table, String tableId,
                       SlimTestContext testContext) {
    super(table, tableId, testContext);
  }

  protected String getTableType() {
    return instancePrefix;
  }

  @Override
  public List<SlimAssertion> getAssertions() throws SyntaxError {
    parseTable();

    // Note: scenario's only add instructions when needed to,
    // since they might need parameters.
    return Collections.emptyList();
  }

  private void parseTable() throws SyntaxError {
    validateHeader();

    parameterized = determineParameterized();
    name = getScenarioName();
    getTestContext().addScenario(name, this);
    getScenarioArguments();
  }

  protected boolean determineParameterized() {
    String firstNameCell = table.getCellContents(1, 0);
    return isNameParameterized(firstNameCell);
  }

    protected void getScenarioArguments() {
    if (parameterized) {
      getArgumentsForParameterizedName();
    } else {
      getArgumentsForAlternatingName();
    }
  }

  private void getArgumentsForAlternatingName() {
    for (int inputCol = 2; inputCol < colsInHeader; inputCol += 2) {
      String argName = table.getCellContents(inputCol, 0);

      if (argName.endsWith("?")) {
        String disgracedArgName = Disgracer.disgraceMethodName(argName.substring(
          0, argName.length()));
        outputs.add(disgracedArgName);
      } else {
        String disgracedArgName = Disgracer.disgraceMethodName(argName);
        inputs.add(disgracedArgName);
      }
    }
  }

  private void getArgumentsForParameterizedName() {
    String argumentString = table.getCellContents(2, 0);
    String[] arguments = argumentString.split(",");

    for (String argument : arguments) {
      addInput(Disgracer.disgraceMethodName(argument.trim()));
    }
  }

  protected void addInput(String argument) {
    inputs.add(argument);
  }

  public String getScenarioName() {
    if (parameterized) {
      String parameterizedName = table.getCellContents(1, 0);

      return unparameterize(parameterizedName);
    } else {
      return getNameFromAlternatingCells();
    }
  }

  public static boolean isNameParameterized(String firstNameCell) {
    Pattern regPat = Pattern.compile(underscorePattern);
    Matcher underscoreMatcher = regPat.matcher(firstNameCell);

    return underscoreMatcher.find();
  }

  public static String unparameterize(String firstNameCell) {
    String name = firstNameCell.replaceAll(underscorePattern, " ").trim();

    return Disgracer.disgraceClassName(name);
  }

  private String getNameFromAlternatingCells() {
    StringBuffer nameBuffer = new StringBuffer();

    for (int nameCol = 1; nameCol < colsInHeader; nameCol += 2)
      nameBuffer.append(table.getCellContents(nameCol, 0)).append(" ");

    return Disgracer.disgraceClassName(nameBuffer.toString().trim());
  }

  private void validateHeader() throws SyntaxError {
    if (colsInHeader <= 1) {
      throw new SyntaxError("Scenario tables must have a name.");
    }
  }

  public String getName() {
    return name;
  }

  public Set<String> getInputs() {
    return new HashSet<String>(inputs);
  }

  public Set<String> getOutputs() {
    return outputs;
  }

  public List<SlimAssertion> call(final Map<String, String> scenarioArguments,
                   SlimTable parentTable, int row) throws SyntaxError {
    Table newTable = getTable().asTemplate(new Table.CellContentSubstitution() {
      @Override
      public String substitute(int col, int row, String content) throws SyntaxError {
        for (Map.Entry<String, String> scenarioArgument : scenarioArguments.entrySet()) {
          String arg = scenarioArgument.getKey();
          if (getInputs().contains(arg)) {
            String argument = scenarioArguments.get(arg);
            content = StringUtil.replaceAll(content, "@" + arg, argument);
            content = StringUtil.replaceAll(content, "@{" + arg + "}", argument);
          } else {
            throw new SyntaxError(String.format("The argument %s is not an input to the scenario.", arg));
          }
        }
        return content;
      }
    });
    ScenarioTestContext testContext = new ScenarioTestContext(parentTable.getTestContext());
    ScriptTable t = createChild(testContext, newTable);
    parentTable.addChildTable(t, row);
    List<SlimAssertion> assertions = t.getAssertions();
    assertions.add(makeAssertion(Instruction.NOOP_INSTRUCTION, new ScenarioExpectation(t, row)));
    return assertions;
  }

  protected ScriptTable createChild(ScenarioTestContext testContext, Table newTable) {
    return new ScriptTable(newTable, id, testContext);
  }

  public List<SlimAssertion> call(String[] args, ScriptTable parentTable, int row) throws SyntaxError {
    Map<String, String> scenarioArguments = new HashMap<String, String>();

    for (int i = 0; (i < inputs.size()) && (i < args.length); i++)
      scenarioArguments.put(inputs.get(i), args[i]);

    return call(scenarioArguments, parentTable, row);
  }

  public boolean isParameterized() {
    return parameterized;
  }

///// scriptTable matcher logic:
  public String[] matchParameters(String invokingString) {
    String parameterizedName;

    if (parameterized) {
      parameterizedName = table.getCellContents(1, 0);
    } else if (this.inputs.size() > 0) {
      StringBuilder nameBuffer = new StringBuilder();

      for (int nameCol = 1; nameCol < colsInHeader; nameCol += 2)
        nameBuffer.append(table.getCellContents(nameCol, 0))
          .append(" _ ");

      parameterizedName = nameBuffer.toString().trim();
    } else {
      return null;
    }

    return getArgumentsMatchingParameterizedName(parameterizedName,
      invokingString);
  }

  private String[] getArgumentsMatchingParameterizedName(
    String parameterizedName, String invokingString) {
    Matcher matcher = makeParameterizedNameMatcher(parameterizedName,
      invokingString);

    if (matcher.matches()) {
      return extractNamesFromMatcher(matcher);
    } else {
      return null;
    }
  }

  private Matcher makeParameterizedNameMatcher(String parameterizedName,
                                               String invokingString) {
    String patternString = parameterizedName.replaceAll("_", "(.*)");
    Pattern pattern = Pattern.compile(patternString);
    Matcher matcher = pattern.matcher(invokingString);

    return matcher;
  }

  private String[] extractNamesFromMatcher(Matcher matcher) {
    String[] arguments = new String[matcher.groupCount()];

    for (int i = 0; i < arguments.length; i++) {
      arguments[i] = matcher.group(i + 1);
    }

    return arguments;
  }
//// till here

  private final class ScenarioExpectation extends RowExpectation {
    private ScriptTable scriptTable;

    private ScenarioExpectation(ScriptTable scriptTable, int row) {
      super(-1, row); // We don't care about anything but the row.
      this.scriptTable = scriptTable;
    }

    @Override
    public TestResult evaluateExpectation(Object returnValue) {
      SlimTable parent = scriptTable.getParent();
      ExecutionResult testStatus = ((ScenarioTestContext) scriptTable.getTestContext()).getExecutionResult();
      parent.getTable().updateContent(getRow(), new SlimTestResult(testStatus));
      return null;
    }

    @Override
    protected SlimTestResult createEvaluationMessage(String actual, String expected) {
      return null;
    }
  }

  // This context is mainly used to determine if the scenario table evaluated successfully
  // This determines the execution result for the "calling" table row.
  final class ScenarioTestContext implements SlimTestContext {

    private final SlimTestContext testContext;
    private final TestSummary testSummary = new TestSummary();

    public ScenarioTestContext(SlimTestContext testContext) {
      this.testContext = testContext;
    }

    @Override
    public String getSymbol(String symbolName) {
      return testContext.getSymbol(symbolName);
    }

    @Override
    public void setSymbol(String symbolName, String value) {
      testContext.setSymbol(symbolName, value);
    }

    @Override
    public void addScenario(String scenarioName, ScenarioTable scenarioTable) {
      testContext.addScenario(scenarioName, scenarioTable);
    }

    @Override
    public ScenarioTable getScenario(String scenarioName) {
      return testContext.getScenario(scenarioName);
    }

    @Override
    public Collection<ScenarioTable> getScenarios() {
      return testContext.getScenarios();
    }

    @Override
    public void incrementPassedTestsCount() {
      testContext.incrementPassedTestsCount();
      testSummary.right++;
    }

    @Override
    public void incrementFailedTestsCount() {
      testContext.incrementFailedTestsCount();
      testSummary.wrong++;
    }

    @Override
    public void incrementErroredTestsCount() {
      testContext.incrementErroredTestsCount();
      testSummary.exceptions++;
    }

    @Override
    public void incrementIgnoredTestsCount() {
      testContext.incrementIgnoredTestsCount();
      testSummary.ignores++;
    }

    @Override
    public void increment(ExecutionResult result) {
      testContext.increment(result);
      testSummary.add(result);
    }

    @Override
    public void increment(TestSummary summary) {
      testContext.increment(summary);
      testSummary.add(summary);
    }

    ExecutionResult getExecutionResult() {
      return ExecutionResult.getExecutionResult(testSummary);
    }
  }
}
