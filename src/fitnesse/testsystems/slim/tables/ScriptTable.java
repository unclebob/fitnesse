// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fitnesse.slim.converters.BooleanConverter;
import fitnesse.slim.converters.VoidConverter;
import fitnesse.slim.instructions.Instruction;
import fitnesse.testsystems.TestExecutionException;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.results.SlimTestResult;

public class ScriptTable extends SlimTable {
  private static final String SEQUENTIAL_ARGUMENT_PROCESSING_SUFFIX = ";";

  public ScriptTable(Table table, String tableId, SlimTestContext context) {
    super(table, tableId, context);
  }
  /**
   * Template method to provide the keyword that identifies the table type.
   *
   * @return table type
   */
  @Override
  protected String getTableType() {
    return "scriptTable";
  }

  /**
   * Template method to provide the keyword that identifies the table type.
   *
   * @return keyword for script table
   */
  protected String getTableKeyword() {
    return "script";
  }

  /**
   * Template method to provide the keyword for the {@code start} action.
   *
   * @return keyword for {@code start} action
   */
  protected String getStartKeyword() {
    return "start";
  }

  /**
   * Template method to provide the keyword for the {@code check} action.
   *
   * @return keyword for {@code check} action
   */
  protected String getCheckKeyword() {
    return "check";
  }

  /**
   * Template method to provide the keyword for the {@code checkNot} action.
   *
   * @return keyword for {@code checkNot} action
   */
  protected String getCheckNotKeyword() {
    return "check not";
  }

  /**
   * Template method to provide the keyword for the {@code reject} action.
   *
   * @return keyword for {@code reject} action
   */
  protected String getRejectKeyword() {
    return "reject";
  }

  /**
   * Template method to provide the keyword for the {@code ensure} action.
   *
   * @return keyword for {@code ensure} action
   */
  protected String getEnsureKeyword() {
    return "ensure";
  }

  /**
   * Template method to provide the keyword for the {@code show} action.
   *
   * @return keyword for {@code show} action
   */
  protected String getShowKeyword() {
    return "show";
  }

  /**
   * Template method to provide the keyword for the {@code note} action.
   *
   * @return keyword for {@code note} action
   */
  protected String getNoteKeyword() {
    return "note";
  }

  @Override
  public List<SlimAssertion> getAssertions() throws TestExecutionException {
    List<SlimAssertion> assertions = new ArrayList<>();
    ScenarioTable.setDefaultChildClass(getClass());
    if (table.getCellContents(0, 0).toLowerCase().startsWith(getTableKeyword())) {
      List<SlimAssertion> createAssertions = startActor();
      if (createAssertions != null) {
        assertions.addAll(createAssertions);
      }
    }
    for (int row = 1; row < table.getRowCount(); row++)
      assertions.addAll(instructionsForRow(row));
    return assertions;
  }

  // returns a list of statements
  protected List<SlimAssertion> instructionsForRow(int row) throws TestExecutionException {
    String firstCell = table.getCellContents(0, row).trim();
    List<SlimAssertion> assertions;
    String match;
    if (firstCell.equalsIgnoreCase(getStartKeyword()))
      assertions = startActor(row);
    else if (firstCell.equalsIgnoreCase(getCheckKeyword()))
      assertions = checkAction(row);
    else if (firstCell.equalsIgnoreCase(getCheckNotKeyword()))
      assertions = checkNotAction(row);
    else if (firstCell.equalsIgnoreCase(getRejectKeyword()))
      assertions = reject(row);
    else if (firstCell.equalsIgnoreCase(getEnsureKeyword()))
      assertions = ensure(row);
    else if (firstCell.equalsIgnoreCase(getShowKeyword()))
      assertions = show(row);
    else if (firstCell.equalsIgnoreCase(getNoteKeyword()))
      assertions = note(row);
    else if ((match = ifSymbolAssignment(0, row)) != null)
      assertions = actionAndAssign(match, row);
    else if (firstCell.isEmpty())
      assertions = note(row);
    else if (firstCell.trim().startsWith("#") || firstCell.trim().startsWith("*"))
      assertions = note(row);
    else {
      // action() is for now the only function that returns a list of statements
      assertions = action(row);
    }
    return assertions;
  }

  protected List<SlimAssertion> actionAndAssign(String symbolName, int row) {
    List<SlimAssertion> assertions = new ArrayList<>();
    int lastCol = table.getColumnCountInRow(row) - 1;
    String actionName = getActionNameStartingAt(1, lastCol, row);
    if (!actionName.equals("")) {
      String[] args = getArgumentsStartingAt(1 + 1, lastCol, row, assertions);
      assertions.add(makeAssertion(callAndAssign(symbolName, getTableType() + "Actor", actionName, (Object[]) args),
              new SymbolAssignmentExpectation(symbolName, 0, row)));

    }
    return assertions;
  }

