package fitnesse.responders.run.slimResponder;

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

  private DecisionTable makeDecisionTableAndBuildInstructions(String tableText) throws Exception {
    dt = makeDecisionTable(tableText);
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
      list("decisionTable_id_0", "make", "decisionTable_id", "fixture", "argument")
    );
    assertEquals(expectedInstructions, instructions);
    Map<String, Object> pseudoResults = SlimClient.resultToMap(
      list(
        list("decisionTable_id_0", "OK")
      )
    );
    dt.evaluateExpectations(pseudoResults);

    String colorizedTable = dt.getTable().toString();
    String expectedColorizedTable =
      "|!style_pass(!-fixture-!)|!-argument-!|\n";
    assertEquals(expectedColorizedTable, colorizedTable);
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
  public void canBuildInstructionsForTableWithVariables() throws Exception {
    makeDecisionTableAndBuildInstructions(
      "|DT:fixture|\n" +
        "|var|func?|\n" +
        "|3|$V=|\n" +
        "|$V|9|\n"
    );
    List<Object> expectedInstructions = list(
      list("decisionTable_id_0", "make", "decisionTable_id", "fixture"),
      list("decisionTable_id_1", "call", "decisionTable_id", "setVar", "3"),
      list("decisionTable_id_2", "call", "decisionTable_id", "execute"),
      list("decisionTable_id_3", "callAndAssign", "V", "decisionTable_id", "func"),
      list("decisionTable_id_4", "call", "decisionTable_id", "setVar", "$V"),
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
      "|!style_pass(!-DT:fixture-!)|!-argument-!|\n" +
        "|!-var-!|!-func?-!|\n" +
        "|!-3-!|!style_pass(!-5-!)|\n" +
        "|!-7-!|[!-5-!] !style_fail(expected [!-9-!])|\n";
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
      "|!style_pass(!-DT:fixture-!)|!-argument-!|\n" +
        "|!-var-!|!-func?-!|\n" +
        "|!-3-!|!style_pass(!-5-!)|\n" +
        "|!-7-!|[!-5-!] !style_fail(expected [!-9-!])|\n";
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
}
