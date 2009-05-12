// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slimTables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.ListUtility.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import fitnesse.responders.run.slimResponder.MockSlimTestContext;
import fitnesse.slim.SlimClient;
import fitnesse.slim.converters.VoidConverter;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;

public class DecisionTableTest {
  private WikiPage root;
  private List<Object> instructions;
  private final String simpleDecisionTable =
    "|DT:fixture|argument|\n" +
      "|var|func?|\n" +
      "|3|5|\n" +
      "|7|9|\n";
  public DecisionTable dt;
  private MockSlimTestContext testContext;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    instructions = new ArrayList<Object>();
    testContext = new MockSlimTestContext();
  }

  private DecisionTable makeDecisionTableAndBuildInstructions(String tableText) throws Exception {
    dt = makeDecisionTable(tableText);
    dt.appendInstructions(instructions);
    return dt;
  }

  private DecisionTable makeDecisionTable(String tableText) throws Exception {
    WikiPageUtil.setPageContents(root, tableText);
    TableScanner ts = new HtmlTableScanner(root.getData().getHtml());
    Table t = ts.getTable(0);
    DecisionTable dt = new DecisionTable(t, "id", testContext);
    return dt;
  }

  @Test
  public void badTableHasTwoRows() throws Exception {
    makeDecisionTableAndBuildInstructions("|x|\n|y|\n");
    assertTrue(dt.getTable().getCellContents(0, 0).indexOf("Bad table") != -1);
  }

  @Test
  public void wrongNumberOfColumns() throws Exception {
    makeDecisionTableAndBuildInstructions(
      "|DT:fixture|argument|\n" +
        "|var|var2|\n" +
        "|3|\n" +
        "|7|9|\n"
    );
    assertTrue(dt.getTable().getCellContents(0, 0).indexOf("Bad table") != -1);
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

    String colorizedTable = dt.getTable().toString();
    String expectedColorizedTable =
      "[[pass(fixture), argument]]";
    assertEquals(expectedColorizedTable, colorizedTable);
  }


  @Test
  public void canBuildInstructionsForSimpleDecisionTable() throws Exception {
    makeDecisionTableAndBuildInstructions(simpleDecisionTable);
    List<Object> expectedInstructions = list(
      list("decisionTable_id_0", "make", "decisionTable_id", "fixture", "argument"),
      list("decisionTable_id_1", "call", "decisionTable_id", "table", list(list("var", "func?"), list("3", "5"), list("7", "9"))),
      list("decisionTable_id_2", "call", "decisionTable_id", "reset"),
      list("decisionTable_id_3", "call", "decisionTable_id", "setVar", "3"),
      list("decisionTable_id_4", "call", "decisionTable_id", "execute"),
      list("decisionTable_id_5", "call", "decisionTable_id", "func"),
      list("decisionTable_id_6", "call", "decisionTable_id", "reset"),
      list("decisionTable_id_7", "call", "decisionTable_id", "setVar", "7"),
      list("decisionTable_id_8", "call", "decisionTable_id", "execute"),
      list("decisionTable_id_9", "call", "decisionTable_id", "func")
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
    List<Object> expectedInstructions = list(
      list("decisionTable_id_0", "make", "decisionTable_id", "fixture"),
      list("decisionTable_id_1", "call", "decisionTable_id", "table", list(list("var", "func?"), list("3", "$V="), list("$V", "9"))),
      list("decisionTable_id_2", "call", "decisionTable_id", "reset"),
      list("decisionTable_id_3", "call", "decisionTable_id", "setVar", "3"),
      list("decisionTable_id_4", "call", "decisionTable_id", "execute"),
      list("decisionTable_id_5", "callAndAssign", "V", "decisionTable_id", "func"),
      list("decisionTable_id_6", "call", "decisionTable_id", "reset"),
      list("decisionTable_id_7", "call", "decisionTable_id", "setVar", "$V"),
      list("decisionTable_id_8", "call", "decisionTable_id", "execute"),
      list("decisionTable_id_9", "call", "decisionTable_id", "func")
    );
    assertEquals(expectedInstructions.toString(), instructions.toString());
  }

  @Test
  public void canEvaluateReturnValuesAndColorizeTable() throws Exception {
    DecisionTable dt = makeDecisionTableAndBuildInstructions(simpleDecisionTable);
    Map<String, Object> pseudoResults = SlimClient.resultToMap(
      list(
        list("decisionTable_id_0", "OK"),
        list("decisionTable_id_1", VoidConverter.VOID_TAG), 
        list("decisionTable_id_2", VoidConverter.VOID_TAG), //reset
        list("decisionTable_id_3", VoidConverter.VOID_TAG), //set
        list("decisionTable_id_4", VoidConverter.VOID_TAG), //execute
        list("decisionTable_id_5", "5"),
        list("decisionTable_id_6", VoidConverter.VOID_TAG),
        list("decisionTable_id_7", VoidConverter.VOID_TAG),
        list("decisionTable_id_8", VoidConverter.VOID_TAG),
        list("decisionTable_id_9", "5")
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
    Map<String, Object> pseudoResults = SlimClient.resultToMap(
      list(
        list("decisionTable_id_0", "OK"),
        list("decisionTable_id_1", VoidConverter.VOID_TAG), 
        list("decisionTable_id_2", VoidConverter.VOID_TAG), //reset
        list("decisionTable_id_3", VoidConverter.VOID_TAG), //set
        list("decisionTable_id_4", VoidConverter.VOID_TAG), //execute
        list("decisionTable_id_5", "5"),
        list("decisionTable_id_6", VoidConverter.VOID_TAG),
        list("decisionTable_id_7", VoidConverter.VOID_TAG),
        list("decisionTable_id_8", VoidConverter.VOID_TAG),
        list("decisionTable_id_9", "5")
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
    List<String> setInstruction = list("decisionTable_id_3", "call", "decisionTable_id", "setMyVar", "8");
    List<String> callInstruction = list("decisionTable_id_5", "call", "decisionTable_id", "myFunc");
    assertEquals(setInstruction, instructions.get(3));
    assertEquals(callInstruction, instructions.get(5));
  }
}
