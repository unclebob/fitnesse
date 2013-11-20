// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fitnesse.testsystems.slim.SlimCommandRunningClient;
import fitnesse.slim.converters.VoidConverter;
import fitnesse.slim.instructions.CallAndAssignInstruction;
import fitnesse.slim.instructions.CallInstruction;
import fitnesse.slim.instructions.Instruction;
import fitnesse.slim.instructions.MakeInstruction;
import fitnesse.testsystems.slim.HtmlTableScanner;
import fitnesse.testsystems.slim.SlimTestContextImpl;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.TableScanner;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.mem.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.ListUtility.list;

public class DecisionTableTest {
  private final String simpleDecisionTable =
    "|DT:fixture|argument|\n" +
      "|var|func?|\n" +
      "|3|5|\n" +
      "|7|9|\n";
  private final String decisionTableWithSameFunctionMultipleTimes= "|DT:fixture|argument|\n" +
      "|func?|func?|\n" +
      "|3|5|\n" +
      "|7|9|\n";
  private DecisionTable decisionTable;
  private SlimTestContextImpl testContext;
  private List<SlimAssertion> assertions;

  @Before
  public void setUp() throws Exception {
    assertions = new ArrayList<SlimAssertion>();
    testContext = new SlimTestContextImpl();
  }


  private DecisionTable makeDecisionTableAndBuildInstructions(String tableText) throws Exception {
    decisionTable = makeDecisionTable(tableText);
    assertions.addAll(decisionTable.getAssertions());
    return decisionTable;
  }

  private List<Instruction> instructions() {
    return SlimAssertion.getInstructions(assertions);
  }

  private DecisionTable makeDecisionTable(String tableText) throws Exception {
    WikiPage root = InMemoryPage.makeRoot("root");
    WikiPageUtil.setPageContents(root, tableText);
    TableScanner ts = new HtmlTableScanner(root.getData().getHtml());
    Table t = ts.getTable(0);
    return new DecisionTable(t, "id", testContext);
  }

  @Test(expected=SyntaxError.class)
  public void aDecisionTableWithOnlyTwoRowsIsBad() throws Exception {
    makeDecisionTableWithTwoRows();
  }

  private void assertTableIsBad(DecisionTable decisionTable) {
    assertTrue(firstCellOfTable(decisionTable).contains("Bad table"));
  }

  private String firstCellOfTable(DecisionTable decisionTable) {
    return decisionTable.getTable().getCellContents(0, 0);
  }

  private DecisionTable makeDecisionTableWithTwoRows() throws Exception {
    return makeDecisionTableAndBuildInstructions("|x|\n|y|\n");
  }

  @Test(expected=SyntaxError.class)
  public void wrongNumberOfColumns() throws Exception {
    DecisionTable aDecisionTable = makeDecisionTableAndBuildInstructions(
      "|DT:fixture|argument|\n" +
        "|var|var2|\n" +
        "|3|\n" +
        "|7|9|\n"
    );
    assertTableIsBad(aDecisionTable);
  }

