package fitnesse.responders.run.slimResponder;

import fitnesse.slim.SlimClient;
import static fitnesse.util.ListUtility.list;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DecisionTableTest {
  private WikiPage root;
  private List<Object> instructions;
  private final String simpleDecisionTable =
    "|fixture|argument|\n" +
      "|var|func?|\n" +
      "|3|5|\n" +
      "|7|9|\n";

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    instructions = new ArrayList<Object>();
  }

  private DecisionTable makeDecisionTableAndBuildInstructions(String pageContents) throws Exception {
    DecisionTable dt = makeDecisionTable(pageContents);
    dt.appendInstructionsTo(instructions);
    return dt;
  }

  private DecisionTable makeDecisionTable(String tableText) throws Exception {
    WikiPageUtil.setPageContents(root, tableText);
    TableScanner ts = new TableScanner(root.getData());
    Table t = ts.getTable(0);
    DecisionTable dt = new DecisionTable(t, "id");
    return dt;
  }

  @Test(expected = DecisionTable.SyntaxError.class)
  public void badTable() throws Exception {
    makeDecisionTableAndBuildInstructions("|x|\n");
  }

  @Test(expected = DecisionTable.SyntaxError.class)
  public void wrongNumberOfColumns() throws Exception {
    makeDecisionTableAndBuildInstructions(
      "|fixture|argument|\n" +
        "|var|var2|\n" +
        "|3|\n" +
        "|7|9|\n"
    );
  }

  @Test
  public void canBuildInstructionsForSimpleDecisionTable() throws Exception {
    DecisionTable dt = makeDecisionTableAndBuildInstructions(simpleDecisionTable);
    List<Object> expectedInstructions = list(
      list("decisionTable_id_0", "make", "decisionTable_id", "fixture", "argument"),
      list("decisionTable_id_1", "call", "decisionTable_id", "setvar", "3"),
      list("decisionTable_id_2", "call", "decisionTable_id", "execute"),
      list("decisionTable_id_3", "call", "decisionTable_id", "func"),
      list("decisionTable_id_4", "call", "decisionTable_id", "setvar", "7"),
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
        list("decisionTable_id_3", "5"),
        list("decisionTable_id_6", "5")
      )
    );
    dt.evaluateExpectations(pseudoResults);

    String colorizedTable = dt.getTable().toString();
    String expectedColorizedTable =
      "|fixture|argument|\n" +
        "|var|func?|\n" +
        "|3|!style_pass(5)|\n" +
        "|7|!style_fail(<5> expected <9>)|\n";
    assertEquals(expectedColorizedTable, colorizedTable);
  }

  @Test
  public void translatesTestTablesIntoLiteralTables() throws Exception {
    DecisionTable dt = makeDecisionTableAndBuildInstructions("!"+simpleDecisionTable);
    Map<String, Object> pseudoResults = SlimClient.resultToMap(
      list(
        list("decisionTable_id_3", "5"),
        list("decisionTable_id_6", "5")
      )
    );
    dt.evaluateExpectations(pseudoResults);

    String colorizedTable = dt.getTable().toString();
    String expectedColorizedTable =
      "|!-fixture-!|!-argument-!|\n" +
        "|!-var-!|!-func?-!|\n" +
        "|!-3-!|!style_pass(!-5-!)|\n" +
        "|!-7-!|!style_fail(<!-5-!> expected <!-9-!>)|\n";
    assertEquals(expectedColorizedTable, colorizedTable);
  }


}
