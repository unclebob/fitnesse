// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import fitnesse.slim.SlimClient;
import fitnesse.slim.instructions.CallInstruction;
import fitnesse.slim.instructions.Instruction;
import fitnesse.testsystems.slim.HtmlTableScanner;
import fitnesse.testsystems.slim.SlimTestContextImpl;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.TableScanner;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static util.ListUtility.list;

public class ScenarioAndScriptTableTest extends SlimTestContextImpl {
  private WikiPage root;
  private List<Assertion> assertions;
  private ScenarioTable st;
  private ScriptTable script;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    assertions = new ArrayList<Assertion>();
    clearTestSummary();
  }

  private void makeTables(String tableText) throws Exception {
    WikiPageUtil.setPageContents(root, tableText);
    TableScanner ts = new HtmlTableScanner(root.getData().getHtml());
    Table t = ts.getTable(0);
    st = new ScenarioTable(t, "s_id", this);
    t = ts.getTable(1);
    script = new ScriptTable(t, "id", this);
    assertions.addAll(st.getAssertions());
    assertions.addAll(script.getAssertions());
  }

  private List<Instruction> instructions() {
    return Assertion.getInstructions(assertions);
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
    List<CallInstruction> expectedInstructions =
      list(
              new CallInstruction("scriptTable_id_0/scriptTable_s_id_0", "scriptTableActor", "function", new Object[]{"7"})
      );
    assertEquals(expectedInstructions, instructions());
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
    List<CallInstruction> expectedInstructions =
      list(
              new CallInstruction("scriptTable_id_0/scriptTable_s_id_0", "scriptTableActor", "function", new Object[]{"1", "2"})
      );
    assertEquals(expectedInstructions, instructions());
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
    List<CallInstruction> expectedInstructions =
      list(
              new CallInstruction("scriptTable_id_0/scriptTable_s_id_0", "scriptTableActor", "loginWithPasswordAndPin", new Object[]{"bob", "xyzzy", "7734"}),
              new CallInstruction("scriptTable_id_1/scriptTable_s_id_0", "scriptTableActor", "loginWithPasswordAndPin", new Object[]{"bill", "yabba", "8892"})
      );
    assertEquals(expectedInstructions, instructions());
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

    Assertion.evaluateExpectations(assertions, pseudoResults);

    String scriptTable = script.getChildren().get(0).getTable().toString();
    String expectedScript =
      "[[scenario, echo, input, giving, output], [check, echo, 7, pass(7)]]";
    assertEquals(expectedScript, scriptTable);
    String dtHtml = script.getTable().toString();
    assertEquals(1, getTestSummary().getRight());
    assertEquals(0, getTestSummary().getWrong());
    assertEquals(0, getTestSummary().getIgnores());
    assertEquals(0, getTestSummary().getExceptions());
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
    Assertion.evaluateExpectations(assertions, pseudoResults);

    String scriptTable = script.getChildren().get(0).getTable().toString();
    String expectedScript =
      "[[scenario, echo, input, giving, output], [check, echo, 7, fail(a=7;e=8)]]";
    assertEquals(expectedScript, scriptTable);
    String dtHtml = script.getTable().toString();
    assertEquals(0, getTestSummary().getRight());
    assertEquals(1, getTestSummary().getWrong());
    assertEquals(0, getTestSummary().getIgnores());
    assertEquals(0, getTestSummary().getExceptions());
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

    Assertion.evaluateExpectations(assertions, pseudoResults);

    String scriptTable = script.getChildren().get(0).getTable().toString();
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

    Assertion.evaluateExpectations(assertions, pseudoResults);

    String scriptTable = script.getChildren().get(0).getTable().toString();
    String expectedScript =
      "[[scenario, echo, input, giving], [check, echo, 7, fail(a=7;e=@output)]]";
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

    Assertion.evaluateExpectations(assertions, pseudoResults);

    String scriptTable = script.getChildren().get(0).getTable().toString();
    String expectedScript =
      "[[scenario, echo, input, giving, output, , output2], [check, echo, 7, pass(7)]]";
    assertEquals(expectedScript, scriptTable);
    String dtHtml = script.getTable().toString();
    assertEquals(1, getTestSummary().getRight());
    assertEquals(0, getTestSummary().getWrong());
    assertEquals(0, getTestSummary().getIgnores());
    assertEquals(0, getTestSummary().getExceptions());
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
    List<CallInstruction> expectedInstructions =
      list(
              new CallInstruction("scriptTable_id_0/scriptTable_s_id_0", "scriptTableActor", "loginWith", new Object[]{"Bob", "xyzzy"})
      );
    assertEquals(expectedInstructions, instructions());
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
    List<CallInstruction> expectedInstructions =
      list(
              new CallInstruction("scriptTable_id_0/scriptTable_s_id_0", "scriptTableActor", "loginWith", new Object[]{"Bob", "xyzzy"})
      );
    assertEquals(expectedInstructions, instructions());
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
    assertions.addAll(st1.getAssertions());
    assertions.addAll(st2.getAssertions());
    assertions.addAll(script.getAssertions());
    List<CallInstruction> expectedInstructions =
      list(
              new CallInstruction("scriptTable_id_0/scriptTable_s2_id_0", "scriptTableActor", "loginWith", new Object[]{"Bob", "xyzzy"})
      );
    assertEquals(expectedInstructions, instructions());
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
    assertions.addAll(st1.getAssertions());
    assertions.addAll(st2.getAssertions());
    assertions.addAll(script.getAssertions());
    List<CallInstruction> expectedInstructions =
      list(
              new CallInstruction("scriptTable_id_0/scriptTable_s2_id_0", "scriptTableActor", "loginWithUsernameAndPassword", new Object[]{"Bob", "xyzzy"})
      );
    assertEquals(expectedInstructions, instructions());
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
    assertions.addAll(st1.getAssertions());
    assertions.addAll(st2.getAssertions());
    assertions.addAll(script.getAssertions());
    List<CallInstruction> expectedInstructions =
      list(
              new CallInstruction("scriptTable_id_0/scriptTable_s2_id_0", "scriptTableActor", "loginWithUsernameAndPassword", new Object[]{"Bob", "xyzzy"})
      );
    assertEquals(expectedInstructions, instructions());
  }

}
