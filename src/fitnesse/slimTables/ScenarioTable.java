// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slimTables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.StringUtil;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.slimResponder.SlimTestContext;
import fitnesse.slim.SlimError;

public class ScenarioTable extends SlimTable {
  private static final String instancePrefix = "scenarioTable";
  private String name;
  private List<String> inputs = new ArrayList<String>();
  private Set<String> outputs = new HashSet<String>();
  private final int colsInHeader = table.getColumnCountInRow(0);

  public ScenarioTable(Table table, String tableId, SlimTestContext testContext) {
    super(table, tableId, testContext);
  }

  protected String getTableType() {
    return instancePrefix;
  }

  public void appendInstructions() {
    parseTable();
  }

  private void parseTable() {
    validateHeader();
    name = getScenarioName();
    getTestContext().addScenario(name, this);
    getScenarioArguments();

  }

  private void getScenarioArguments() {
    for (int inputCol = 2; inputCol < colsInHeader; inputCol += 2) {
      String argName = table.getCellContents(inputCol, 0);
      if (argName.endsWith("?")) {
        String disgracedArgName = Disgracer.disgraceMethodName(argName.substring(0, argName.length()));
        outputs.add(disgracedArgName);
      } else {
        String disgracedArgName = Disgracer.disgraceMethodName(argName);
        inputs.add(disgracedArgName);
      }
    }
  }

  private String getScenarioName() {
    StringBuffer nameBuffer = new StringBuffer();
    for (int nameCol = 1; nameCol < colsInHeader; nameCol += 2)
      nameBuffer.append(table.getCellContents(nameCol, 0)).append(" ");
    return Disgracer.disgraceClassName(nameBuffer.toString().trim());
  }

  private void validateHeader() {
    if (colsInHeader <= 1)
      throw new SyntaxError("Scenario tables must have a name.");
  }

  public void evaluateReturnValues(Map<String, Object> returnValues) throws Exception {
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

  public void call(Map<String, String> scenarioArguments, SlimTable parentTable, int row) {
    String script = getTable().toHtml();
    script = replaceArgsInScriptTable(script, scenarioArguments);
    insertAndProcessScript(script, parentTable, row);
  }

  public void call(String[] args, ScriptTable parentTable, int row) {
    Map<String, String> scenarioArguments = new HashMap<String, String>();
    for (int i = 0; i < inputs.size() && i < args.length; i++)
      scenarioArguments.put(inputs.get(i), args[i]);
    call(scenarioArguments, parentTable, row);
  }

  private void insertAndProcessScript(String script, SlimTable parentTable, int row) {
    try {
      TableScanner ts = new HtmlTableScanner(script);
      ScriptTable t = new ScriptTable(ts.getTable(0), id, parentTable.getTestContext());
      parentTable.addChildTable(t, row);
      t.appendInstructions(parentTable.instructions);
      parentTable.addExpectation(new ScenarioExpectation(t, row));
    } catch (Exception e) {
      throw new SlimError(e);
    }
  }

  private String replaceArgsInScriptTable(String script, Map<String, String> scenarioArguments) {
    for (String arg : scenarioArguments.keySet()) {
      if (getInputs().contains(arg)) {
        String argument = scenarioArguments.get(arg);
        String replacement = replaceSymbols(argument);
        script = StringUtil.replaceAll(script, "@" + arg, replacement);
      } else {
        throw new SyntaxError(String.format("The argument %s is not an input to the scenario.", arg));
      }
    }
    return script;
  }

  private class ScenarioExpectation extends Expectation {
    private ScriptTable scriptTable;

    private ScenarioExpectation(ScriptTable scriptTable, int row) {
      super("", -1, row);  // We don't care about anything but the row.
      this.scriptTable = scriptTable;
    }

    public void evaluateExpectation(Map<String, Object> returnValues) {
      TestSummary counts = scriptTable.getTestSummary();
      boolean testStatus = (counts.getWrong() + counts.getExceptions()) == 0;
      SlimTable parent = scriptTable.getParent();
      parent.getTable().setTestStatusOnRow(getRow(), testStatus);
      parent.getTestSummary().add(scriptTable.getTestSummary());
    }

    protected String createEvaluationMessage(String actual, String expected) {
      return null;
    }
  }
}
