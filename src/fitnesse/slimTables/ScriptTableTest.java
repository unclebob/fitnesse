// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slimTables;

import static org.junit.Assert.assertEquals;
import static util.ListUtility.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import fitnesse.responders.run.slimResponder.MockSlimTestContext;
import fitnesse.slim.SlimClient;
import fitnesse.slim.converters.BooleanConverter;
import fitnesse.slim.converters.VoidConverter;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wikitext.Utils;

public class ScriptTableTest {
  private WikiPage root;
  private List<Object> instructions;
  private final String scriptTableHeader = "|Script|\n";
  public ScriptTable st;
  private MockSlimTestContext testContext;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    instructions = new ArrayList<Object>();
  }

  private ScriptTable buildInstructionsForWholeTable(String pageContents) throws Exception {
    st = makeScriptTable(pageContents);
    st.appendInstructions(instructions);
    return st;
  }

  private ScriptTable makeScriptTable(String tableText) throws Exception {
    WikiPageUtil.setPageContents(root, tableText);
    TableScanner ts = new HtmlTableScanner(root.getData().getHtml());
    Table t = ts.getTable(0);
    testContext = new MockSlimTestContext();
    return new ScriptTable(t, "id", testContext);
  }

  private void assertScriptResults(String sriptStatements, List<Object> scriptResults, String table) throws Exception {
    buildInstructionsFor(sriptStatements);
    List<Object> resultList = list(list("scriptTable_id_0", "OK"));
    resultList.addAll(scriptResults);
    Map<String, Object> pseudoResults = SlimClient.resultToMap(resultList);
    testContext.evaluateExpectations(pseudoResults);
    assertEquals(table, Utils.unescapeWiki(st.getTable().toString()));
  }

  private void buildInstructionsFor(String scriptStatements) throws Exception {
    buildInstructionsForWholeTable(scriptTableHeader + scriptStatements);
  }

  @Test
  public void instructionsForScriptTable() throws Exception {
    buildInstructionsFor("||\n");
    assertEquals(0, instructions.size());
  }

  @Test
  public void startStatement() throws Exception {
    buildInstructionsFor("|start|Bob|\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "make", "scriptTableActor", "Bob")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void scriptWithActor() throws Exception {
    buildInstructionsForWholeTable("|script|Bob|\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "make", "scriptTableActor", "Bob")
      );
    assertEquals(expectedInstructions, instructions);  }

  @Test
  public void startStatementWithArguments() throws Exception {
    buildInstructionsFor("|start|Bob martin|x|y|\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "make", "scriptTableActor", "BobMartin", "x", "y")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void scriptStatementWithArguments() throws Exception {
    buildInstructionsForWholeTable("|script|Bob martin|x|y|\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "make", "scriptTableActor", "BobMartin", "x", "y")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void simpleFunctionCall() throws Exception {
    buildInstructionsFor("|function|\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "call", "scriptTableActor", "function")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void functionCallWithOneArgument() throws Exception {
    buildInstructionsFor("|function|arg|\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "call", "scriptTableActor", "function", "arg")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void functionCallWithOneArgumentAndTrailingName() throws Exception {
    buildInstructionsFor("|function|arg|trail|\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "call", "scriptTableActor", "functionTrail", "arg")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void complexFunctionCallWithManyArguments() throws Exception {
    buildInstructionsFor("|eat|3|meals with|12|grams protein|3|grams fat |\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "call", "scriptTableActor", "eatMealsWithGramsProteinGramsFat", "3", "12", "3")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void checkWithFunction() throws Exception {
    buildInstructionsFor("|check|function|arg|result|\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "call", "scriptTableActor", "function", "arg")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void checkNotWithFunction() throws Exception {
    buildInstructionsFor("|check not|function|arg|result|\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "call", "scriptTableActor", "function", "arg")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void checkWithFunctionAndTrailingName() throws Exception {
    buildInstructionsFor("|check|function|arg|trail|result|\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "call", "scriptTableActor", "functionTrail", "arg")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void rejectWithFunctionCall() throws Exception {
    buildInstructionsFor("|reject|function|arg|\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "call", "scriptTableActor", "function", "arg")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void ensureWithFunctionCall() throws Exception {
    buildInstructionsFor("|ensure|function|arg|\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "call", "scriptTableActor", "function", "arg")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void showWithFunctionCall() throws Exception {
    buildInstructionsFor("|show|function|arg|\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "call", "scriptTableActor", "function", "arg")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void setSymbol() throws Exception {
    buildInstructionsFor("|$V=|function|arg|\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "callAndAssign", "V", "scriptTableActor", "function", "arg")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void useSymbol() throws Exception {
    buildInstructionsFor("|function|$V|\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "call", "scriptTableActor", "function", "$V")
      );
    assertEquals(expectedInstructions, instructions);
  }


  @Test
  public void noteDoesNothing() throws Exception {
    buildInstructionsFor("|note|blah|blah|\n");
    List<Object> expectedInstructions = list();
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void voidActionHasNoEffectOnColor() throws Exception {
    assertScriptResults("|func|\n",
      list(
        list("scriptTable_id_0", VoidConverter.VOID_TAG)
      ),
      "[[Script], [func]]"
    );
  }

  @Test
  public void actionReturningNullHasNoEffectOnColor() throws Exception {
    assertScriptResults("|func|\n",
      list(
        list("scriptTable_id_0", "null")
      ),
      "[[Script], [func]]"
    );
  }

  @Test
  public void trueActionPasses() throws Exception {
    assertScriptResults("|func|\n",
      list(
        list("scriptTable_id_0", BooleanConverter.TRUE)
      ),
      "[[Script], [pass(func)]]"
    );
  }

  @Test
  public void falseActionFails() throws Exception {
    assertScriptResults("|func|\n",
      list(
        list("scriptTable_id_0", BooleanConverter.FALSE)
      ),
      "[[Script], [fail(func)]]"
    );
  }

  @Test
  public void checkPasses() throws Exception {
    assertScriptResults("|check|func|3|\n",
      list(
        list("scriptTable_id_0", "3")
      ),
      "[[Script], [check, func, pass(3)]]"
    );
  }

  @Test
  public void checkNotFails() throws Exception {
    assertScriptResults("|check not|func|3|\n",
      list(
        list("scriptTable_id_0", "3")
      ),
      "[[Script], [check not, func, fail(3)]]"
    );
  }

  @Test
  public void checkFails() throws Exception {
    assertScriptResults("|check|func|3|\n",
      list(
        list("scriptTable_id_0", "4")
      ),
      "[[Script], [check, func, [4] fail(expected [3])]]"
    );
  }

  @Test
  public void checkNotPasses() throws Exception {
    assertScriptResults("|check not|func|3|\n",
      list(
        list("scriptTable_id_0", "4")
      ),
      "[[Script], [check not, func, [4] pass(expected [3])]]"
    );
  }

  @Test
  public void ensurePasses() throws Exception {
    assertScriptResults("|ensure|func|3|\n",
      list(
        list("scriptTable_id_0", BooleanConverter.TRUE)
      ),
      "[[Script], [pass(ensure), func, 3]]"
    );
  }

  @Test
  public void ensureFails() throws Exception {
    assertScriptResults("|ensure|func|3|\n",
      list(
        list("scriptTable_id_0", BooleanConverter.FALSE)
      ),
      "[[Script], [fail(ensure), func, 3]]"
    );
  }

  @Test
  public void rejectPasses() throws Exception {
    assertScriptResults("|reject|func|3|\n",
      list(
        list("scriptTable_id_0", BooleanConverter.FALSE)
      ),
      "[[Script], [pass(reject), func, 3]]"
    );
  }

  @Test
  public void rejectFails() throws Exception {
    assertScriptResults("|reject|func|3|\n",
      list(
        list("scriptTable_id_0", BooleanConverter.TRUE)
      ),
      "[[Script], [fail(reject), func, 3]]"
    );
  }

  @Test
  public void show() throws Exception {
    assertScriptResults("|show|func|3|\n",
      list(
        list("scriptTable_id_0", "kawabunga")
      ),
      "[[Script], [show, func, 3, kawabunga]]"
    );
  }

  @Test
  public void symbolReplacement() throws Exception {
    assertScriptResults(
      "|$V=|function|\n" +
        "|check|funcion|$V|$V|\n",
      list(
        list("scriptTable_id_0", "3"),
        list("scriptTable_id_1", "3")
      ),
      "[[Script], [$V<-[3], function], [check, funcion, $V->[3], pass($V->[3])]]"
    );
  }
}
