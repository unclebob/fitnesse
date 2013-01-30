// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import fitnesse.slim.SlimClient;
import fitnesse.slim.converters.BooleanConverter;
import fitnesse.slim.converters.VoidConverter;
import fitnesse.slim.instructions.CallAndAssignInstruction;
import fitnesse.slim.instructions.CallInstruction;
import fitnesse.slim.instructions.Instruction;
import fitnesse.slim.instructions.MakeInstruction;
import fitnesse.testsystems.slim.HtmlTableScanner;
import fitnesse.testsystems.slim.MockSlimTestContext;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.TableScanner;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wikitext.Utils;
import org.junit.Before;
import org.junit.Test;
import util.ListUtility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static util.ListUtility.list;

public class ScriptTableTest {
  private WikiPage root;
  private List<Assertion> assertions;
  private final String scriptTableHeader = "|Script|\n";
  public ScriptTable st;
  private MockSlimTestContext testContext;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    assertions = new ArrayList<Assertion>();
  }

  private ScriptTable buildInstructionsForWholeTable(String pageContents) throws Exception {
    st = makeScriptTable(pageContents);
    assertions.addAll(st.getAssertions());
    return st;
  }

  private ScriptTable makeScriptTable(String tableText) throws Exception {
    WikiPageUtil.setPageContents(root, tableText);
    TableScanner ts = new HtmlTableScanner(root.getData().getHtml());
    Table t = ts.getTable(0);
    testContext = new MockSlimTestContext();
    return new ScriptTable(t, "id", testContext);
  }

  private void assertScriptResults(String scriptStatements, List<List<?>> scriptResults, String table) throws Exception {
    buildInstructionsFor(scriptStatements);
    List<List<?>> resultList = ListUtility.<List<?>>list(list("scriptTable_id_0", "OK"));
    resultList.addAll(scriptResults);
    Map<String, Object> pseudoResults = SlimClient.resultToMap(resultList);
    Assertion.evaluateExpectations(assertions, pseudoResults);
    assertEquals(table, Utils.unescapeWiki(st.getTable().toString()));
  }

  private void buildInstructionsFor(String scriptStatements) throws Exception {
    buildInstructionsForWholeTable(scriptTableHeader + scriptStatements);
  }

  private List<Instruction> instructions() {
    return Assertion.getInstructions(assertions);
  }

  @Test
  public void instructionsForScriptTable() throws Exception {
    buildInstructionsFor("||\n");
    assertEquals(0, assertions.size());
  }

  @Test
  public void startStatement() throws Exception {
    buildInstructionsFor("|start|Bob|\n");
    List<MakeInstruction> expectedInstructions =
      list(
              new MakeInstruction("scriptTable_id_0", "scriptTableActor", "Bob")
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void scriptWithActor() throws Exception {
    buildInstructionsForWholeTable("|script|Bob|\n");
    List<MakeInstruction> expectedInstructions =
      list(
              new MakeInstruction("scriptTable_id_0", "scriptTableActor", "Bob")
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void startStatementWithArguments() throws Exception {
    buildInstructionsFor("|start|Bob martin|x|y|\n");
    List<MakeInstruction> expectedInstructions =
      list(
              new MakeInstruction("scriptTable_id_0", "scriptTableActor", "BobMartin", new Object[]{"x", "y"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void scriptStatementWithArguments() throws Exception {
    buildInstructionsForWholeTable("|script|Bob martin|x|y|\n");
    List<MakeInstruction> expectedInstructions =
      list(
              new MakeInstruction("scriptTable_id_0", "scriptTableActor", "BobMartin", new Object[]{"x", "y"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void simpleFunctionCall() throws Exception {
    buildInstructionsFor("|function|\n");
    List<CallInstruction> expectedInstructions =
      list(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "function")
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void functionCallWithOneArgument() throws Exception {
    buildInstructionsFor("|function|arg|\n");
    List<CallInstruction> expectedInstructions =
      list(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "function", new Object[]{"arg"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void functionCallWithOneArgumentAndTrailingName() throws Exception {
    buildInstructionsFor("|function|arg|trail|\n");
    List<CallInstruction> expectedInstructions =
      list(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "functionTrail", new Object[]{"arg"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void complexFunctionCallWithManyArguments() throws Exception {
    buildInstructionsFor("|eat|3|meals with|12|grams protein|3|grams fat |\n");
    List<CallInstruction> expectedInstructions =
      list(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "eatMealsWithGramsProteinGramsFat", new Object[]{"3", "12", "3"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void functionCallWithSequentialArgumentProcessingAndOneArgument() throws Exception {
    buildInstructionsFor("|function;|arg0|\n");
    List<CallInstruction> expectedInstructions =
      list(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "function", new Object[]{"arg0"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void functionCallWithSequentialArgumentProcessingAndMultipleArguments() throws Exception {
    buildInstructionsFor("|function;|arg0|arg1|arg2|\n");
    List<CallInstruction> expectedInstructions =
      list(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "function", new Object[]{"arg0", "arg1", "arg2"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void functionCallWithSequentialArgumentProcessingEmbedded() throws Exception {
    buildInstructionsFor("|set name|Marisa|department and title;|QA|Tester|\n");
    List<CallInstruction> expectedInstructions =
      list(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "setNameDepartmentAndTitle", new Object[]{"Marisa", "QA", "Tester"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void functionCallWithSequentialArgumentProcessingEmbedded2() throws Exception {
    buildInstructionsFor("|set name|Marisa|department|QA|title and length of employment;|Tester|2 years|\n");
    List<CallInstruction> expectedInstructions =
      list(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "setNameDepartmentTitleAndLengthOfEmployment", new Object[]{"Marisa", "QA", "Tester", "2 years"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void checkWithFunction() throws Exception {
    buildInstructionsFor("|check|function|arg|result|\n");
    List<CallInstruction> expectedInstructions =
      list(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "function", new Object[]{"arg"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void checkNotWithFunction() throws Exception {
    buildInstructionsFor("|check not|function|arg|result|\n");
    List<CallInstruction> expectedInstructions =
      list(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "function", new Object[]{"arg"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void checkWithFunctionAndTrailingName() throws Exception {
    buildInstructionsFor("|check|function|arg|trail|result|\n");
    List<CallInstruction> expectedInstructions =
      list(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "functionTrail", new Object[]{"arg"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void rejectWithFunctionCall() throws Exception {
    buildInstructionsFor("|reject|function|arg|\n");
    List<CallInstruction> expectedInstructions =
      list(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "function", new Object[]{"arg"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void ensureWithFunctionCall() throws Exception {
    buildInstructionsFor("|ensure|function|arg|\n");
    List<CallInstruction> expectedInstructions =
      list(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "function", new Object[]{"arg"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void showWithFunctionCall() throws Exception {
    buildInstructionsFor("|show|function|arg|\n");
    List<CallInstruction> expectedInstructions =
      list(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "function", new Object[]{"arg"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void setSymbol() throws Exception {
    buildInstructionsFor("|$V=|function|arg|\n");
    List<CallAndAssignInstruction> expectedInstructions =
      list(
              new CallAndAssignInstruction("scriptTable_id_0", "V", "scriptTableActor", "function", new Object[]{"arg"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void useSymbol() throws Exception {
    buildInstructionsFor("|function|$V|\n");
    List<CallInstruction> expectedInstructions =
      list(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "function", new Object[]{"$V"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void noteDoesNothing() throws Exception {
    buildInstructionsFor("|note|blah|blah|\n");
    List<Instruction> expectedInstructions = Collections.emptyList();
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void initialBlankCellDoesNothing() throws Exception {
    buildInstructionsFor("||blah|blah|\n");
    List<Instruction> expectedInstructions = Collections.emptyList();
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void initialHashDoesNothing() throws Exception {
    buildInstructionsFor("|!-#comment-!|blah|blah|\n");
    List<Instruction> expectedInstructions = Collections.emptyList();
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void initialStarDoesNothing() throws Exception {
    buildInstructionsFor("|*comment|blah|blah|\n");
    List<Instruction> expectedInstructions = Collections.emptyList();
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void voidActionHasNoEffectOnColor() throws Exception {
    assertScriptResults("|func|\n",
            ListUtility.<List<?>>list(
                    list("scriptTable_id_0", VoidConverter.VOID_TAG)
            ),
      "[[Script], [func]]"
    );
  }

  @Test
  public void actionReturningNullHasNoEffectOnColor() throws Exception {
    assertScriptResults("|func|\n",
            ListUtility.<List<?>>list(
                    list("scriptTable_id_0", "null")
            ),
      "[[Script], [func]]"
    );
  }

  @Test
  public void trueActionPasses() throws Exception {
    assertScriptResults("|func|\n",
            ListUtility.<List<?>>list(
                    list("scriptTable_id_0", BooleanConverter.TRUE)
            ),
      "[[Script], [pass(func)]]"
    );
  }

  @Test
  public void falseActionFails() throws Exception {
    assertScriptResults("|func|\n",
            ListUtility.<List<?>>list(
                    list("scriptTable_id_0", BooleanConverter.FALSE)
            ),
      "[[Script], [fail(func)]]"
    );
  }

  @Test
  public void checkPasses() throws Exception {
    assertScriptResults("|check|func|3|\n",
            ListUtility.<List<?>>list(
                    list("scriptTable_id_0", "3")
            ),
      "[[Script], [check, func, pass(3)]]"
    );
  }

  @Test
  public void checkNotFails() throws Exception {
    assertScriptResults("|check not|func|3|\n",
            ListUtility.<List<?>>list(
                    list("scriptTable_id_0", "3")
            ),
      "[[Script], [check not, func, fail(3)]]"
    );
  }

  @Test
  public void checkFails() throws Exception {
    assertScriptResults("|check|func|3|\n",
            ListUtility.<List<?>>list(
                    list("scriptTable_id_0", "4")
            ),
      "[[Script], [check, func, fail(a=4;e=3)]]"
    );
  }

  @Test
  public void checkNotPasses() throws Exception {
    assertScriptResults("|check not|func|3|\n",
            ListUtility.<List<?>>list(
                    list("scriptTable_id_0", "4")
            ),
      "[[Script], [check not, func, pass(a=4;e=3)]]"
    );
  }

  @Test
  public void ensurePasses() throws Exception {
    assertScriptResults("|ensure|func|3|\n",
            ListUtility.<List<?>>list(
                    list("scriptTable_id_0", BooleanConverter.TRUE)
            ),
      "[[Script], [pass(ensure), func, 3]]"
    );
  }

  @Test
  public void ensureFails() throws Exception {
    assertScriptResults("|ensure|func|3|\n",
            ListUtility.<List<?>>list(
                    list("scriptTable_id_0", BooleanConverter.FALSE)
            ),
      "[[Script], [fail(ensure), func, 3]]"
    );
  }

  @Test
  public void rejectPasses() throws Exception {
    assertScriptResults("|reject|func|3|\n",
            ListUtility.<List<?>>list(
                    list("scriptTable_id_0", BooleanConverter.FALSE)
            ),
      "[[Script], [pass(reject), func, 3]]"
    );
  }

  @Test
  public void rejectFails() throws Exception {
    assertScriptResults("|reject|func|3|\n",
            ListUtility.<List<?>>list(
                    list("scriptTable_id_0", BooleanConverter.TRUE)
            ),
      "[[Script], [fail(reject), func, 3]]"
    );
  }

  @Test
  public void show() throws Exception {
    assertScriptResults("|show|func|3|\n",
            ListUtility.<List<?>>list(
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
            ListUtility.<List<?>>list(
                    list("scriptTable_id_0", "3"),
                    list("scriptTable_id_1", "3")
            ),
      "[[Script], [$V<-[3], function], [check, funcion, $V->[3], pass($V->[3])]]"
    );
  }

  @Test
  public void sameSymbolTwiceReplacement() throws Exception {
    assertScriptResults(
      "|$V=|function|\n" +
        "|check|funcion|$V $V|$V|\n",
            ListUtility.<List<?>>list(
                    list("scriptTable_id_0", "3"),
                    list("scriptTable_id_1", "3")
            ),
      "[[Script], [$V<-[3], function], [check, funcion, $V->[3] $V->[3], pass($V->[3])]]"
    );
  }

}
