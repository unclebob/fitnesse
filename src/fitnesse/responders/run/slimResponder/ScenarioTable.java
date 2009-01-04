package fitnesse.responders.run.slimResponder;

import fitnesse.responders.run.TestSummary;
import fitnesse.slim.SlimError;

import java.util.*;

public class ScenarioTable extends SlimTable {
  private static final String instancePrefix = "scenarioTable";
  private String name;
  private List<String> inputs = new ArrayList<String>();
  private Set<String> outputs = new HashSet<String>();
  private final int colsInHeader = table.getColumnCountInRow(0);

  public ScenarioTable(Table table, String tableId, SlimTestContext testContext) {
    super(table, tableId, testContext);
  }

  public ScenarioTable(Table t, String id) {
    super(t, id);
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
    testContext.addScenario(name, this);
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

  protected void evaluateReturnValues(Map<String, Object> returnValues) throws Exception {
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
    for (int i = 0; i < inputs.size(); i++)
      scenarioArguments.put(inputs.get(i), args[i]);
    call(scenarioArguments, parentTable, row);
  }

  private void insertAndProcessScript(String script, SlimTable parentTable, int row) {
    try {
      TableScanner ts = new HtmlTableScanner(script);
      ScriptTable t = new ScriptTable(ts.getTable(0), parentTable.id, parentTable.testContext);
      parentTable.addChildTable(t, row);
      parentTable.addExpectation(new ScenarioExpectation(t, row));
      t.appendInstructions(parentTable.instructions);
    } catch (Exception e) {
      throw new SlimError(e);
    }
  }

  private String replaceArgsInScriptTable(String script, Map<String, String> scenarioArguments) {
    for (String arg : scenarioArguments.keySet()) {
      if (getInputs().contains(arg)) {
        script = script.replaceAll("@" + arg, scenarioArguments.get(arg));
      } else {
        throw new SyntaxError(String.format("The argument %s is not an input to the scenario.", arg));
      }
    }
    return script;
  }

  private class ScenarioExpectation extends Expectation {
    private ScriptTable scriptTable;

    private ScenarioExpectation(ScriptTable scriptTable, int row) {
      super(null, "", -1, row);  // We don't care about anything but the row.
      this.scriptTable = scriptTable;
    }

    protected void evaluateExpectation(Map<String, Object> returnValues) {
      TestSummary counts = scriptTable.getTestSummary();
      boolean testStatus = (counts.wrong + counts.exceptions) == 0;
      SlimTable parent = scriptTable.getParent();
      parent.getTable().setTestStatusOnRow(row, testStatus);
      parent.getTestSummary().add(scriptTable.getTestSummary());
    }

    protected String createEvaluationMessage(String value, String originalValue) {
      return null;
    }
  }
}
