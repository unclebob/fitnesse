// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fitnesse.slim.instructions.CallInstruction;
import fitnesse.slim.instructions.Instruction;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.slim.HtmlTableScanner;
import fitnesse.testsystems.slim.SlimCommandRunningClient;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.SlimTestContextImpl;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.TableScanner;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.fs.InMemoryPage;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@Ignore
public class ScenarioAndDecisionTableScriptOnlyExtensionTest {
  private static final String SCRIPT_EXTENSION_NAME = "diffScript2";
  private static final String DIFF_SCRIPT_TABLE2_TYPE = "diffScriptTable2";

  private WikiPage root;
  private List<SlimAssertion> assertions;
  private DecisionTable dt;

  private List<Instruction> instructions() {
    return SlimAssertion.getInstructions(assertions);
  }

  @Before
  public void setUp() throws Exception {
    SlimTableFactory slimTableFactory = new SlimTableFactory();
    slimTableFactory.addTableType(SCRIPT_EXTENSION_NAME, DiffScriptTable2.class);
    root = InMemoryPage.makeRoot("root");
    assertions = new ArrayList<>();
  }

  private SlimTestContextImpl makeTables(String scenarioText, String decisionTableText) throws Exception {
    SlimTestContextImpl testContext = new SlimTestContextImpl(new WikiTestPage(root));
    String tableText = "!|scenario|" + scenarioText + "|\n"
            + "\n"
            + "!|" + SCRIPT_EXTENSION_NAME + "|\n"
            + "\n"
            + "!|DT:" + decisionTableText + "|\n";
    WikiPageUtil.setPageContents(root, tableText);
    TableScanner ts = new HtmlTableScanner(root.getHtml());
    Table t = ts.getTable(0);
    ScenarioTable st = new ScenarioTable(t, "s_id", testContext);
    t = ts.getTable(1);
    DiffScriptTable2 dst = new DiffScriptTable2(t, "ds_id", testContext);
    t = ts.getTable(2);
    dt = new DecisionTable(t, "did", testContext);
    assertions.addAll(st.getAssertions());
    assertions.addAll(dst.getAssertions());
    assertions.addAll(dt.getAssertions());
    return testContext;
  }

