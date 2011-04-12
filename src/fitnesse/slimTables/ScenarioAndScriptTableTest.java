// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slimTables;

import fitnesse.responders.run.slimResponder.MockSlimTestContext;
import fitnesse.slim.SlimClient;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static util.ListUtility.list;
import static util.RegexTestCase.assertSubString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScenarioAndScriptTableTest extends MockSlimTestContext {
  private WikiPage root;
  private List<Object> instructions;
  private ScenarioTable st;
  private ScriptTable script;

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
    script = new ScriptTable(t, "id", this);
    st.appendInstructions(instructions);
    script.appendInstructions(instructions);
  }

  @Test
  public void oneInput() throws Exception {
    makeTables(
      "!|scenario|myScenario|input|\n" +
        "|function|@input|\n" +
        "\n" +
        "!|script|\n" +
        "|myScenario|7|\n"
    );
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0/scriptTable_s_id_0", "call", "scriptTableActor", "function", "7")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void simpleNameWithUnnamedArgument() throws Exception {
    makeTables(
      "!|scenario|f|a||b|\n" +
        "|function|@a||@b|\n" +
        "\n" +
        "!|script|\n" +
        "|f|1||2|\n"
    );
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0/scriptTable_s_id_0", "call", "scriptTableActor", "function", "1", "2")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void manyInputsAndRows() throws Exception {
    makeTables(
      "!|scenario|login|user name|password|password|pin|pin|\n" +
        "|login|@userName|with password|@password|and pin|@pin|\n" +
        "\n" +
        "!|script|\n" +
        "|login|bob|password|xyzzy|pin|7734|\n" +
        "|login|bill|password|yabba|pin|8892|\n"
    );
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0/scriptTable_s_id_0", "call", "scriptTableActor", "loginWithPasswordAndPin", "bob", "xyzzy", "7734"),
        list("scriptTable_id_1/scriptTable_s_id_0", "call", "scriptTableActor", "loginWithPasswordAndPin", "bill", "yabba", "8892")
      );
    assertEquals(expectedInstructions, instructions);
  }


  @Test
  public void simpleInputAndOutputPassing() throws Exception {
    makeTables(
      "!|scenario|echo|input|giving|output|\n" +
        "|check|echo|@input|@output|\n" +
        "\n" +
        "!|script|\n" +
        "|echo|7|giving|7|\n"
    );
    Map<String, Object> pseudoResults = SlimClient.resultToMap(
      list(
        list("scriptTable_id_0/scriptTable_s_id_0", "7")
      )
    );

    evaluateExpectations(pseudoResults);

    String scriptTable = script.getChild(0).getTable().toString();
    String expectedScript =
      "[[scenario, echo, input, giving, output], [check, echo, 7, pass(7)]]";
    assertEquals(expectedScript, scriptTable);
    String dtHtml = script.getTable().toString();
    assertSubString("<span id=\"test_status\" class=pass>Scenario</span>", dtHtml);
    assertEquals(1, script.getTestSummary().getRight());
    assertEquals(0, script.getTestSummary().getWrong());
    assertEquals(0, script.getTestSummary().getIgnores());
    assertEquals(0, script.getTestSummary().getExceptions());
  }

  @Test
  public void simpleInputAndOutputFailing() throws Exception {
    makeTables(
      "!|scenario|echo|input|giving|output|\n" +
        "|check|echo|@input|@output|\n" +
        "\n" +
        "!|script|\n" +
        "|echo|7|giving|8|\n"
    );
    Map<String, Object> pseudoResults = SlimClient.resultToMap(
      list(
        list("scriptTable_id_0/scriptTable_s_id_0", "7")
      )
    );
    evaluateExpectations(pseudoResults);

    String scriptTable = script.getChild(0).getTable().toString();
    String expectedScript =
      "[[scenario, echo, input, giving, output], [check, echo, 7, [7] fail(expected [8])]]";
    assertEquals(expectedScript, scriptTable);
    String dtHtml = script.getTable().toString();
    assertSubString("<span id=\"test_status\" class=fail>Scenario</span>", dtHtml);
    assertEquals(0, script.getTestSummary().getRight());
    assertEquals(1, script.getTestSummary().getWrong());
    assertEquals(0, script.getTestSummary().getIgnores());
    assertEquals(0, script.getTestSummary().getExceptions());
  }

  @Test
  public void inputAndOutputWithSymbol() throws Exception {
    makeTables(
      "!|scenario|echo|input|giving|output|\n" +
        "|check|echo|@input|@output|\n" +
        "\n" +
        "!|script|\n" +
        "|$V=|echo|7|\n" +
        "|echo|$V|giving|$V|\n"
    );
    Map<String, Object> pseudoResults = SlimClient.resultToMap(
      list(
        list("scriptTable_id_0", "7"),
        list("scriptTable_id_1/scriptTable_s_id_0", "7")
      )
    );

    evaluateExpectations(pseudoResults);

    String scriptTable = script.getChild(0).getTable().toString();
    String expectedScript =
      "[[scenario, echo, input, giving, output], [check, echo, $V->[7], pass($V->[7])]]";
    assertEquals(expectedScript, scriptTable);
  }

  @Test
  public void scenarioHasTooFewArguments() throws Exception {
    makeTables(
      "!|scenario|echo|input|giving|\n" +
        "|check|echo|@input|@output|\n" +
        "\n" +
        "!|script|\n" +
        "|echo|7|giving|7|\n"
    );
    Map<String, Object> pseudoResults = SlimClient.resultToMap(
      list(
        list("scriptTable_id_0/scriptTable_s_id_0", "7")
      )
    );

    evaluateExpectations(pseudoResults);

    String scriptTable = script.getChild(0).getTable().toString();
    String expectedScript =
      "[[scenario, echo, input, giving], [check, echo, 7, [7] fail(expected [@output])]]";
    assertEquals(expectedScript, scriptTable);
  }

  @Test
  public void scenarioExtraArgumentsAreIgnored() throws Exception {
    makeTables(
      "!|scenario|echo|input|giving|output||output2|\n" +
        "|check|echo|@input|@output|\n" +
        "\n" +
        "!|script|\n" +
        "|echo|7|giving|7|\n"
    );
    Map<String, Object> pseudoResults = SlimClient.resultToMap(
      list(
        list("scriptTable_id_0/scriptTable_s_id_0", "7")
      )
    );

    evaluateExpectations(pseudoResults);

    String scriptTable = script.getChild(0).getTable().toString();
    String expectedScript =
      "[[scenario, echo, input, giving, output, , output2], [check, echo, 7, pass(7)]]";
    assertEquals(expectedScript, scriptTable);
    String dtHtml = script.getTable().toString();
    assertSubString("<span id=\"test_status\" class=pass>Scenario</span>", dtHtml);
    assertEquals(1, script.getTestSummary().getRight());
    assertEquals(0, script.getTestSummary().getWrong());
    assertEquals(0, script.getTestSummary().getIgnores());
    assertEquals(0, script.getTestSummary().getExceptions());
  }

  @Test
  public void callParameterizedScenario() throws Exception {
    makeTables(
      "!|scenario|Login user _ with password _|name,password|\n" +
        "|login|@name|with|@password|\n" +
        "\n" +
        "!|script|\n" +
        "|Login user Bob with password xyzzy|\n"
    );
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0/scriptTable_s_id_0", "call", "scriptTableActor", "loginWith", "Bob", "xyzzy")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void callNormalScenarioAsThoughItWereParameterized() throws Exception {
    makeTables(
      "!|scenario|Login user|name|with password|password|\n" +
        "|login|@name|with|@password|\n" +
        "\n" +
        "!|script|\n" +
        "|Login user Bob with password xyzzy|\n"
    );
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0/scriptTable_s_id_0", "call", "scriptTableActor", "loginWith", "Bob", "xyzzy")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void matchesScenarioWithMostArguments() throws Exception {
    WikiPageUtil.setPageContents(root, "" +
        "!|scenario|Login user|name|\n" +
        "|should not get here|\n" +
        "\n" +
        "!|scenario|Login user|name|with password|password|\n" +
        "|login|@name|with|@password|\n" +
        "\n" +
        "!|script|\n" +
        "|Login user Bob with password xyzzy|\n");
    TableScanner ts = new HtmlTableScanner(root.getData().getHtml());
    ScenarioTable st1 = new ScenarioTable(ts.getTable(0), "s1_id", this);
    ScenarioTable st2 = new ScenarioTable(ts.getTable(1), "s2_id", this);
    script = new ScriptTable(ts.getTable(2), "id", this);
    st1.appendInstructions(instructions);
    st2.appendInstructions(instructions);
    script.appendInstructions(instructions);
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0/scriptTable_s2_id_0", "call", "scriptTableActor", "loginWith", "Bob", "xyzzy")
      );
    assertEquals(expectedInstructions, instructions);
  }
  
  @Test
  public void doesntMatchScenarioWithNoArgumentsThatSharesFirstWord() throws Exception {
    WikiPageUtil.setPageContents(root, "" +
        "!|scenario|login |\n" +
        "|should not get here|\n" +
        "\n" +
        "!|scenario|connect to |name|with password|password|\n" +
        "|login with username|@name |and Password|@password    |\n" +
        "\n" +
        "!|script|\n" +
        "|connect to  |Bob| with password| xyzzy|\n");
    TableScanner ts = new HtmlTableScanner(root.getData().getHtml());
    ScenarioTable st1 = new ScenarioTable(ts.getTable(0), "s1_id", this);
    ScenarioTable st2 = new ScenarioTable(ts.getTable(1), "s2_id", this);
    script = new ScriptTable(ts.getTable(2), "id", this);
    st1.appendInstructions(instructions);
    st2.appendInstructions(instructions);
    script.appendInstructions(instructions);
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0/scriptTable_s2_id_0", "call", "scriptTableActor","loginWithUsernameAndPassword", "Bob", "xyzzy")
      );
    assertEquals(expectedInstructions, instructions);
  }

  
  @Test
  public void dontTryParameterizedForRowWithMultipleCells() throws Exception {
    WikiPageUtil.setPageContents(root, "" +
        "!|scenario|login with |name|\n" +
        "|should not get here|\n" +
        "\n" +
        "!|scenario|connect to |name|with password|password|\n" +
        "|login with username|@name |and Password|@password    |\n" +
        "\n" +
        "!|script|\n" +
        "|connect to  |Bob| with password| xyzzy|\n");
    TableScanner ts = new HtmlTableScanner(root.getData().getHtml());
    ScenarioTable st1 = new ScenarioTable(ts.getTable(0), "s1_id", this);
    ScenarioTable st2 = new ScenarioTable(ts.getTable(1), "s2_id", this);
    script = new ScriptTable(ts.getTable(2), "id", this);
    st1.appendInstructions(instructions);
    st2.appendInstructions(instructions);
    script.appendInstructions(instructions);
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0/scriptTable_s2_id_0", "call", "scriptTableActor","loginWithUsernameAndPassword", "Bob", "xyzzy")
      );
    assertEquals(expectedInstructions, instructions);
  }
 
}
