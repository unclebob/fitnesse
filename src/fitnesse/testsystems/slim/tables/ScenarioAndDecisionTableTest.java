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

public class ScenarioAndDecisionTableTest extends SlimTestContextImpl {
  private WikiPage root;
  private List<Assertion> assertions;
  private ScenarioTable st;
  private DecisionTable dt;

  private List<Instruction> instructions() {
    return Assertion.getInstructions(assertions);
  }

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
    dt = new DecisionTable(t, "did", this);
    assertions.addAll(st.getAssertions());
    assertions.addAll(dt.getAssertions());
  }

  @Test
  public void bracesArountArgumentInTable() throws Exception {
    makeTables(
      "!|scenario|echo|user|giving|user_old|\n" +
        "|check|echo|@{user}|@{user_old}|\n" +
        "\n" +
        "!|DT:EchoGiving|\n" +
        "|user|user_old|\n" +
        "|7|7|\n"
    );
    Map<String, Object> pseudoResults = SlimClient.resultToMap(
      list(
        list("decisionTable_did_0/scriptTable_s_id_0", "7")
      )
    );
    Assertion.evaluateExpectations(assertions, pseudoResults);

    String scriptTable = dt.getChildren().get(0).getTable().toString();
    String expectedScript =
      "[[scenario, echo, user, giving, user_old], [check, echo, 7, pass(7)]]";
    assertEquals(expectedScript, scriptTable);
    String dtHtml = dt.getTable().toString();
    assertEquals(1, getTestSummary().getRight());
    assertEquals(0, getTestSummary().getWrong());
    assertEquals(0, getTestSummary().getIgnores());
    assertEquals(0, getTestSummary().getExceptions());
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
    List<CallInstruction> expectedInstructions =
      list(
              new CallInstruction("decisionTable_did_0/scriptTable_s_id_0", "scriptTableActor", "function", new Object[]{"7"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void manyInputsAndRows() throws Exception {
    makeTables(
      "!|scenario|login|user name|password|password|pin|pin|\n" +
        "|login|@userName|with password|@password|and pin|@pin|\n" +
        "\n" +
        "!|DT:LoginPasswordPin|\n" +
        "|user name|password|pin|\n" +
        "|bob|xyzzy|7734|\n" +
        "|bill|yabba|8892|\n"
    );
    List<CallInstruction> expectedInstructions =
      list(
              new CallInstruction("decisionTable_did_0/scriptTable_s_id_0", "scriptTableActor", "loginWithPasswordAndPin", new Object[]{"bob", "xyzzy", "7734"}),
              new CallInstruction("decisionTable_did_1/scriptTable_s_id_0", "scriptTableActor", "loginWithPasswordAndPin", new Object[]{"bill", "yabba", "8892"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void simpleInputAndOutputPassing() throws Exception {
    makeTables(
      "!|scenario|echo|input|giving|output|\n" +
        "|check|echo|@input|@output|\n" +
        "\n" +
        "!|DT:EchoGiving|\n" +
        "|input|output|\n" +
        "|7|7|\n"
    );
    Map<String, Object> pseudoResults = SlimClient.resultToMap(
      list(
        list("decisionTable_did_0/scriptTable_s_id_0", "7")
      )
    );
    Assertion.evaluateExpectations(assertions, pseudoResults);

    String scriptTable = dt.getChildren().get(0).getTable().toString();
    String expectedScript =
      "[[scenario, echo, input, giving, output], [check, echo, 7, pass(7)]]";
    assertEquals(expectedScript, scriptTable);
    String dtHtml = dt.getTable().toString();
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
        "!|DT:EchoGiving|\n" +
        "|input|output|\n" +
        "|7|8|\n"
    );
    Map<String, Object> pseudoResults = SlimClient.resultToMap(
      list(
        list("decisionTable_did_0/scriptTable_s_id_0", "7")
      )
    );
    Assertion.evaluateExpectations(assertions, pseudoResults);

    String scriptTable = dt.getChildren().get(0).getTable().toString();
    String expectedScript =
      "[[scenario, echo, input, giving, output], [check, echo, 7, fail(a=7;e=8)]]";
    assertEquals(expectedScript, scriptTable);
    String dtHtml = dt.getTable().toString();
    assertEquals(0, getTestSummary().getRight());
    assertEquals(1, getTestSummary().getWrong());
    assertEquals(0, getTestSummary().getIgnores());
    assertEquals(0, getTestSummary().getExceptions());
  }

  @Test(expected=SyntaxError.class)
  public void scenarioHasTooFewArguments() throws Exception {
    makeTables(
      "!|scenario|echo|input|giving|\n" +
        "|check|echo|@input|@output|\n" +
        "\n" +
        "!|DT:EchoGiving|\n" +
        "|input|output|\n" +
        "|7|8|\n"
    );
  }

  @Test
  public void scenarioHasExtraArgumentsThatAreIgnored() throws Exception {
    makeTables(
      "!|scenario|echo|input|giving|output||output2|\n" +
        "|check|echo|@input|@output|\n" +
        "\n" +
        "!|DT:EchoGiving|\n" +
        "|input|output|\n" +
        "|7|7|\n"
    );
    Map<String, Object> pseudoResults = SlimClient.resultToMap(
      list(
        list("decisionTable_did_0/scriptTable_s_id_0", "7")
      )
    );
    Assertion.evaluateExpectations(assertions, pseudoResults);

    String scriptTable = dt.getChildren().get(0).getTable().toString();
    String expectedScript =
      "[[scenario, echo, input, giving, output, , output2], [check, echo, 7, pass(7)]]";
    assertEquals(expectedScript, scriptTable);
    String dtHtml = dt.getTable().toString();
  }
}
