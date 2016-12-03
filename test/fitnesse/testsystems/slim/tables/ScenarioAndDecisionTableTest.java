// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.slim.SlimCommandRunningClient;
import fitnesse.slim.instructions.CallInstruction;
import fitnesse.slim.instructions.Instruction;
import fitnesse.testsystems.slim.HtmlTableScanner;
import fitnesse.testsystems.slim.SlimTestContextImpl;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.TableScanner;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.fs.InMemoryPage;

import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class ScenarioAndDecisionTableTest {
  private WikiPage root;
  private List<SlimAssertion> assertions;
  private DecisionTable dt;

  private List<Instruction> instructions() {
    return SlimAssertion.getInstructions(assertions);
  }

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    assertions = new ArrayList<>();
  }

  private SlimTestContextImpl makeTables(String tableText) throws Exception {
    SlimTestContextImpl testContext = new SlimTestContextImpl(new WikiTestPage(root));
    WikiPageUtil.setPageContents(root, tableText);
    TableScanner ts = new HtmlTableScanner(root.getHtml());
    Table t = ts.getTable(0);
    ScenarioTable st = new ScenarioTable(t, "s_id", testContext);
    t = ts.getTable(1);
    dt = new DecisionTable(t, "did", testContext);
    assertions.addAll(st.getAssertions());
    assertions.addAll(dt.getAssertions());
    return testContext;
  }

  @Test
  public void bracesAroundArgumentInTable() throws Exception {
    SlimTestContextImpl testContext = makeTables(
            "!|scenario|echo|user|giving|user_old|\n" +
                    "|check|echo|@{user}|@{user_old}|\n" +
                    "\n" +
                    "!|DT:EchoGiving|\n" +
                    "|user|user_old|\n" +
                    "|7|7|\n"
    );
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(asList("decisionTable_did_0/scriptTable_s_id_0", "7"))
    );
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);

    String scriptTable = dt.getChildren().get(0).getTable().toString();
    String expectedScript =
      "[[scenario, echo, user, giving, user_old], [check, echo, 7, pass(7)]]";
    assertEquals(expectedScript, scriptTable);
    String dtHtml = dt.getTable().toString();
    assertEquals(1, testContext.getTestSummary().getRight());
    assertEquals(0, testContext.getTestSummary().getWrong());
    assertEquals(0, testContext.getTestSummary().getIgnores());
    assertEquals(0, testContext.getTestSummary().getExceptions());
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
            asList(new CallInstruction("decisionTable_did_0/scriptTable_s_id_0", "scriptTableActor", "function", new Object[]{"7"}));
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
            asList(new CallInstruction("decisionTable_did_0/scriptTable_s_id_0", "scriptTableActor", "loginWithPasswordAndPin", new Object[]{"bob", "xyzzy", "7734"}), new CallInstruction("decisionTable_did_1/scriptTable_s_id_0", "scriptTableActor", "loginWithPasswordAndPin", new Object[]{"bill", "yabba", "8892"}));
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void simpleInputAndOutputPassing() throws Exception {
    SlimTestContextImpl testContext = makeTables(
            "!|scenario|echo|input|giving|output|\n" +
                    "|check|echo|@input|@output|\n" +
                    "\n" +
                    "!|DT:EchoGiving|\n" +
                    "|input|output|\n" +
                    "|7|7|\n"
    );
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(asList("decisionTable_did_0/scriptTable_s_id_0", "7"))
    );
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);

    String scriptTable = dt.getChildren().get(0).getTable().toString();
    String expectedScript =
      "[[scenario, echo, input, giving, output], [check, echo, 7, pass(7)]]";
    assertEquals(expectedScript, scriptTable);
    String dtHtml = dt.getTable().toString();
    assertEquals(1, testContext.getTestSummary().getRight());
    assertEquals(0, testContext.getTestSummary().getWrong());
    assertEquals(0, testContext.getTestSummary().getIgnores());
    assertEquals(0, testContext.getTestSummary().getExceptions());
  }

  @Test
  public void gracefulNamesInputAndOutputArgPassing() throws Exception {
    SlimTestContextImpl testContext = makeTables(
            "!|scenario|echo|inPut|giving|outPut?|\n" +
                    "|$outPut=|echo|@inPut|\n" +
                    "\n" +
                    "!|DT:EchoGiving|\n" +
                    "|in put|out put?|\n" +
                    "|7|7|\n"
    );
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(asList("decisionTable_did_0/scriptTable_s_id_0", "7"))
    );
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);

    String scriptTable = dt.getChildren().get(0).getTable().toString();
    String expectedScript =
            "[[scenario, echo, inPut, giving, outPut?], [$outPut<-[7], echo, 7]]";
    assertEquals(expectedScript, scriptTable);

    String dtHtml = dt.getTable().toString();
    assertEquals("[[DT:EchoGiving], [in put, out put?], [7, pass(7)]]", dtHtml);

    assertEquals(1, testContext.getTestSummary().getRight());
    assertEquals(0, testContext.getTestSummary().getWrong());
    assertEquals(0, testContext.getTestSummary().getIgnores());
    assertEquals(0, testContext.getTestSummary().getExceptions());
  }

  @Test
  public void camelNamesInputAndOutputArgPassing() throws Exception {
    SlimTestContextImpl testContext = makeTables(
            "!|scenario|echo|inPut|giving|outPut?|\n" +
                    "|$outPut=|echo|@inPut|\n" +
                    "\n" +
                    "!|DT:EchoGiving|\n" +
                    "|inPut|outPut?|\n" +
                    "|7|7|\n"
    );
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(asList("decisionTable_did_0/scriptTable_s_id_0", "7"))
    );
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);

    String scriptTable = dt.getChildren().get(0).getTable().toString();
    String expectedScript =
            "[[scenario, echo, inPut, giving, outPut?], [$outPut<-[7], echo, 7]]";
    assertEquals(expectedScript, scriptTable);

    String dtHtml = dt.getTable().toString();
    assertEquals("[[DT:EchoGiving], [inPut, outPut?], [7, pass(7)]]", dtHtml);

    assertEquals(1, testContext.getTestSummary().getRight());
    assertEquals(0, testContext.getTestSummary().getWrong());
    assertEquals(0, testContext.getTestSummary().getIgnores());
    assertEquals(0, testContext.getTestSummary().getExceptions());
  }

  @Test
  public void gracefulNamesInputAndOutputArgAssign() throws Exception {
    SlimTestContextImpl testContext = makeTables(
            "!|scenario|echo|inPut|giving|outPut?|\n" +
                    "|$outPut=|echo|@inPut|\n" +
                    "\n" +
                    "!|DT:EchoGiving|\n" +
                    "|in put|out put?|\n" +
                    "|7|$x=|\n"
    );
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(asList("decisionTable_did_0/scriptTable_s_id_0", "7"))
    );
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);

    String scriptTable = dt.getChildren().get(0).getTable().toString();
    String expectedScript =
            "[[scenario, echo, inPut, giving, outPut?], [$outPut<-[7], echo, 7]]";
    assertEquals(expectedScript, scriptTable);

    String dtHtml = dt.getTable().toString();
    assertEquals("[[DT:EchoGiving], [in put, out put?], [7, $x<-[7]]]", dtHtml);

    assertEquals(0, testContext.getTestSummary().getRight());
    assertEquals(0, testContext.getTestSummary().getWrong());
    assertEquals(0, testContext.getTestSummary().getIgnores());
    assertEquals(0, testContext.getTestSummary().getExceptions());
  }

  @Test
  public void camelNamesInputAndOutputArgAssign() throws Exception {
    SlimTestContextImpl testContext = makeTables(
            "!|scenario|echo|inPut|giving|outPut?|\n" +
                    "|$outPut=|echo|@inPut|\n" +
                    "\n" +
                    "!|DT:EchoGiving|\n" +
                    "|inPut|outPut?|\n" +
                    "|7|$x=|\n"
    );
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(asList("decisionTable_did_0/scriptTable_s_id_0", "7"))
    );
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);

    String scriptTable = dt.getChildren().get(0).getTable().toString();
    String expectedScript =
            "[[scenario, echo, inPut, giving, outPut?], [$outPut<-[7], echo, 7]]";
    assertEquals(expectedScript, scriptTable);

    String dtHtml = dt.getTable().toString();
    assertEquals("[[DT:EchoGiving], [inPut, outPut?], [7, $x<-[7]]]", dtHtml);

    assertEquals(0, testContext.getTestSummary().getRight());
    assertEquals(0, testContext.getTestSummary().getWrong());
    assertEquals(0, testContext.getTestSummary().getIgnores());
    assertEquals(0, testContext.getTestSummary().getExceptions());
  }

  @Test
  public void simpleInputAndOutputFailing() throws Exception {
    SlimTestContextImpl testContext = makeTables(
            "!|scenario|echo|input|giving|output|\n" +
                    "|check|echo|@input|@output|\n" +
                    "\n" +
                    "!|DT:EchoGiving|\n" +
                    "|input|output|\n" +
                    "|7|8|\n"
    );
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(asList("decisionTable_did_0/scriptTable_s_id_0", "7"))
    );
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);

    String scriptTable = dt.getChildren().get(0).getTable().toString();
    String expectedScript =
      "[[scenario, echo, input, giving, output], [check, echo, 7, fail(a=7;e=8)]]";
    assertEquals(expectedScript, scriptTable);
    String dtHtml = dt.getTable().toString();
    assertEquals(0, testContext.getTestSummary().getRight());
    assertEquals(1, testContext.getTestSummary().getWrong());
    assertEquals(0, testContext.getTestSummary().getIgnores());
    assertEquals(0, testContext.getTestSummary().getExceptions());
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
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(asList("decisionTable_did_0/scriptTable_s_id_0", "7"))
    );
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);

    String scriptTable = dt.getChildren().get(0).getTable().toString();
    String expectedScript =
      "[[scenario, echo, input, giving, output, , output2], [check, echo, 7, pass(7)]]";
    assertEquals(expectedScript, scriptTable);
    String dtHtml = dt.getTable().toString();
  }
}
