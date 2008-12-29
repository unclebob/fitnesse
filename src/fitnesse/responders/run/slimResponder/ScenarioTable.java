package fitnesse.responders.run.slimResponder;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScenarioTable extends SlimTable {
  private static final String instancePrefix = "scenarioTable";
  private String name;
  private Set<String> inputs = new HashSet<String>();
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
    return inputs;
  }

  public Set<String> getOutputs() {
    return outputs;
  }
}
