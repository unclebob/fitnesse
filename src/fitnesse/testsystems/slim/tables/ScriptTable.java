// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import fitnesse.slim.converters.BooleanConverter;
import fitnesse.slim.converters.VoidConverter;
import fitnesse.slim.instructions.Instruction;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.SlimTestSystem;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.results.TestResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static util.ListUtility.list;

public class ScriptTable extends SlimTable {
  private static final String SEQUENTIAL_ARGUMENT_PROCESSING_SUFFIX = ";";

  public ScriptTable(Table table, String tableId, SlimTestContext context) {
    super(table, tableId, context);
  }

  protected String getTableType() {
    return "scriptTable";
  }

  public List<Assertion> getAssertions() throws SyntaxError {
    int rows = table.getRowCount();
    List<Assertion> assertions = new ArrayList<Assertion>();
    if (isScript() && table.getColumnCountInRow(0) > 1)
      assertions.addAll(startActor(0));
    for (int row = 1; row < rows; row++)
      assertions.addAll(instructionsForRow(row));
    return assertions;
  }

  private boolean isScript() {
    return "script".equalsIgnoreCase(table.getCellContents(0, 0));
  }

  // returns a list of statements
  private List<Assertion> instructionsForRow(int row) throws SyntaxError {
    String firstCell = table.getCellContents(0, row).trim();
    List<Assertion> assertions;
    String match;
    if (firstCell.equalsIgnoreCase("start"))
      assertions = startActor(row);
    else if (firstCell.equalsIgnoreCase("check"))
      assertions = checkAction(row);
    else if (firstCell.equalsIgnoreCase("check not"))
      assertions = checkNotAction(row);
    else if (firstCell.equalsIgnoreCase("reject"))
      assertions = reject(row);
    else if (firstCell.equalsIgnoreCase("ensure"))
      assertions = ensure(row);
    else if (firstCell.equalsIgnoreCase("show"))
      assertions = show(row);
    else if (firstCell.equalsIgnoreCase("note"))
      assertions = note(row);
    else if ((match = ifSymbolAssignment(0, row)) != null)
      assertions = actionAndAssign(match, row);
    else if (firstCell.length() == 0)
      assertions = note(row);
    else if (firstCell.trim().startsWith("#") || firstCell.trim().startsWith("*"))
      assertions = note(row);
    else {
      // action() is for now the only function that returns a list of statements
      assertions = action(row);
    }
    return assertions;
  }

  private List<Assertion> actionAndAssign(String symbolName, int row) {
    List<Assertion> assertions = new ArrayList<Assertion>();
    int lastCol = table.getColumnCountInRow(row) - 1;
    String actionName = getActionNameStartingAt(1, lastCol, row);
    if (!actionName.equals("")) {
      String[] args = getArgumentsStartingAt(1 + 1, lastCol, row, assertions);
      assertions.add(makeAssertion(callAndAssign(symbolName, "scriptTableActor", actionName, args),
              new SymbolAssignmentExpectation(symbolName, 0, row)));

    }
    return assertions;
  }

  private List<Assertion> action(int row) throws SyntaxError {
    List<Assertion> assertions = assertionsFromScenario(row);
    if (assertions.isEmpty()) {
      // Invoke fixture:
      int lastCol = table.getColumnCountInRow(row) - 1;
      String actionName = getActionNameStartingAt(0, lastCol, row);
      String[] args = getArgumentsStartingAt(1, lastCol, row, assertions);
      assertions.add(makeAssertion(callFunction("scriptTableActor", actionName, (Object[]) args),
              new ScriptActionExpectation(0, row)));
    }
    return assertions;
  }

  private List<Assertion> assertionsFromScenario(int row) throws SyntaxError {
    int lastCol = table.getColumnCountInRow(row) - 1;
    String actionName = getActionNameStartingAt(0, lastCol, row);
    ScenarioTable scenario = getTestContext().getScenario(Disgracer.disgraceClassName(actionName));
    List<Assertion> assertions = new ArrayList<Assertion>();
    if (scenario != null) {
      String[] args = getArgumentsStartingAt(1, lastCol, row, assertions);
      assertions.addAll(scenario.call(args, this, row));
    } else if (lastCol == 0) {
      String firstNameCell = table.getCellContents(0, row);
      for (ScenarioTable s : getScenariosWithMostArgumentsFirst()) {
        String[] args = s.matchParameters(firstNameCell);
        if (args != null) {
          assertions.addAll(s.call(args, this, row));
          break;
        }
      }
    }
    return assertions;
  }

  private List<ScenarioTable> getScenariosWithMostArgumentsFirst() {
    Collection<ScenarioTable> scenarioMap = getTestContext().getScenarios();
    List<ScenarioTable> scenarios = new ArrayList<ScenarioTable>(scenarioMap);
    Collections.sort(scenarios, new ScenarioTableLengthComparator());
    return scenarios;
  }

