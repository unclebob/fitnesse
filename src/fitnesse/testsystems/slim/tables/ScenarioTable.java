// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import fitnesse.slim.instructions.Instruction;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TestExecutionException;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.results.SlimTestResult;
import fitnesse.util.StringUtils;

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


/**
 * Scenario table acts as a factory for script tables. Those tables are created
 * where ever a scenario table is invoked. The type of table used to actually execute
 * the scenario may vary, depending on from which table a scenario is invoked.
 */
public class ScenarioTable extends SlimTable {
  private static final String instancePrefix = "scenarioTable";
  private static final String underscorePattern = "\\W_(?=\\W|$)";
  private String name;
  private List<String> inputs = new ArrayList<>();
  private Set<String> outputs = new HashSet<>();
  private final int colsInHeader = table.getColumnCountInRow(0);
  private boolean parameterized = false;
  private Pattern pattern = null;

  public ScenarioTable(Table table, String tableId,
                       SlimTestContext testContext) {
    super(table, tableId, testContext);
  }

  @Override
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
    getScenarioArguments();
    setParameterMatchingPattern();
    getTestContext().addScenario(name, this);
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

      splitInputAndOutputArguments(argName);
    }
  }

  private void splitInputAndOutputArguments(String argName) {
    argName = argName.trim();
    if (argName.endsWith("?")) {
      String disgracedArgName = Disgracer.disgraceMethodName(argName);
      outputs.add(disgracedArgName);
    } else {
      String disgracedArgName = Disgracer.disgraceMethodName(argName);
      inputs.add(disgracedArgName);
    }
  }

  private void getArgumentsForParameterizedName() {
    String argumentString = table.getCellContents(2, 0);
    String[] arguments = argumentString.split(",");

    for (String argument : arguments) {
        splitInputAndOutputArguments(argument);
    }
  }

  protected void addInput(String argument) {
    inputs.add(argument);
  }

  protected void addOutput(String argument) {
    outputs.add(argument);
  }

  public String getScenarioName() {
    if (parameterized) {
      String parameterizedName = table.getCellContents(1, 0);

      return unparameterize(parameterizedName);
    } else {
      return getNameFromAlternatingCells();
    }
  }

  private boolean isNameParameterized(String firstNameCell) {
    Pattern regPat = Pattern.compile(underscorePattern);
    Matcher underscoreMatcher = regPat.matcher(firstNameCell);

    return underscoreMatcher.find();
  }

  private String unparameterize(String firstNameCell) {
    String name = firstNameCell.replaceAll(underscorePattern, " ").trim();

    return Disgracer.disgraceClassName(name);
  }

  private String getNameFromAlternatingCells() {
    StringBuilder nameBuffer = new StringBuilder();

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
    return new HashSet<>(inputs);
  }

  public Set<String> getOutputs() {
    return new HashSet<>(outputs);
  }

  public List<SlimAssertion> call(final Map<String, String> scenarioArguments,
                   SlimTable parentTable, int row) throws TestExecutionException {
    Table newTable = getTable().asTemplate(new Table.CellContentSubstitution() {
      @Override
      public String substitute(String content) throws SyntaxError {
        for (Map.Entry<String, String> scenarioArgument : scenarioArguments.entrySet()) {
          String arg = scenarioArgument.getKey();
          if (getInputs().contains(arg)) {
            String argument = scenarioArguments.get(arg);
            content = StringUtils.replace(content, "@" + arg, argument);
            content = StringUtils.replace(content, "@{" + arg + "}", argument);
          } else {
            throw new SyntaxError(String.format("The argument %s is not an input to the scenario.", arg));
          }
        }
        return content;
      }
    });
    ScenarioTestContext testContext = new ScenarioTestContext(parentTable.getTestContext());
    ScriptTable t = createChild(testContext, parentTable, newTable);
    parentTable.addChildTable(t, row);
    List<SlimAssertion> assertions = t.getAssertions();
    assertions.add(makeAssertion(Instruction.NOOP_INSTRUCTION, new ScenarioExpectation(t, row)));
    return assertions;
  }

  protected ScriptTable createChild(ScenarioTestContext testContext, SlimTable parentTable, Table newTable) throws TableCreationException {
    ScriptTable scriptTable;
    if (parentTable instanceof ScriptTable) {
      scriptTable = createChild((ScriptTable) parentTable, newTable, testContext);
    } else {
      scriptTable = createChild(getTestContext().getCurrentScriptClass(), newTable, testContext);
    }
    scriptTable.setCustomComparatorRegistry(customComparatorRegistry);
    return scriptTable;
  }

  protected ScriptTable createChild(ScriptTable parentScriptTable, Table newTable, SlimTestContext testContext) throws TableCreationException {
    return createChild(parentScriptTable.getClass(), newTable, testContext);
  }

  protected ScriptTable createChild(Class<? extends ScriptTable> parentTableClass, Table newTable, SlimTestContext testContext) throws TableCreationException {
      return SlimTableFactory.createTable(parentTableClass, newTable, id, testContext);
  }

  public List<SlimAssertion> call(String[] args, ScriptTable parentTable, int row) throws TestExecutionException {
    Map<String, String> scenarioArguments = new HashMap<>();

    for (int i = 0; (i < inputs.size()) && (i < args.length); i++)
      scenarioArguments.put(inputs.get(i), args[i]);

    return call(scenarioArguments, parentTable, row);
  }

  public boolean isParameterized() {
    return parameterized;
  }

