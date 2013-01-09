// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slimTables;

import static util.ListUtility.list;
import fitnesse.responders.run.slimResponder.SlimTestContext;
import fitnesse.responders.run.slimResponder.SlimTestSystem;
import fitnesse.slim.converters.BooleanConverter;
import fitnesse.slim.converters.VoidConverter;
import fitnesse.wikitext.Utils;

import java.util.*;
import java.util.regex.Matcher;

public class ScriptTable extends SlimTable {
  private static final String SEQUENTIAL_ARGUMENT_PROCESSING_SUFFIX = ";";
  private Matcher symbolAssignmentMatcher;

  public ScriptTable(Table table, String tableId, SlimTestContext context) {
    super(table, tableId, context);
  }

  protected String getTableType() {
    return "scriptTable";
  }

  public List<Object> getInstructions() {
    int rows = table.getRowCount();
    List<Object> instructions = new ArrayList<Object>();
    if (isScript() && table.getColumnCountInRow(0) > 1)
      instructions.add(startActor(0));
    for (int row = 1; row < rows; row++)
      instructions.addAll(instructionForRow(row));
    return instructions;
  }

  private boolean isScript() {
    return "script".equalsIgnoreCase(table.getCellContents(0, 0));
  }

  // returns a list of statements
  private List<Object> instructionForRow(int row) {
    String firstCell = table.getCellContents(0, row).trim();
    List<Object> instruction;
    if (firstCell.equalsIgnoreCase("start"))
      instruction = startActor(row);
    else if (firstCell.equalsIgnoreCase("check"))
      instruction = checkAction(row);
    else if (firstCell.equalsIgnoreCase("check not"))
      instruction = checkNotAction(row);
    else if (firstCell.equalsIgnoreCase("reject"))
      instruction = reject(row);
    else if (firstCell.equalsIgnoreCase("ensure"))
      instruction = ensure(row);
    else if (firstCell.equalsIgnoreCase("show"))
      instruction = show(row);
    else if (firstCell.equalsIgnoreCase("note"))
      instruction = note(row);
    else if (isSymbolAssignment(firstCell))
      instruction = actionAndAssign(row);
    else if (firstCell.length() == 0)
      instruction = note(row);
    else if (firstCell.trim().startsWith("#") || firstCell.trim().startsWith("*"))
      instruction = note(row);
    else {
      // action() is for now the only function that returns a list of statements
      return action(row);
    }
    return instruction.isEmpty() ? instruction : list(instruction);
  }

  private List<Object> actionAndAssign(int row) {
    int lastCol = table.getColumnCountInRow(row) - 1;
    String symbolName = symbolAssignmentMatcher.group(1);
    addExpectation(new SymbolAssignmentExpectation(symbolName, getInstructionTag(), 0, row));
    String actionName = getActionNameStartingAt(1, lastCol, row);
    if (!actionName.equals("")) {
      String[] args = getArgumentsStartingAt(1 + 1, lastCol, row);
      return callAndAssign(symbolName, "scriptTableActor", actionName, args);
    }
    return Collections.emptyList();
  }

  private boolean isSymbolAssignment(String firstCell) {
    symbolAssignmentMatcher = symbolAssignmentPattern.matcher(firstCell);
    return symbolAssignmentMatcher.matches();
  }

  private List<Object> action(int row) {
    int lastCol = table.getColumnCountInRow(row) - 1;
    String actionName = getActionNameStartingAt(0, lastCol, row);
    String[] args = getArgumentsStartingAt(0 + 1, lastCol, row);
    ScenarioTable scenario = getTestContext().getScenario(Disgracer.disgraceClassName(actionName));
    if (scenario != null) {
      return scenario.call(args, this, row);
    } else {
      List<Object> instructions = invokeParameterizedScenarioIfPossible(row);
      if (instructions.isEmpty()) {
        addExpectation(new ScriptActionExpectation(getInstructionTag(), 0, row));
        return list(callFunction("scriptTableActor", actionName, (Object[]) args));
      }
      return instructions;
    }
  }

  private List<Object> invokeParameterizedScenarioIfPossible(int row) {
    if (table.getColumnCountInRow(row) == 1){
      String firstNameCell = table.getCellContents(0, row);
      for (ScenarioTable scenario : getScenariosWithMostArgumentsFirst()) {
        String[] arguments = scenario.matchParameters(firstNameCell);
        if (arguments != null) {
          return scenario.call(arguments, this, row);
        }
      }
    }
    return Collections.emptyList();
  }

