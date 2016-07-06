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

public class ScenarioAndScriptTableTest {
  private WikiPage root;
  private List<SlimAssertion> assertions;
  private ScriptTable script;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    assertions = new ArrayList<>();
    ScenarioTable.setDefaultChildClass(ScriptTable.class);
  }

  private SlimTestContextImpl makeTables(String tableText) throws Exception {
    SlimTestContextImpl testContext = new SlimTestContextImpl(new WikiTestPage(root));
    WikiPageUtil.setPageContents(root, tableText);
    TableScanner ts = new HtmlTableScanner(root.getHtml());
    Table t = ts.getTable(0);
    ScenarioTable st = new ScenarioTable(t, "s_id", testContext);
    t = ts.getTable(1);
    if (t.getCellContents(0,0).equals("script")) {
      script = new ScriptTable(t, "id", testContext);
    } else {
      script = new ScriptTableTest.LocalizedScriptTable(t, "id", testContext);
    }
    assertions.addAll(st.getAssertions());
    assertions.addAll(script.getAssertions());
    return testContext;
  }

  private List<Instruction> instructions() {
    return SlimAssertion.getInstructions(assertions);
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
            asList(new CallInstruction("scriptTable_id_0/scriptTable_s_id_0", "scriptTableActor", "function", new Object[]{"7"}));
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void oneInputDifferentScriptClass() throws Exception {
    makeTables(
            "!|scenario|myScenario|input|\n" +
                    "|function|@input|\n" +
                    "\n" +
                    "!|localisedScript|\n" +
                    "|myScenario|7|\n"
    );
    List<CallInstruction> expectedInstructions =
            asList(new CallInstruction("localizedScriptTable_id_0/localizedScriptTable_s_id_0", "localizedScriptTableActor", "function", new Object[]{"7"}));
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
            asList(new CallInstruction("scriptTable_id_0/scriptTable_s_id_0", "scriptTableActor", "function", new Object[]{"1", "2"}));
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
            asList(new CallInstruction("scriptTable_id_0/scriptTable_s_id_0", "scriptTableActor", "loginWithPasswordAndPin", new Object[]{"bob", "xyzzy", "7734"}), new CallInstruction("scriptTable_id_1/scriptTable_s_id_0", "scriptTableActor", "loginWithPasswordAndPin", new Object[]{"bill", "yabba", "8892"}));
    assertEquals(expectedInstructions, instructions());
  }


  @Test
  public void simpleInputAndOutputPassing() throws Exception {
    SlimTestContextImpl testContext = makeTables(
            "!|scenario|echo|input|giving|output|\n" +
                    "|check|echo|@input|@output|\n" +
                    "\n" +
                    "!|script|\n" +
                    "|echo|7|giving|7|\n"
    );
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(asList("scriptTable_id_0/scriptTable_s_id_0", "7"))
    );

    SlimAssertion.evaluateExpectations(assertions, pseudoResults);

    String scriptTable = script.getChildren().get(0).getTable().toString();
    String expectedScript =
      "[[scenario, echo, input, giving, output], [check, echo, 7, pass(7)]]";
    assertEquals(expectedScript, scriptTable);
    assertEquals(1, testContext.getTestSummary().getRight());
    assertEquals(0, testContext.getTestSummary().getWrong());
    assertEquals(0, testContext.getTestSummary().getIgnores());
    assertEquals(0, testContext.getTestSummary().getExceptions());
  }

  @Test
  public void differentScriptSimpleInputAndOutputPassing() throws Exception {
    SlimTestContextImpl testContext = makeTables(
            "!|scenario|echo|input|giving|output|\n" +
                    "|localized check|echo|@input|@output|\n" +
                    "\n" +
                    "!|localisedScript|\n" +
                    "|echo|7|giving|7|\n"
    );
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(asList("localizedScriptTable_id_0/localizedScriptTable_s_id_0", "7"))
    );

    SlimAssertion.evaluateExpectations(assertions, pseudoResults);

    String scriptTable = script.getChildren().get(0).getTable().toString();
    String expectedScript =
            "[[scenario, echo, input, giving, output], [localized check, echo, 7, pass(7)]]";
    assertEquals(expectedScript, scriptTable);
    assertEquals(1, testContext.getTestSummary().getRight());
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
        "!|script|\n" +
        "|echo|7|giving|8|\n"
    );
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(asList("scriptTable_id_0/scriptTable_s_id_0", "7"))
    );
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);

    String scriptTable = script.getChildren().get(0).getTable().toString();
    String expectedScript =
      "[[scenario, echo, input, giving, output], [check, echo, 7, fail(a=7;e=8)]]";
    assertEquals(expectedScript, scriptTable);
    assertEquals(0, testContext.getTestSummary().getRight());
    assertEquals(1, testContext.getTestSummary().getWrong());
    assertEquals(0, testContext.getTestSummary().getIgnores());
    assertEquals(0, testContext.getTestSummary().getExceptions());
  }

  @Test
  public void inputAndOutputWithSymbol() throws Exception {
    SlimTestContextImpl testContext = makeTables(
      "!|scenario|echo|input|giving|output|\n" +
        "|check|echo|@input|@output|\n" +
        "\n" +
        "!|script|\n" +
        "|$V=|echo|7|\n" +
        "|echo|$V|giving|$V|\n"
    );
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(asList("scriptTable_id_0", "7"), asList("scriptTable_id_1/scriptTable_s_id_0", "7"))
    );

    SlimAssertion.evaluateExpectations(assertions, pseudoResults);

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
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(asList("scriptTable_id_0/scriptTable_s_id_0", "7"))
    );

    SlimAssertion.evaluateExpectations(assertions, pseudoResults);

    String scriptTable = script.getChildren().get(0).getTable().toString();
    String expectedScript =
      "[[scenario, echo, input, giving], [check, echo, 7, fail(a=7;e=@output)]]";
    assertEquals(expectedScript, scriptTable);
  }

  @Test
  public void scenarioExtraArgumentsAreIgnored() throws Exception {
    SlimTestContextImpl testContext = makeTables(
      "!|scenario|echo|input|giving|output||output2|\n" +
        "|check|echo|@input|@output|\n" +
        "\n" +
        "!|script|\n" +
        "|echo|7|giving|7|\n"
    );
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(asList("scriptTable_id_0/scriptTable_s_id_0", "7"))
    );

    SlimAssertion.evaluateExpectations(assertions, pseudoResults);

    String scriptTable = script.getChildren().get(0).getTable().toString();
    String expectedScript =
      "[[scenario, echo, input, giving, output, , output2], [check, echo, 7, pass(7)]]";
    assertEquals(expectedScript, scriptTable);
    String dtHtml = script.getTable().toString();
    assertEquals(1, testContext.getTestSummary().getRight());
    assertEquals(0, testContext.getTestSummary().getWrong());
    assertEquals(0, testContext.getTestSummary().getIgnores());
    assertEquals(0, testContext.getTestSummary().getExceptions());
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
            asList(new CallInstruction("scriptTable_id_0/scriptTable_s_id_0", "scriptTableActor", "loginWith", new Object[]{"Bob", "xyzzy"}));
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
            asList(new CallInstruction("scriptTable_id_0/scriptTable_s_id_0", "scriptTableActor", "loginWith", new Object[]{"Bob", "xyzzy"}));
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void matchesScenarioWithMostArguments() throws Exception {
    SlimTestContextImpl testContext = new SlimTestContextImpl(new WikiTestPage(root));
    WikiPageUtil.setPageContents(root, "" +
        "!|scenario|Login user|name|\n" +
        "|should not get here|\n" +
        "\n" +
        "!|scenario|Login user|name|with password|password|\n" +
        "|login|@name|with|@password|\n" +
        "\n" +
        "!|script|\n" +
        "|Login user Bob with password xyzzy|\n");
    TableScanner ts = new HtmlTableScanner(root.getHtml());
    ScenarioTable st1 = new ScenarioTable(ts.getTable(0), "s1_id", testContext);
    ScenarioTable st2 = new ScenarioTable(ts.getTable(1), "s2_id", testContext);
    script = new ScriptTable(ts.getTable(2), "id", testContext);
    assertions.addAll(st1.getAssertions());
    assertions.addAll(st2.getAssertions());
    assertions.addAll(script.getAssertions());
    List<CallInstruction> expectedInstructions =
            asList(new CallInstruction("scriptTable_id_0/scriptTable_s2_id_0", "scriptTableActor", "loginWith", new Object[]{"Bob", "xyzzy"}));
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void doesntMatchScenarioWithNoArgumentsThatSharesFirstWord() throws Exception {
    SlimTestContextImpl testContext = new SlimTestContextImpl(new WikiTestPage(root));
    WikiPageUtil.setPageContents(root, "" +
        "!|scenario|login |\n" +
        "|should not get here|\n" +
        "\n" +
        "!|scenario|connect to |name|with password|password|\n" +
        "|login with username|@name |and Password|@password    |\n" +
        "\n" +
        "!|script|\n" +
        "|connect to  |Bob| with password| xyzzy|\n");
    TableScanner ts = new HtmlTableScanner(root.getHtml());
    ScenarioTable st1 = new ScenarioTable(ts.getTable(0), "s1_id", testContext);
    ScenarioTable st2 = new ScenarioTable(ts.getTable(1), "s2_id", testContext);
    script = new ScriptTable(ts.getTable(2), "id", testContext);
    assertions.addAll(st1.getAssertions());
    assertions.addAll(st2.getAssertions());
    assertions.addAll(script.getAssertions());
    List<CallInstruction> expectedInstructions =
            asList(new CallInstruction("scriptTable_id_0/scriptTable_s2_id_0", "scriptTableActor", "loginWithUsernameAndPassword", new Object[]{"Bob", "xyzzy"}));
    assertEquals(expectedInstructions, instructions());
  }


  @Test
  public void dontTryParameterizedForRowWithMultipleCells() throws Exception {
    SlimTestContextImpl testContext = new SlimTestContextImpl(new WikiTestPage(root));
    WikiPageUtil.setPageContents(root, "" +
        "!|scenario|login with |name|\n" +
        "|should not get here|\n" +
        "\n" +
        "!|scenario|connect to |name|with password|password|\n" +
        "|login with username|@name |and Password|@password    |\n" +
        "\n" +
        "!|script|\n" +
        "|connect to  |Bob| with password| xyzzy|\n");
    TableScanner ts = new HtmlTableScanner(root.getHtml());
    ScenarioTable st1 = new ScenarioTable(ts.getTable(0), "s1_id", testContext);
    ScenarioTable st2 = new ScenarioTable(ts.getTable(1), "s2_id", testContext);
    script = new ScriptTable(ts.getTable(2), "id", testContext);
    assertions.addAll(st1.getAssertions());
    assertions.addAll(st2.getAssertions());
    assertions.addAll(script.getAssertions());
    List<CallInstruction> expectedInstructions =
            asList(new CallInstruction("scriptTable_id_0/scriptTable_s2_id_0", "scriptTableActor", "loginWithUsernameAndPassword", new Object[]{"Bob", "xyzzy"}));
    assertEquals(expectedInstructions, instructions());
  }

}