///// scriptTable matcher logic:
  private void setParameterMatchingPattern() {
    String parameterizedName = null;
    if (parameterized) {
      parameterizedName = table.getCellContents(1, 0);
    } else if (!inputs.isEmpty()) {
      StringBuilder nameBuffer = new StringBuilder();

      for (int nameCol = 1; nameCol < colsInHeader; nameCol += 2) {
        String cell = table.getCellContents(nameCol, 0);
        nameBuffer.append(cell)
          .append(" _ ");
      }

      parameterizedName = nameBuffer.toString().trim();
    }
    if (parameterizedName != null) {
      String patternString = StringUtils.replace(parameterizedName, "_", "(.*)");
      pattern = Pattern.compile(patternString);
    }
  }

  public boolean canMatchParameters(String invokingString) {
    Matcher matcher = getMatchingMatcher(invokingString);
    return matcher != null;
  }

  public String[] matchParameters(String invokingString) {
    String[] result = null;
    Matcher matcher = getMatchingMatcher(invokingString);
    if (matcher != null) {
      result = extractNamesFromMatcher(matcher);
    }
    return result;
  }

  private Matcher getMatchingMatcher(String invokingString) {
    Matcher result = null;
    if (pattern != null) {
      Matcher matcher = pattern.matcher(invokingString);
      if (matcher.matches()) {
        result = matcher;
      }
    }
    return result;
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
      if (outputs.isEmpty() || testStatus != ExecutionResult.PASS){
    	  // if the scenario has no output parameters
    	  // or the scenario failed
    	  // then the whole line should be flagged
    	  parent.getTable().updateContent(getRow(), new SlimTestResult(testStatus));
      }
      return null;
    }

    @Override
    protected SlimTestResult createEvaluationMessage(String actual, String expected) {
      return null;
    }
  }

  // This context is mainly used to determine if the scenario table evaluated successfully
  // This determines the execution result for the "calling" table row.
  public final class ScenarioTestContext implements SlimTestContext {

    private final SlimTestContext testContext;
    private final TestSummary testSummary = new TestSummary();

    public ScenarioTestContext(SlimTestContext testContext) {
      this.testContext = testContext;
    }

    public ScenarioTable getScenarioTable() {
      return ScenarioTable.this;
    }

    @Override
    public String getSymbol(String symbolName) {
      return testContext.getSymbol(symbolName);
    }

    @Override
    public Map<String, String> getSymbols() {
      return testContext.getSymbols();
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
    public ScenarioTable getScenarioByPattern(String invokingString) {
      return testContext.getScenarioByPattern(invokingString);
    }

    @Override
    public Collection<ScenarioTable> getScenarios() {
      return testContext.getScenarios();
    }

    @Override
    public void incrementPassedTestsCount() {
      increment(ExecutionResult.PASS);
    }

    @Override
    public void incrementFailedTestsCount() {
      increment(ExecutionResult.FAIL);
    }

    @Override
    public void incrementErroredTestsCount() {
      increment(ExecutionResult.ERROR);
    }

    @Override
    public void incrementIgnoredTestsCount() {
      increment(ExecutionResult.IGNORE);
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

    @Override
    public TestPage getPageToTest() {
      return testContext.getPageToTest();
    }

    @Override
    public void setCurrentScriptClass(Class<? extends ScriptTable> currentScriptClass) {
      testContext.setCurrentScriptClass(currentScriptClass);
    }

    @Override
    public Class<? extends ScriptTable> getCurrentScriptClass() {
      return testContext.getCurrentScriptClass();
    }

    @Override
    public void setCurrentScriptActor(String currentScriptActor) {
      testContext.setCurrentScriptActor(currentScriptActor);
    }

    @Override
    public String getCurrentScriptActor() {
      return testContext.getCurrentScriptActor();
    }
  }
}