  private List<ScenarioTable> getScenariosWithMostArgumentsFirst() {
    Map<String, ScenarioTable> scenarioMap = getTestContext().getScenarios();
    List<ScenarioTable> scenarios = new ArrayList<ScenarioTable>(scenarioMap.values());
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


  private List<Object> note(int row) {
    return Collections.emptyList();
  }

  private List<Object> show(int row) {
    int lastCol = table.getColumnCountInRow(row) - 1;
    addExpectation(new ShowActionExpectation(getInstructionTag(), 0, row));
    return invokeAction(1, lastCol, row);
  }

  private List<Object> ensure(int row) {
    addExpectation(new EnsureActionExpectation(getInstructionTag(), 0, row));
    int lastCol = table.getColumnCountInRow(row) - 1;
    return invokeAction(1, lastCol, row);
  }

  private List<Object> reject(int row) {
    addExpectation(new RejectActionExpectation(getInstructionTag(), 0, row));
    int lastCol = table.getColumnCountInRow(row) - 1;
    return invokeAction(1, lastCol, row);
  }

  private List<Object> checkAction(int row) {
    int lastColInAction = table.getColumnCountInRow(row) - 1;
    table.getCellContents(lastColInAction, row);
    addExpectation(new ReturnedValueExpectation(getInstructionTag(), lastColInAction, row));
    return invokeAction(1, lastColInAction - 1, row);
  }

  private List<Object> checkNotAction(int row) {
    int lastColInAction = table.getColumnCountInRow(row) - 1;
    table.getCellContents(lastColInAction, row);
    addExpectation(new RejectedValueExpectation(getInstructionTag(), lastColInAction, row));
    return invokeAction(1, lastColInAction - 1, row);
  }

  private List<Object> invokeAction(int startingCol, int endingCol, int row) {
    String actionName = getActionNameStartingAt(startingCol, endingCol, row);
    String[] args = getArgumentsStartingAt(startingCol + 1, endingCol, row);
    return callFunction("scriptTableActor", actionName, (Object[]) args);
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

  private String[] getArgumentsStartingAt(int startingCol, int endingCol, int row) {
    ArgumentExtractor extractor = new ArgumentExtractor(startingCol, endingCol, row);
    while (extractor.hasMoreToExtract()) {
      addExpectation(new ArgumentExpectation(getInstructionTag(), extractor.argumentColumn, row));
      extractor.extractNextArgument();
    }
    return extractor.getArguments();
  }

  private boolean invokesSequentialArgumentProcessing(String cellContents) {
    return cellContents.endsWith(SEQUENTIAL_ARGUMENT_PROCESSING_SUFFIX);
  }

  private List<Object> startActor(int row) {
    int classNameColumn = 1;
    String cellContents = table.getCellContents(classNameColumn, row);
    String className = Disgracer.disgraceClassName(cellContents);
    return constructInstance("scriptTableActor", className, classNameColumn, row);
  }

  public void evaluateReturnValues(Map<String, Object> returnValues) throws Exception {
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

  private class ScriptActionExpectation extends Expectation {
    private ScriptActionExpectation(String instructionTag, int col, int row) {
      super(instructionTag, col, row);
    }

    protected String createEvaluationMessage(String actual, String expected) {
      if (actual == null)
        return failMessage(expected, "Returned null value.");
      else if (actual.equals(VoidConverter.VOID_TAG) || actual.equals("null"))
        return expected;
      else if (actual.equals(BooleanConverter.FALSE))
        return fail(expected);
      else if (actual.equals(BooleanConverter.TRUE))
        return pass(expected);
      else
        return expected;
    }
  }

  private class EnsureActionExpectation extends Expectation {
    public EnsureActionExpectation(String instructionTag, int col, int row) {
      super(instructionTag, col, row);
    }

    protected String createEvaluationMessage(String actual, String expected) {
      return (actual != null && actual.equals(BooleanConverter.TRUE)) ?
        pass(expected) : fail(expected);
    }
  }

  private class RejectActionExpectation extends Expectation {
    public RejectActionExpectation(String instructionTag, int col, int row) {
      super(instructionTag, col, row);
    }

    protected String createEvaluationMessage(String actual, String expected) {
      if (actual == null)
        return pass(expected);
      else
        return actual.equals(BooleanConverter.FALSE) ? pass(expected) : fail(expected);
    }
  }

  private class ShowActionExpectation extends Expectation {
    public ShowActionExpectation(String instructionTag, int col, int row) {
      super(instructionTag, col, row);
    }

    protected String createEvaluationMessage(String actual, String expected) {
      try {
        table.appendCellToRow(getRow(), Utils.escapeHTML(actual));
      } catch (Throwable e) {
        return failMessage(actual, SlimTestSystem.exceptionToString(e));
      }
      return expected;
    }
  }

  private class ArgumentExpectation extends Expectation {
    private ArgumentExpectation(String instructionTag, int col, int row) {
      super(instructionTag, col, row);
    }

    public void evaluateExpectation(Map<String, Object> returnValues) {
      String originalContent = table.getCellContents(getCol(), getRow());
      table.setCell(getCol(), getRow(), replaceSymbolsWithFullExpansion(originalContent));
    }

    protected String createEvaluationMessage(String actual, String expected) {
      return null;
    }
  }
}