  private static class ScenarioTableLengthComparator implements java.util.Comparator<ScenarioTable> {
    public int compare(ScenarioTable st1, ScenarioTable st2) {
      int size1 = st1.getInputs().size();
      int size2 = st2.getInputs().size();
      return size2 - size1;
    }
  }


  private List<Assertion> note(int row) {
    return Collections.emptyList();
  }

  private List<Assertion> show(int row) {
    int lastCol = table.getColumnCountInRow(row) - 1;
    return invokeAction(1, lastCol, row,
            new ShowActionExpectation(0, row));
  }

  private List<Assertion> ensure(int row) {
    int lastCol = table.getColumnCountInRow(row) - 1;
    return invokeAction(1, lastCol, row,
            new EnsureActionExpectation(0, row));
  }

  private List<Assertion> reject(int row) {
    int lastCol = table.getColumnCountInRow(row) - 1;
    return invokeAction(1, lastCol, row,
            new RejectActionExpectation(0, row));

  }

  private List<Assertion> checkAction(int row) {
    int lastColInAction = table.getColumnCountInRow(row) - 1;
    table.getCellContents(lastColInAction, row);
    return invokeAction(1, lastColInAction - 1, row,
            new ReturnedValueExpectation(lastColInAction, row));
  }

  private List<Assertion> checkNotAction(int row) {
    int lastColInAction = table.getColumnCountInRow(row) - 1;
    table.getCellContents(lastColInAction, row);
    return invokeAction(1, lastColInAction - 1, row,
            new RejectedValueExpectation(lastColInAction, row));
  }

  private List<Assertion> invokeAction(int startingCol, int endingCol, int row, Expectation expectation) {
    String actionName = getActionNameStartingAt(startingCol, endingCol, row);
    List<Assertion> assertions = new ArrayList<Assertion>();
    String[] args = getArgumentsStartingAt(startingCol + 1, endingCol, row, assertions);
    assertions.add(makeAssertion(callFunction("scriptTableActor", actionName, (Object[]) args),
            expectation));
    return assertions;
  }

  private String getActionNameStartingAt(int startingCol, int endingCol, int row) {
    StringBuffer actionName = new StringBuffer();
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
  private String[] getArgumentsStartingAt(int startingCol, int endingCol, int row, List<Assertion> assertions) {
    ArgumentExtractor extractor = new ArgumentExtractor(startingCol, endingCol, row);
    while (extractor.hasMoreToExtract()) {
      assertions.add(makeAssertion(Instruction.NOOP_INSTRUCTION,
              new ArgumentExpectation(extractor.argumentColumn, row)));
      extractor.extractNextArgument();
    }
    return extractor.getArguments();
  }

  private boolean invokesSequentialArgumentProcessing(String cellContents) {
    return cellContents.endsWith(SEQUENTIAL_ARGUMENT_PROCESSING_SUFFIX);
  }

  private List<Assertion> startActor(int row) {
    int classNameColumn = 1;
    String cellContents = table.getCellContents(classNameColumn, row);
    String className = Disgracer.disgraceClassName(cellContents);
    return list(constructInstance("scriptTableActor", className, classNameColumn, row));
  }

  class ArgumentExtractor {
    private int argumentColumn;
    private int endingCol;
    private int row;

    private List<String> arguments = new ArrayList<String>();
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
      arguments.add(table.getUnescapedCellContents(argumentColumn, row));
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
    protected TestResult createEvaluationMessage(String actual, String expected) {
      if (actual == null)
        return TestResult.fail("null", expected);
      else if (actual.equals(VoidConverter.VOID_TAG) || actual.equals("null"))
        return TestResult.plain();
      else if (actual.equals(BooleanConverter.FALSE))
        return TestResult.fail();
      else if (actual.equals(BooleanConverter.TRUE))
        return TestResult.pass();
      else
        return TestResult.plain();
    }
  }

  private class EnsureActionExpectation extends RowExpectation {
    public EnsureActionExpectation(int col, int row) {
      super(col, row);
    }

    @Override
    protected TestResult createEvaluationMessage(String actual, String expected) {
      return (actual != null && actual.equals(BooleanConverter.TRUE)) ?
              TestResult.pass() : TestResult.fail();
    }
  }

  private class RejectActionExpectation extends RowExpectation {
    public RejectActionExpectation(int col, int row) {
      super(col, row);
    }

    @Override
    protected TestResult createEvaluationMessage(String actual, String expected) {
      if (actual == null)
        return TestResult.pass();
      else
        return actual.equals(BooleanConverter.FALSE) ? TestResult.pass() : TestResult.fail();
    }
  }

  private class ShowActionExpectation extends RowExpectation {
    public ShowActionExpectation(int col, int row) {
      super(col, row);
    }

    @Override
    protected TestResult createEvaluationMessage(String actual, String expected) {
      try {
        table.addColumnToRow(getRow(), actual);
      } catch (Throwable e) {
        return TestResult.fail(actual, SlimTestSystem.exceptionToString(e));
      }
      return TestResult.plain();
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
    protected TestResult createEvaluationMessage(String actual, String expected) {
      return null;
    }
  }
}
