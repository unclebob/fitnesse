package fitnesse.responders.run.slimResponder;

import fitnesse.slim.SlimClient;
import static fitnesse.util.ListUtility.list;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScenarioAndDecisionTableTest implements SlimTestContext {
  private WikiPage root;
  private List<Object> instructions;
  private ScenarioTable st;
  private DecisionTable dt;
  private Map<String, String> symbols = new HashMap<String, String>();
  private Map<String, ScenarioTable> scenarios = new HashMap<String, ScenarioTable>();

  public String getSymbol(String symbolName) {
    return symbols.get(symbolName);
  }

  public void setSymbol(String symbolName, String value) {
    symbols.put(symbolName, value);
  }

  public void addScenario(String scenarioName, ScenarioTable scenarioTable) {
    scenarios.put(scenarioName, scenarioTable);
  }

  public ScenarioTable getScenario(String scenarioName) {
    return scenarios.get(scenarioName);
  }

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    instructions = new ArrayList<Object>();
  }

  private void makeTables(String tableText) throws Exception {
    WikiPageUtil.setPageContents(root, tableText);
    TableScanner ts = new HtmlTableScanner(root.getData().getHtml());
    Table t = ts.getTable(0);
    st = new ScenarioTable(t, "s_id", this);
    t = ts.getTable(1);
    dt = new DecisionTable(t, "did", this);
    st.appendInstructions(instructions);
    dt.appendInstructions(instructions);
  }

  @Test
  public void oneInput() throws Exception {
    makeTables(
      "!|scenario|myScenario|input|\n" +
        "|function|@input|\n" +
        "\n" +
        "!|DT:myScenario|\n" +
        "|input|\n" +
        "|7|\n"
    );
    List<Object> expectedInstructions =
      list(
        list("scriptTable_did.0_0", "call", "scriptTableActor", "function", "7")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void manyInputsAndRows() throws Exception {
    makeTables(
      "!|scenario|login|user name|password|password|pin|pin|\n" +
        "|login|@userName|password|@password|pin|@pin|\n" +
        "\n" +
        "!|DT:LoginPasswordPin|\n" +
        "|user name|password|pin|\n" +
        "|bob|xyzzy|7734|\n" +
        "|bill|yabba|8892|\n"
    );
    List<Object> expectedInstructions =
      list(
        list("scriptTable_did.0_0", "call", "scriptTableActor", "loginPasswordPin", "bob", "xyzzy", "7734"),
        list("scriptTable_did.1_0", "call", "scriptTableActor", "loginPasswordPin", "bill", "yabba", "8892")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void simpleInputAndOutput() throws Exception {
    makeTables(
      "!|scenario|echo|input|giving|output|\n" +
        "|check|echo|@input|@output|\n" +
        "\n" +
        "!|DT:EchoGiving|\n" +
        "|input|output|\n" +
        "|7|7|\n"
    );
    List<Object> expectedInstructions =
      list(
        list("scriptTable_did.0_0", "call", "scriptTableActor", "echo", "7")
      );
    assertEquals(expectedInstructions, instructions);
    Map<String, Object> pseudoResults = SlimClient.resultToMap(
      list(
        list("scriptTable_did.0_0", "7")
      )
    );
    dt.evaluateExpectations(pseudoResults);

    String scriptTable = dt.getChild(0).getTable().toString();
    String expectedScript =
      "[[scenario, echo, input, giving, output], [check, echo, 7, pass(7)]]";
    assertEquals(expectedScript, scriptTable);
  }
}
