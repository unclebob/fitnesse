// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slimTables;

import fitnesse.responders.run.slimResponder.MockSlimTestContext;
import fitnesse.slim.SlimClient;
import fitnesse.slim.converters.VoidConverter;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static util.ListUtility.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DecisionTableTest {
  private WikiPage root;
  private List<Object> instructions;
  private final String simpleDecisionTable =
    "|DT:fixture|argument|\n" +
      "|var|func?|\n" +
      "|3|5|\n" +
      "|7|9|\n";
  private DecisionTable decisionTable;
  private MockSlimTestContext testContext;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    instructions = new ArrayList<Object>();
    testContext = new MockSlimTestContext();
  }

  private DecisionTable makeDecisionTableAndBuildInstructions(String tableText) throws Exception {
    decisionTable = makeDecisionTable(tableText);
    decisionTable.appendInstructions(instructions);
    return decisionTable;
  }

  private DecisionTable makeDecisionTable(String tableText) throws Exception {
    WikiPageUtil.setPageContents(root, tableText);
    TableScanner ts = new HtmlTableScanner(root.getData().getHtml());
    Table t = ts.getTable(0);
    return new DecisionTable(t, "id", testContext);
  }

  @Test
  public void aDecisionTableWithOnlyTwoRowsIsBad() throws Exception {
    assertTableIsBad(makeDecisionTableWithTwoRows());
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

  @Test
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
    List<Object> expectedInstructions = list(
      list("decisionTable_id_0", "make", "decisionTable_id", "fixture", "argument"),
      list("decisionTable_id_1", "call", "decisionTable_id", "table", list())
    );
    assertEquals(expectedInstructions, instructions);
    Map<String, Object> pseudoResults = SlimClient.resultToMap(
      list(
        list("decisionTable_id_0", "OK"),
        list("decisionTable_id_1", "OK")
      )
    );
    testContext.evaluateExpectations(pseudoResults);

    String colorizedTable = decisionTable.getTable().toString();
    String expectedColorizedTable =
      "[[pass(fixture), argument]]";
    assertEquals(expectedColorizedTable, colorizedTable);
  }


  @Test
  public void canBuildInstructionsForSimpleDecisionTable() throws Exception {
    makeDecisionTableAndBuildInstructions(simpleDecisionTable);
    int n = 0;
    List<Object> expectedInstructions = list(
      list(id(n++), "make", "decisionTable_id", "fixture", "argument"),
      list(id(n++), "call", "decisionTable_id", "table", list(list("var", "func?"), list("3", "5"), list("7", "9"))),
      list(id(n++), "call", "decisionTable_id", "beginTable"),
      list(id(n++), "call", "decisionTable_id", "reset"),
      list(id(n++), "call", "decisionTable_id", "setVar", "3"),
      list(id(n++), "call", "decisionTable_id", "execute"),
      list(id(n++), "call", "decisionTable_id", "func"),
      list(id(n++), "call", "decisionTable_id", "reset"),
      list(id(n++), "call", "decisionTable_id", "setVar", "7"),
      list(id(n++), "call", "decisionTable_id", "execute"),
      list(id(n++), "call", "decisionTable_id", "func"),
      list(id(n++), "call", "decisionTable_id", "endTable")

    );
    assertEquals(expectedInstructions.toString(), instructions.toString());
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
    List<Object> expectedInstructions = list(
      list(id(n++), "make", "decisionTable_id", "fixture", "argument"),
      list(id(n++), "call", "decisionTable_id", "table", list(list("var", "func!"), list("3", "5"), list("7", "9"))),
      list(id(n++), "call", "decisionTable_id", "beginTable"),
      list(id(n++), "call", "decisionTable_id", "reset"),
      list(id(n++), "call", "decisionTable_id", "setVar", "3"),
      list(id(n++), "call", "decisionTable_id", "execute"),
      list(id(n++), "call", "decisionTable_id", "func"),
      list(id(n++), "call", "decisionTable_id", "reset"),
      list(id(n++), "call", "decisionTable_id", "setVar", "7"),
      list(id(n++), "call", "decisionTable_id", "execute"),
      list(id(n++), "call", "decisionTable_id", "func"),
      list(id(n++), "call", "decisionTable_id", "endTable")
    );
    assertEquals(expectedInstructions.toString(), instructions.toString());
  }


  @SuppressWarnings("unchecked")
  @Test
  public void settersAreFirstFunnctionsAreLastLeftToRight() throws Exception {
    int counter = 0;
    Map<String, Integer> counters = new HashMap<String, Integer>();
    String functionsInOrder[] = {"setA", "setB", "setC", "setD", "setE", "setF", "fa", "fb", "fc", "fd", "fe", "ff"};

    makeDecisionTableAndBuildInstructions("|DT:fixture|\n" +
      "|a|fa?|b|fb?|c|fc?|d|e|f|fd?|fe?|ff?|\n" +
      "|a|a|b|b|c|c|d|e|f|d|e|f|\n");

    for (Object instructionObject : instructions) {
      List<Object> instruction = (List<Object>) instructionObject;
      for (String function : functionsInOrder) {
        if (function.equals(instruction.get(3)))
          counters.put(function, counter++);
      }
    }
    assertEquals(functionsInOrder.length, counters.size());
    for (int i=0; i<functionsInOrder.length; i++)
      assertEquals(functionsInOrder[i], i, (int)counters.get(functionsInOrder[i]));
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
    List<Object> expectedInstructions = list(
      list(id(n++), "make", "decisionTable_id", "fixture"),
      list(id(n++), "call", "decisionTable_id", "table", list(list("var", "func?"), list("3", "$V="), list("$V", "9"))),
      list(id(n++), "call", "decisionTable_id", "beginTable"),
      list(id(n++), "call", "decisionTable_id", "reset"),
      list(id(n++), "call", "decisionTable_id", "setVar", "3"),
      list(id(n++), "call", "decisionTable_id", "execute"),
      list(id(n++), "callAndAssign", "V", "decisionTable_id", "func"),
      list(id(n++), "call", "decisionTable_id", "reset"),
      list(id(n++), "call", "decisionTable_id", "setVar", "$V"),
      list(id(n++), "call", "decisionTable_id", "execute"),
      list(id(n++), "call", "decisionTable_id", "func"),
      list(id(n++), "call", "decisionTable_id", "endTable")
    );
    assertEquals(expectedInstructions.toString(), instructions.toString());
  }

  @Test
  public void canEvaluateReturnValuesAndColorizeTable() throws Exception {
    DecisionTable dt = makeDecisionTableAndBuildInstructions(simpleDecisionTable);
    int n=0;
    Map<String, Object> pseudoResults = SlimClient.resultToMap(
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
    testContext.evaluateExpectations(pseudoResults);

    String colorizedTable = dt.getTable().toString();
    String expectedColorizedTable =
      "[" +
        "[pass(DT:fixture), argument], " +
        "[var, func?], " +
        "[3, pass(5)], " +
        "[7, [5] fail(expected [9])]" +
        "]";
    assertEquals(expectedColorizedTable, colorizedTable);
  }

  @Test
  public void translatesTestTablesIntoLiteralTables() throws Exception {
    DecisionTable dt = makeDecisionTableAndBuildInstructions("!" + simpleDecisionTable);
    int n=0;
    Map<String, Object> pseudoResults = SlimClient.resultToMap(
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
    testContext.evaluateExpectations(pseudoResults);

    String colorizedTable = dt.getTable().toString();
    String expectedColorizedTable =
      "[" +
        "[pass(DT:fixture), argument], " +
        "[var, func?], " +
        "[3, pass(5)], " +
        "[7, [5] fail(expected [9])]" +
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

    List<String> makeInstruction = list("decisionTable_id_0", "make", "decisionTable_id", "SlimTest");
    assertEquals(makeInstruction, instructions.get(0));
  }

  @Test
  public void usesDisgracedMethodNames() throws Exception {
    makeDecisionTableAndBuildInstructions(
      "|DT:fixture|\n" +
        "|my var|my func?|\n" +
        "|8|7|\n"
    );
    List<String> setInstruction = list("decisionTable_id_4", "call", "decisionTable_id", "setMyVar", "8");
    List<String> callInstruction = list("decisionTable_id_6", "call", "decisionTable_id", "myFunc");
    assertEquals(setInstruction, instructions.get(4));
    assertEquals(callInstruction, instructions.get(6));
  }
}