  protected List<SlimAssertion> action(int row) throws TestExecutionException {
    List<SlimAssertion> assertions = assertionsFromScenario(row);
    if (assertions.isEmpty()) {
      // Invoke fixture:
      int lastCol = table.getColumnCountInRow(row) - 1;
      return invokeAction(0, lastCol, row, new ScriptActionExpectation(0, row));
    }
    return assertions;
  }

  protected List<SlimAssertion> assertionsFromScenario(int row) throws TestExecutionException {
    int lastCol = table.getColumnCountInRow(row) - 1;
    String scenarioName = getScenarioNameFromAlternatingCells(lastCol, row);
    ScenarioTable scenario = getTestContext().getScenario(scenarioName);
    String[] args = null;
    List<SlimAssertion> assertions = new ArrayList<>();
    if (scenario != null) {
      args = getArgumentsStartingAt(1, lastCol, row, assertions);
    } else if (lastCol == 0) {
      String cellContents = table.getCellContents(0, row);
      scenario = getTestContext().getScenarioByPattern(cellContents);
      if (scenario != null) {
        args = scenario.matchParameters(cellContents);
      }
    }
    if (scenario != null) {
      scenario.setCustomComparatorRegistry(customComparatorRegistry);
      assertions.addAll(scenario.call(args, this, row));
    }
    return assertions;
  }

  protected String getScenarioNameFromAlternatingCells(int endingCol, int row) {
    String actionName = getActionNameStartingAt(0, endingCol, row);
    String simpleName = actionName.replace(SEQUENTIAL_ARGUMENT_PROCESSING_SUFFIX, "");
    return Disgracer.disgraceClassName(simpleName);
  }

  protected List<SlimAssertion> note(int row) {
    return Collections.emptyList();
  }

  protected List<SlimAssertion> show(int row) {
    int lastCol = table.getColumnCountInRow(row) - 1;
    return invokeAction(1, lastCol, row,
            new ShowActionExpectation(0, row));
  }

  protected List<SlimAssertion> ensure(int row) {
    int lastCol = table.getColumnCountInRow(row) - 1;
    return invokeAction(1, lastCol, row,
            new EnsureActionExpectation(0, row));
  }

  protected List<SlimAssertion> reject(int row) {
    int lastCol = table.getColumnCountInRow(row) - 1;
    return invokeAction(1, lastCol, row,
            new RejectActionExpectation(0, row));

  }

  protected List<SlimAssertion> checkAction(int row) {
    int lastColInAction = table.getColumnCountInRow(row) - 1;
    table.getCellContents(lastColInAction, row);
    return invokeAction(1, lastColInAction - 1, row,
            new ReturnedValueExpectation(lastColInAction, row));
  }

  protected List<SlimAssertion> checkNotAction(int row) {
    int lastColInAction = table.getColumnCountInRow(row) - 1;
    table.getCellContents(lastColInAction, row);
    return invokeAction(1, lastColInAction - 1, row,
            new RejectedValueExpectation(lastColInAction, row));
  }

  protected List<SlimAssertion> invokeAction(int startingCol, int endingCol, int row, SlimExpectation expectation) {
    String actionName = getActionNameStartingAt(startingCol, endingCol, row);
    List<SlimAssertion> assertions = new ArrayList<>();
    String[] args = getArgumentsStartingAt(startingCol + 1, endingCol, row, assertions);
    assertions.add(makeAssertion(callFunction(getTableType() + "Actor", actionName, (Object[]) args),
            expectation));
    return assertions;
  }

  protected String getActionNameStartingAt(int startingCol, int endingCol, int row) {
    StringBuilder actionName = new StringBuilder();
    actionName.append(table.getCellContents(startingCol, row));
    int actionNameCol = startingCol + 2;
    while (actionNameCol <= endingCol &&
    !invokesSequentialArgumentProcessing(actionName.toString())) {
      actionName.append(" ").append(table.getCellContents(actionNameCol, row));
      actionNameCol += 2;
    }
    return actionName.toString().trim();
  }

  // Adds extra assertions to the "assertions" list!
  protected String[] getArgumentsStartingAt(int startingCol, int endingCol, int row, List<SlimAssertion> assertions) {
    ArgumentExtractor extractor = new ArgumentExtractor(startingCol, endingCol, row);
    while (extractor.hasMoreToExtract()) {
      assertions.add(makeAssertion(Instruction.NOOP_INSTRUCTION,
              new ArgumentExpectation(extractor.argumentColumn, row)));
      extractor.extractNextArgument();
    }
    return extractor.getArguments();
  }

