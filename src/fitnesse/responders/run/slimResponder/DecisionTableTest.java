package fitnesse.responders.run.slimResponder;

import static fitnesse.responders.run.slimResponder.DecisionTable.ReturnedValueExpectation;
import static fitnesse.responders.run.slimResponder.SlimTable.Expectation;
import fitnesse.slim.SlimClient;
import static fitnesse.util.ListUtility.list;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
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
  public DecisionTable dt;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    instructions = new ArrayList<Object>();
  }

  private DecisionTable makeDecisionTableAndBuildInstructions(String pageContents) throws Exception {
    dt = makeDecisionTable(pageContents);
    dt.appendInstructions(instructions);
    return dt;
  }

  private DecisionTable makeDecisionTable(String tableText) throws Exception {
    WikiPageUtil.setPageContents(root, tableText);
    TableScanner ts = new TableScanner(root.getData());
    Table t = ts.getTable(0);
    DecisionTable dt = new DecisionTable(t, "id");
    return dt;
  }

  @Test
  public void badTable() throws Exception {
    makeDecisionTableAndBuildInstructions("|x|\n");
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
  public void canBuildInstructionsForSimpleDecisionTable() throws Exception {
    makeDecisionTableAndBuildInstructions(simpleDecisionTable);
    List<Object> expectedInstructions = list(
      list("decisionTable_id_0", "make", "decisionTable_id", "fixture", "argument"),
      list("decisionTable_id_1", "call", "decisionTable_id", "setVar", "3"),
      list("decisionTable_id_2", "call", "decisionTable_id", "execute"),
      list("decisionTable_id_3", "call", "decisionTable_id", "func"),
      list("decisionTable_id_4", "call", "decisionTable_id", "setVar", "7"),
      list("decisionTable_id_5", "call", "decisionTable_id", "execute"),
      list("decisionTable_id_6", "call", "decisionTable_id", "func")
    );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void canEvaluateReturnValuesAndColorizeTable() throws Exception {
    DecisionTable dt = makeDecisionTableAndBuildInstructions(simpleDecisionTable);
    Map<String, Object> pseudoResults = SlimClient.resultToMap(
      list(
        list("decisionTable_id_0", "OK"),
        list("decisionTable_id_1", "set"),
        list("decisionTable_id_3", "5"),
        list("decisionTable_id_4", "set"),
        list("decisionTable_id_6", "5")
      )
    );
    dt.evaluateExpectations(pseudoResults);

    String colorizedTable = dt.getTable().toString();
    String expectedColorizedTable =
      "|DT:fixture|argument|\n" +
        "|var|func?|\n" +
        "|3|!style_pass(5)|\n" +
        "|7|!style_fail(<5> expected <9>)|\n";
    assertEquals(expectedColorizedTable, colorizedTable);
  }

  @Test
  public void translatesTestTablesIntoLiteralTables() throws Exception {
    DecisionTable dt = makeDecisionTableAndBuildInstructions("!" + simpleDecisionTable);
    Map<String, Object> pseudoResults = SlimClient.resultToMap(
      list(
        list("decisionTable_id_0", "OK"),
        list("decisionTable_id_1", "set"),
        list("decisionTable_id_3", "5"),
        list("decisionTable_id_4", "set"),
        list("decisionTable_id_6", "5")
      )
    );
    dt.evaluateExpectations(pseudoResults);

    String colorizedTable = dt.getTable().toString();
    String expectedColorizedTable =
      "|!-DT:fixture-!|!-argument-!|\n" +
        "|!-var-!|!-func?-!|\n" +
        "|!-3-!|!style_pass(!-5-!)|\n" +
        "|!-7-!|!style_fail(<!-5-!> expected <!-9-!>)|\n";
    assertEquals(expectedColorizedTable, colorizedTable);
  }

  @Test
  public void usesDisgracedClassNames() throws Exception {
    makeDecisionTableAndBuildInstructions(
      "|DT:slim test|\n" +
        "|x|\n" +
        "|y|\n"
    );

    List<Object> makeInstruction = list("decisionTable_id_0", "make", "decisionTable_id", "SlimTest");
    assertEquals(makeInstruction, instructions.get(0));
  }

  @Test
  public void usesDisgracedMethodNames() throws Exception {
    makeDecisionTableAndBuildInstructions(
      "|DT:fixture|\n" +
        "|my var|my func?|\n" +
        "|8|7|\n"
    );
    List<Object> setInstruction = list("decisionTable_id_1", "call", "decisionTable_id", "setMyVar", "8");
    List<Object> callInstruction = list("decisionTable_id_3", "call", "decisionTable_id", "myFunc");
    assertEquals(setInstruction, instructions.get(1));
    assertEquals(callInstruction, instructions.get(3));
  }

  private Expectation makeDummyExpectation(String expected) {
    Expectation expectation = new ReturnedValueExpectation(expected, 0, 0, 0);
    return expectation;
  }

  @Test
  public void evaluationMessageForBlankInput() throws Exception {
    String expected = "";
    Expectation expectation = makeDummyExpectation(expected);
    assertEquals("!style_pass(BLANK)", expectation.createEvaluationMessage("", "", ""));
  }

  @Test
  public void evaluationMessageForBlankExpectation() throws Exception {
    Expectation expectation = makeDummyExpectation("");
    assertEquals("!style_ignore(!-ignore-!)", expectation.createEvaluationMessage("ignore", "!-ignore-!", ""));
  }

  @Test
  public void lessThanComparisons() throws Exception {
    Expectation expectation = makeDummyExpectation(" < 5.2");
    assertEquals("!style_pass(3.0<5.2)", expectation.createEvaluationMessage("3", "", ""));
    assertEquals("!style_pass(2.0<5.2)", expectation.createEvaluationMessage("2", "", ""));
    assertEquals("!style_fail(6.0<5.2)", expectation.createEvaluationMessage("6", "", ""));
    assertEquals("!style_pass(2.8<5.2)", expectation.createEvaluationMessage("2.8", "", ""));
  }

  @Test
  public void greaterThanComparison() throws Exception {
    Expectation expectation = makeDummyExpectation(" > 5.9");
    assertEquals("!style_pass(8.0>5.9)", expectation.createEvaluationMessage("8", "", ""));
    assertEquals("!style_fail(3.6>5.9)", expectation.createEvaluationMessage("3.6", "", ""));
  }


  @Test
  public void notEqualComparison() throws Exception {
    Expectation expectation = makeDummyExpectation(" != 5.9");
    assertEquals("!style_pass(8.0!=5.9)", expectation.createEvaluationMessage("8", "", ""));
    assertEquals("!style_fail(5.9!=5.9)", expectation.createEvaluationMessage("5.9", "", ""));
  }

  @Test
  public void greaterOrEqualComparison() throws Exception {
    Expectation expectation = makeDummyExpectation(" >=  5.9 ");
    assertEquals("!style_pass(8.0>=5.9)", expectation.createEvaluationMessage("8", "", ""));
    assertEquals("!style_pass(5.9>=5.9)", expectation.createEvaluationMessage("5.9", "", ""));
    assertEquals("!style_fail(3.6>=5.9)", expectation.createEvaluationMessage("3.6", "", ""));
  }

  @Test
  public void lessOrEqualComparison() throws Exception {
    Expectation expectation = makeDummyExpectation(" <= 5.9 ");
    assertEquals("!style_pass(2.0<=5.9)", expectation.createEvaluationMessage("2", "", ""));
    assertEquals("!style_pass(5.9<=5.9)", expectation.createEvaluationMessage("5.9", "", ""));
    assertEquals("!style_fail(8.3<=5.9)", expectation.createEvaluationMessage("8.3", "", ""));
  }

  @Test
  public void openIntervalComparison() throws Exception {
    Expectation expectation = makeDummyExpectation(" 2.1 < _ < 5.9");
    assertEquals("!style_pass(2.1<4.3<5.9)", expectation.createEvaluationMessage("4.3", "", ""));
    assertEquals("!style_fail(2.1<2.1<5.9)", expectation.createEvaluationMessage("2.1", "", ""));
    assertEquals("!style_fail(2.1<8.3<5.9)", expectation.createEvaluationMessage("8.3", "", ""));
  }

  @Test
  public void closedLeftIntervalComparison() throws Exception {
    Expectation expectation = makeDummyExpectation(" 2.1 <= _ < 5.9");
    assertEquals("!style_pass(2.1<=4.3<5.9)", expectation.createEvaluationMessage("4.3", "", ""));
    assertEquals("!style_pass(2.1<=2.1<5.9)", expectation.createEvaluationMessage("2.1", "", ""));
    assertEquals("!style_fail(2.1<=8.3<5.9)", expectation.createEvaluationMessage("8.3", "", ""));
  }

  @Test
  public void closedRightIntervalComparison() throws Exception {
    Expectation expectation = makeDummyExpectation(" 2.1 < _ <= 5.9");
    assertEquals("!style_pass(2.1<4.3<=5.9)", expectation.createEvaluationMessage("4.3", "", ""));
    assertEquals("!style_fail(2.1<2.1<=5.9)", expectation.createEvaluationMessage("2.1", "", ""));
    assertEquals("!style_pass(2.1<5.9<=5.9)", expectation.createEvaluationMessage("5.9", "", ""));
    assertEquals("!style_fail(2.1<8.3<=5.9)", expectation.createEvaluationMessage("8.3", "", ""));
  }


}