  @Test
  public void bracesAroundArgumentInTable() throws Exception {
    SlimTestContextImpl testContext = makeTables(
      "echo|user|giving|user_old|\n" +
        "|check|echo|@{user}|@{user_old}",
      "EchoGiving|\n" +
        "|user|user_old|\n" +
        "|7|7"
    );
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(
                    asList("decisionTable_did_0/"+ DIFF_SCRIPT_TABLE2_TYPE + "_s_id_0", "7")
            )
    );
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);

    String scriptTable = dt.getChildren().get(0).getTable().toString();
    String expectedScript =
      "[[scenario, echo, user, giving, user_old], [check, echo, 7, pass(7)]]";
    assertEquals(expectedScript, scriptTable);
    assertEquals(1, testContext.getTestSummary().getRight());
    assertEquals(0, testContext.getTestSummary().getWrong());
    assertEquals(0, testContext.getTestSummary().getIgnores());
    assertEquals(0, testContext.getTestSummary().getExceptions());
  }

  @Test
  public void oneInput() throws Exception {
    makeTables(
      "myScenario|input|\n" +
        "|function|@input",
      "myScenario|\n" +
        "|input|\n" +
        "|7"
    );
    List<CallInstruction> expectedInstructions =
      asList(
              new CallInstruction("decisionTable_did_0/" + DIFF_SCRIPT_TABLE2_TYPE + "_s_id_0", DIFF_SCRIPT_TABLE2_TYPE + "Actor", "function", new Object[]{"7"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void twoDecisionTablesDifferentScripts() throws Exception {
    SlimTestContextImpl testContext = new SlimTestContextImpl(new WikiTestPage(root));
    String tableText = "!|scenario|myScenario|input|\n"
            + "|function|@input|\n"
            + "\n"
            + "!|" + SCRIPT_EXTENSION_NAME + "|\n"
            + "\n"
            + "!|DT:myScenario|\n"
            + "|input|\n"
            + "|7|\n"
            + "\n"
            + "!|script|\n"
            + "\n"
            + "!|DT:myScenario|\n"
            + "|input|\n"
            + "|6|\n";
    WikiPageUtil.setPageContents(root, tableText);
    TableScanner ts = new HtmlTableScanner(root.getHtml());
    Table t = ts.getTable(0);
    ScenarioTable st = new ScenarioTable(t, "s_id", testContext);
    t = ts.getTable(1);
    DiffScriptTable2 dst = new DiffScriptTable2(t, "ds_id", testContext);
    t = ts.getTable(2);
    dt = new DecisionTable(t, "did", testContext);

    t = ts.getTable(3);
    ScriptTable sct = new ScriptTable(t, "sct_id", testContext);
    t = ts.getTable(4);
    DecisionTable dt2 = new DecisionTable(t, "did2", testContext);

    assertions.addAll(st.getAssertions());
    assertions.addAll(dst.getAssertions());
    assertions.addAll(dt.getAssertions());
    assertions.addAll(sct.getAssertions());
    assertions.addAll(dt2.getAssertions());

    List<CallInstruction> expectedInstructions =
            asList(
                    new CallInstruction("decisionTable_did_0/"+ DIFF_SCRIPT_TABLE2_TYPE + "_s_id_0", DIFF_SCRIPT_TABLE2_TYPE + "Actor", "function", new Object[]{"7"}),
                    new CallInstruction("decisionTable_did2_0/scriptTable_s_id_0", "scriptTableActor", "function", new Object[]{"6"})
            );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void manyInputsAndRows() throws Exception {
    makeTables(
      "login|user name|password|password|pin|pin|\n" +
        "|login|@userName|with password|@password|and pin|@pin|\n" +
        "|show|currentUserProfileUrl",
      "LoginPasswordPin|\n" +
        "|user name|password|pin|\n" +
        "|bob|xyzzy|7734|\n" +
        "|bill|yabba|8892"
    );
    List<CallInstruction> expectedInstructions =
      asList(
              new CallInstruction("decisionTable_did_0/"+ DIFF_SCRIPT_TABLE2_TYPE + "_s_id_0", DIFF_SCRIPT_TABLE2_TYPE + "Actor", "loginWithPasswordAndPin", new Object[]{"bob", "xyzzy", "7734"}),
              new CallInstruction("decisionTable_did_0/"+ DIFF_SCRIPT_TABLE2_TYPE + "_s_id_1", DIFF_SCRIPT_TABLE2_TYPE + "Actor", "currentUserProfileUrl", new Object[0]),
              new CallInstruction("decisionTable_did_1/"+ DIFF_SCRIPT_TABLE2_TYPE + "_s_id_0", DIFF_SCRIPT_TABLE2_TYPE + "Actor", "loginWithPasswordAndPin", new Object[]{"bill", "yabba", "8892"}),
              new CallInstruction("decisionTable_did_1/"+ DIFF_SCRIPT_TABLE2_TYPE + "_s_id_1", DIFF_SCRIPT_TABLE2_TYPE + "Actor", "currentUserProfileUrl", new Object[0])
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void simpleInputAndOutputPassing() throws Exception {
    SlimTestContextImpl testContext = makeTables(
            "echo|input|giving|output|\n" +
                    "|check|echo|@input|@output",
            "EchoGiving|\n" +
                    "|input|output|\n" +
                    "|7|7"
    );
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(
                    asList("decisionTable_did_0/" + DIFF_SCRIPT_TABLE2_TYPE + "_s_id_0", "7")
            )
    );
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);

    String scriptTable = dt.getChildren().get(0).getTable().toString();
    String expectedScript =
      "[[scenario, echo, input, giving, output], [check, echo, 7, pass(7)]]";
    assertEquals(expectedScript, scriptTable);
    assertEquals(1, testContext.getTestSummary().getRight());
    assertEquals(0, testContext.getTestSummary().getWrong());
    assertEquals(0, testContext.getTestSummary().getIgnores());
    assertEquals(0, testContext.getTestSummary().getExceptions());
  }

  @Test
  public void simpleInputAndOutputFailing() throws Exception {
    SlimTestContextImpl testContext = makeTables(
            "echo|input|giving|output|\n" +
                    "|check|echo|@input|@output",
            "EchoGiving|\n" +
                    "|input|output|\n" +
                    "|7|8"
    );
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(
                    asList("decisionTable_did_0/" + DIFF_SCRIPT_TABLE2_TYPE + "_s_id_0", "7")
            )
    );
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);

    String scriptTable = dt.getChildren().get(0).getTable().toString();
    String expectedScript =
      "[[scenario, echo, input, giving, output], [check, echo, 7, fail(a=7;e=8)]]";
    assertEquals(expectedScript, scriptTable);
    assertEquals(0, testContext.getTestSummary().getRight());
    assertEquals(1, testContext.getTestSummary().getWrong());
    assertEquals(0, testContext.getTestSummary().getIgnores());
    assertEquals(0, testContext.getTestSummary().getExceptions());
  }

  @Test(expected=SyntaxError.class)
  public void scenarioHasTooFewArguments() throws Exception {
    makeTables(
      "echo|input|giving|\n" +
        "|check|echo|@input|@output",
      "EchoGiving|\n" +
        "|input|output|\n" +
        "|7|8"
    );
  }

  @Test
  public void scenarioHasExtraArgumentsThatAreIgnored() throws Exception {
    makeTables(
      "echo|input|giving|output||output2|\n" +
        "|check|echo|@input|@output",
      "EchoGiving|\n" +
        "|input|output|\n" +
        "|7|7"
    );
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(
                    asList("decisionTable_did_0/" + DIFF_SCRIPT_TABLE2_TYPE + "_s_id_0", "7")
            )
    );
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);

    String scriptTable = dt.getChildren().get(0).getTable().toString();
    String expectedScript =
      "[[scenario, echo, input, giving, output, , output2], [check, echo, 7, pass(7)]]";
    assertEquals(expectedScript, scriptTable);
  }

  /**
   * Special script table.
   */
  public static class DiffScriptTable2 extends ScriptTable {

    public DiffScriptTable2(Table table, String tableId, SlimTestContext context) {
      super(table, tableId, context);
    }
    @Override
    protected String getTableType() {
      return DIFF_SCRIPT_TABLE2_TYPE;
    }

  }
}