  protected boolean invokesSequentialArgumentProcessing(String cellContents) {
    return cellContents.endsWith(SEQUENTIAL_ARGUMENT_PROCESSING_SUFFIX);
  }

  protected List<SlimAssertion> startActor() {
    String firstCellContents = table.getCellContents(0, 0);
    String keyworkd = getTableKeyword() + ":";
    int pos = firstCellContents.toLowerCase().indexOf(keyworkd);
    if (pos == 0) {
      return startActor(0, firstCellContents.substring(keyworkd.length() ), 0);
    } else if (table.getColumnCountInRow(0) > 1) {
      return startActor(0);
    }
    return null;
  }

  protected List<SlimAssertion> startActor(int row) {
    int classNameColumn = 1;
    String cellContents = table.getCellContents(classNameColumn, row);
    return startActor(row, cellContents, classNameColumn);
  }

  protected List<SlimAssertion> startActor(int row, String cellContents, int classNameColumn) {
    List<SlimAssertion> assertions = new ArrayList<>();
    String className = Disgracer.disgraceClassName(cellContents);
    assertions.add(constructInstance(getTableType() + "Actor", className, classNameColumn, row));
    getArgumentsStartingAt(classNameColumn + 1, table.getColumnCountInRow(row) - 1, row, assertions);
    return assertions;
  }

  class ArgumentExtractor {
    private int argumentColumn;
    private int endingCol;
    private int row;

    private List<String> arguments = new ArrayList<>();
    private int increment = 2;
    private boolean sequentialArguments = false;

    ArgumentExtractor(int startingCol, int endingCol, int row) {
      this.argumentColumn = startingCol;
      this.endingCol = endingCol;
      this.row = row;
    }

    public boolean hasMoreToExtract() {
      return argumentColumn <= endingCol;
    }

    public void extractNextArgument() {
      arguments.add(table.getCellContents(argumentColumn, row));
      String argumentKeyword = table.getCellContents(argumentColumn - 1, row);
      boolean argumentIsSequential = invokesSequentialArgumentProcessing(argumentKeyword);
      sequentialArguments = (sequentialArguments || argumentIsSequential);
      increment = sequentialArguments ? 1 : 2;
      argumentColumn += increment;
    }

    public String[] getArguments() {
      return arguments.toArray(new String[arguments.size()]);
    }
  }

  private class ScriptActionExpectation extends RowExpectation {
    private ScriptActionExpectation(int col, int row) {
      super(col, row);
    }

    @Override
    protected SlimTestResult createEvaluationMessage(String actual, String expected) {
      if (actual == null)
        return SlimTestResult.fail("null", expected);
      else if (actual.equals(VoidConverter.VOID_TAG) || actual.equals("null"))
        return SlimTestResult.plain();
      else if (actual.equals(BooleanConverter.FALSE))
        return SlimTestResult.fail();
      else if (actual.equals(BooleanConverter.TRUE))
        return SlimTestResult.pass();
      else
        return SlimTestResult.plain();
    }
  }

  private class EnsureActionExpectation extends RowExpectation {
    public EnsureActionExpectation(int col, int row) {
      super(col, row);
    }

    @Override
    protected SlimTestResult createEvaluationMessage(String actual, String expected) {
      return (actual != null && actual.equals(BooleanConverter.TRUE)) ?
              SlimTestResult.pass() : SlimTestResult.fail();
    }
  }

  private class RejectActionExpectation extends RowExpectation {
    public RejectActionExpectation(int col, int row) {
      super(col, row);
    }

    @Override
    protected SlimTestResult createEvaluationMessage(String actual, String expected) {
      if (actual == null)
        return SlimTestResult.pass();
      else
        return actual.equals(BooleanConverter.FALSE) ? SlimTestResult.pass() : SlimTestResult.fail();
    }
  }

  private class ShowActionExpectation extends RowExpectation {
    public ShowActionExpectation(int col, int row) {
      super(col, row);
    }

    @Override
    protected SlimTestResult createEvaluationMessage(String actual, String expected) {
      try {
        table.addColumnToRow(getRow(), actual);
      } catch (Exception e) {
        return SlimTestResult.fail(actual, e.getMessage());
      }
      return SlimTestResult.plain();
    }
  }

  private class ArgumentExpectation extends RowExpectation {

    private ArgumentExpectation(int col, int row) {
      super(col, row);
    }

    @Override
    public TestResult evaluateExpectation(Object returnValue) {
      table.substitute(getCol(), getRow(), replaceSymbolsWithFullExpansion(getExpected()));
      return null;
    }

    @Override
    protected SlimTestResult createEvaluationMessage(String actual, String expected) {
      return null;
    }
  }
}