  @Test
  public void decisionTableCanBeConstructorOnly() throws Exception {
    makeDecisionTableAndBuildInstructions("|fixture|argument|\n");
    List<Instruction> expectedInstructions = list(
            new MakeInstruction("decisionTable_id_0", "decisionTable_id", "fixture", new Object[]{"argument"}),
            new CallInstruction("decisionTable_id_1", "decisionTable_id", "table", new Object[]{list()})
    );
    assertEquals(expectedInstructions, instructions());
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            list(
                    list("decisionTable_id_0", "OK"),
                    list("decisionTable_id_1", "OK")
            )
    );
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);

    String colorizedTable = decisionTable.getTable().toString();
    String expectedColorizedTable =
      "[[pass(fixture), argument]]";
    assertEquals(expectedColorizedTable, colorizedTable);
  }

  @Test
  public void canBuildInstructionsForSimpleDecisionTable() throws Exception {
    makeDecisionTableAndBuildInstructions(simpleDecisionTable);
    int n = 0;
    List<Instruction> expectedInstructions = list(
            new MakeInstruction(id(n++), "decisionTable_id", "fixture", new Object[]{"argument"}),
            new CallInstruction(id(n++), "decisionTable_id", "table", new Object[]{list(list("var", "func?"), list("3", "5"), list("7", "9"))}),
            new CallInstruction(id(n++), "decisionTable_id", "beginTable"),
            new CallInstruction(id(n++), "decisionTable_id", "reset"),
            new CallInstruction(id(n++), "decisionTable_id", "setVar", new Object[]{"3"}),
            new CallInstruction(id(n++), "decisionTable_id", "execute"),
            new CallInstruction(id(n++), "decisionTable_id", "func"),
            new CallInstruction(id(n++), "decisionTable_id", "reset"),
            new CallInstruction(id(n++), "decisionTable_id", "setVar", new Object[]{"7"}),
            new CallInstruction(id(n++), "decisionTable_id", "execute"),
            new CallInstruction(id(n++), "decisionTable_id", "func"),
            new CallInstruction(id(n++), "decisionTable_id", "endTable")

    );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void canBuildInstructionsForMultipleCallsToSameSetter() throws Exception {
    String decisionTableWithSameSetterMultipleTimes = "|DT:fixture|argument|\n" +
            "|var|var|\n" +
            "|3|5|\n" +
            "|7|9|\n";
    makeDecisionTableAndBuildInstructions(decisionTableWithSameSetterMultipleTimes);
    int n = 0;
    List<Instruction> expectedInstructions = list(
            new MakeInstruction(id(n++), "decisionTable_id", "fixture", new Object[]{"argument"}),
            new CallInstruction(id(n++), "decisionTable_id", "table", new Object[]{list(list("var", "var"), list("3", "5"), list("7", "9"))}),
            new CallInstruction(id(n++), "decisionTable_id", "beginTable"),
            new CallInstruction(id(n++), "decisionTable_id", "reset"),
            new CallInstruction(id(n++), "decisionTable_id", "setVar", new Object[]{"3"}),
            new CallInstruction(id(n++), "decisionTable_id", "setVar", new Object[]{"5"}),
            new CallInstruction(id(n++), "decisionTable_id", "execute"),
            new CallInstruction(id(n++), "decisionTable_id", "reset"),
            new CallInstruction(id(n++), "decisionTable_id", "setVar", new Object[]{"7"}),
            new CallInstruction(id(n++), "decisionTable_id", "setVar", new Object[]{"9"}),
            new CallInstruction(id(n++), "decisionTable_id", "execute"),
            new CallInstruction(id(n++), "decisionTable_id", "endTable")

    );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void canBuildInstructionsForMultipleCallsToSameFunction() throws Exception {
    makeDecisionTableAndBuildInstructions(decisionTableWithSameFunctionMultipleTimes);
    int n = 0;
    List<Instruction> expectedInstructions = list(
            new MakeInstruction(id(n++), "decisionTable_id", "fixture", new Object[]{"argument"}),
            new CallInstruction(id(n++), "decisionTable_id", "table", new Object[]{list(list("func?", "func?"), list("3", "5"), list("7", "9"))}),
            new CallInstruction(id(n++), "decisionTable_id", "beginTable"),
            new CallInstruction(id(n++), "decisionTable_id", "reset"),
            new CallInstruction(id(n++), "decisionTable_id", "execute"),
            new CallInstruction(id(n++), "decisionTable_id", "func"),
            new CallInstruction(id(n++), "decisionTable_id", "func"),
            new CallInstruction(id(n++), "decisionTable_id", "reset"),
            new CallInstruction(id(n++), "decisionTable_id", "execute"),
            new CallInstruction(id(n++), "decisionTable_id", "func"),
            new CallInstruction(id(n++), "decisionTable_id", "func"),
            new CallInstruction(id(n++), "decisionTable_id", "endTable")

    );
    assertEquals(expectedInstructions, instructions());
  }

  private String id(int n) {
    return "decisionTable_id_"+n;
  }

  @Test
  public void canUseBangToCallFunction() throws Exception {
    makeDecisionTableAndBuildInstructions(
      "|DT:fixture|argument|\n" +
      "|var|func!|\n" +
      "|3|5|\n" +
      "|7|9|\n");
    int n=0;
    List<Instruction> expectedInstructions = list(
            new MakeInstruction(id(n++), "decisionTable_id", "fixture", new Object[]{"argument"}),
            new CallInstruction(id(n++), "decisionTable_id", "table", new Object[]{list(list("var", "func!"), list("3", "5"), list("7", "9"))}),
            new CallInstruction(id(n++), "decisionTable_id", "beginTable"),
            new CallInstruction(id(n++), "decisionTable_id", "reset"),
            new CallInstruction(id(n++), "decisionTable_id", "setVar", new Object[]{"3"}),
            new CallInstruction(id(n++), "decisionTable_id", "execute"),
            new CallInstruction(id(n++), "decisionTable_id", "func"),
            new CallInstruction(id(n++), "decisionTable_id", "reset"),
            new CallInstruction(id(n++), "decisionTable_id", "setVar", new Object[]{"7"}),
            new CallInstruction(id(n++), "decisionTable_id", "execute"),
            new CallInstruction(id(n++), "decisionTable_id", "func"),
            new CallInstruction(id(n++), "decisionTable_id", "endTable")
    );
    assertEquals(expectedInstructions, instructions());
  }


  @SuppressWarnings("unchecked")
  @Test
  public void settersAreFirstFunctionsAreLastLeftToRight() throws Exception {
    makeDecisionTableAndBuildInstructions("|DT:fixture|\n" +
      "|a|fa?|b|fb?|c|fc?|d|e|f|fd?|fe?|ff?|\n" +
      "|a|a|b|b|c|c|d|e|f|d|e|f|\n");
    int n = 0;

    List<Instruction> expectedInstructions = list(
            new MakeInstruction(id(n++), "decisionTable_id", "fixture"),
            new CallInstruction(id(n++),"decisionTable_id", "table", new Object[] {list(
                list("a", "fa?", "b", "fb?", "c", "fc?", "d", "e", "f", "fd?", "fe?", "ff?"),
                list("a", "a", "b", "b", "c", "c", "d", "e", "f", "d", "e", "f"))}),
            new CallInstruction(id(n++), "decisionTable_id", "beginTable"),
            new CallInstruction(id(n++), "decisionTable_id", "reset"),
            new CallInstruction(id(n++), "decisionTable_id", "setA", new Object[] {"a"}),
            new CallInstruction(id(n++), "decisionTable_id", "setB", new Object[] {"b"}),
            new CallInstruction(id(n++), "decisionTable_id", "setC", new Object[] {"c"}),
            new CallInstruction(id(n++), "decisionTable_id", "setD", new Object[] {"d"}),
            new CallInstruction(id(n++), "decisionTable_id", "setE", new Object[] {"e"}),
            new CallInstruction(id(n++), "decisionTable_id", "setF", new Object[] {"f"}),
            new CallInstruction(id(n++), "decisionTable_id", "execute"),
            new CallInstruction(id(n++), "decisionTable_id", "fa"),
            new CallInstruction(id(n++), "decisionTable_id", "fb"),
            new CallInstruction(id(n++), "decisionTable_id", "fc"),
            new CallInstruction(id(n++), "decisionTable_id", "fd"),
            new CallInstruction(id(n++), "decisionTable_id", "fe"),
            new CallInstruction(id(n++), "decisionTable_id", "ff"),
            new CallInstruction(id(n++), "decisionTable_id", "endTable")
    );
    assertEquals(expectedInstructions, instructions());
  }



  @Test
  public void canBuildInstructionsForTableWithVariables() throws Exception {
    makeDecisionTableAndBuildInstructions(
      "|DT:fixture|\n" +
        "|var|func?|\n" +
        "|3|$V=|\n" +
        "|$V|9|\n"
    );
    int n=0;
    List<Instruction> expectedInstructions = list(
            new MakeInstruction(id(n++), "decisionTable_id", "fixture"),
            new CallInstruction(id(n++), "decisionTable_id", "table", new Object[]{list(list("var", "func?"), list("3", "$V="), list("$V", "9"))}),
            new CallInstruction(id(n++), "decisionTable_id", "beginTable"),
            new CallInstruction(id(n++), "decisionTable_id", "reset"),
            new CallInstruction(id(n++), "decisionTable_id", "setVar", new Object[]{"3"}),
            new CallInstruction(id(n++), "decisionTable_id", "execute"),
            new CallAndAssignInstruction(id(n++), "V", "decisionTable_id", "func"),
            new CallInstruction(id(n++), "decisionTable_id", "reset"),
            new CallInstruction(id(n++), "decisionTable_id", "setVar", new Object[]{"$V"}),
            new CallInstruction(id(n++), "decisionTable_id", "execute"),
            new CallInstruction(id(n++), "decisionTable_id", "func"),
            new CallInstruction(id(n++), "decisionTable_id", "endTable")
    );
    assertEquals(expectedInstructions.toString(), instructions().toString());
  }

  @Test
  public void canEvaluateReturnValuesAndColorizeTable() throws Exception {
    DecisionTable dt = makeDecisionTableAndBuildInstructions(simpleDecisionTable);
    int n=0;
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            list(
                    list(id(n++), "OK"),
                    list(id(n++), VoidConverter.VOID_TAG),
                    list(id(n++), VoidConverter.VOID_TAG), // beginTable
                    list(id(n++), VoidConverter.VOID_TAG), //reset
                    list(id(n++), VoidConverter.VOID_TAG), //set
                    list(id(n++), VoidConverter.VOID_TAG), //execute
                    list(id(n++), "5"),
                    list(id(n++), VoidConverter.VOID_TAG),
                    list(id(n++), VoidConverter.VOID_TAG),
                    list(id(n++), VoidConverter.VOID_TAG),
                    list(id(n++), "5"),
                    list(id(n++), VoidConverter.VOID_TAG) //endTable
            )
    );
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);

    String colorizedTable = dt.getTable().toString();
    String expectedColorizedTable =
      "[" +
        "[pass(DT:fixture), argument], " +
        "[var, func?], " +
        "[3, pass(5)], " +
        "[7, fail(a=5;e=9)]" +
        "]";
    assertEquals(expectedColorizedTable, colorizedTable);
  }

  @Test
  public void translatesTestTablesIntoLiteralTables() throws Exception {
    DecisionTable dt = makeDecisionTableAndBuildInstructions("!" + simpleDecisionTable);
    int n=0;
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            list(
                    list(id(n++), "OK"),
                    list(id(n++), VoidConverter.VOID_TAG),
                    list(id(n++), VoidConverter.VOID_TAG), //beginTable
                    list(id(n++), VoidConverter.VOID_TAG), //reset
                    list(id(n++), VoidConverter.VOID_TAG), //set
                    list(id(n++), VoidConverter.VOID_TAG), //execute
                    list(id(n++), "5"),
                    list(id(n++), VoidConverter.VOID_TAG),
                    list(id(n++), VoidConverter.VOID_TAG),
                    list(id(n++), VoidConverter.VOID_TAG),
                    list(id(n++), "5"),
                    list(id(n++), VoidConverter.VOID_TAG) //endTable
            )
    );
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);

    String colorizedTable = dt.getTable().toString();
    String expectedColorizedTable =
      "[" +
        "[pass(DT:fixture), argument], " +
        "[var, func?], " +
        "[3, pass(5)], " +
        "[7, fail(a=5;e=9)]" +
        "]";
    assertEquals(expectedColorizedTable, colorizedTable);
  }

  @Test
  public void canEvaluateReturnValuesAndColorizeTableForMultipleCallsToSameFunction() throws Exception {
    DecisionTable dt = makeDecisionTableAndBuildInstructions(decisionTableWithSameFunctionMultipleTimes);
    int n=0;
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            list(
                    list(id(n++), "OK"),
                    list(id(n++), VoidConverter.VOID_TAG),
                    list(id(n++), VoidConverter.VOID_TAG), // beginTable
                    list(id(n++), VoidConverter.VOID_TAG), //reset
                    list(id(n++), VoidConverter.VOID_TAG), //execute
                    list(id(n++), "4"),
                    list(id(n++), "5"),
                    list(id(n++), VoidConverter.VOID_TAG),
                    list(id(n++), VoidConverter.VOID_TAG),
                    list(id(n++), "7"),
                    list(id(n++), "5"),
                    list(id(n++), VoidConverter.VOID_TAG) //endTable
            )
    );
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);

    String colorizedTable = dt.getTable().toString();
    String expectedColorizedTable =
      "[" +
        "[pass(DT:fixture), argument], " +
        "[func?, func?], " +
        "[fail(a=4;e=3), pass(5)], " +
        "[pass(7), fail(a=5;e=9)]" +
        "]";
    assertEquals(expectedColorizedTable, colorizedTable);
  }

  @Test
  public void usesDisgracedClassNames() throws Exception {
    makeDecisionTableAndBuildInstructions(
      "|DT:slim test|\n" +
        "|x|\n" +
        "|y|\n"
    );

    Instruction makeInstruction = new MakeInstruction("decisionTable_id_0", "decisionTable_id", "SlimTest");
    assertEquals(makeInstruction, instructions().get(0));
  }

  @Test
  public void usesDisgracedMethodNames() throws Exception {
    makeDecisionTableAndBuildInstructions(
      "|DT:fixture|\n" +
        "|my var|my func?|\n" +
        "|8|7|\n"
    );
    CallInstruction setInstruction = new CallInstruction("decisionTable_id_4", "decisionTable_id", "setMyVar", new Object[]{"8"});
    CallInstruction callInstruction = new CallInstruction("decisionTable_id_6", "decisionTable_id", "myFunc");
    assertEquals(setInstruction, instructions().get(4));
    assertEquals(callInstruction, instructions().get(6));
  }
}
