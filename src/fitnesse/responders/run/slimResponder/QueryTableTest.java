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

public class QueryTableTest {
  private WikiPage root;
  private List<Object> instructions;
  private final String queryTableHeader =
    "|Query:fixture|argument|\n" +
      "|n|2n|\n";

  public QueryTable qt;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    instructions = new ArrayList<Object>();
  }

  private QueryTable makeQueryTableAndBuildInstructions(String pageContents) throws Exception {
    qt = makeQueryTable(pageContents);
    qt.appendInstructions(instructions);
    return qt;
  }

  private QueryTable makeQueryTable(String tableText) throws Exception {
    WikiPageUtil.setPageContents(root, tableText);
    TableScanner ts = new TableScanner(root.getData());
    Table t = ts.getTable(0);
    return new QueryTable(t, "id");
  }

  private void assertQueryResults(String queryRows, List<Object> queryResults, String table) throws Exception {
    makeQueryTableAndBuildInstructions(queryTableHeader + queryRows);
    Map<String, Object> pseudoResults = SlimClient.resultToMap(list(
      list("queryTable_id_0", "OK"),
      list("queryTable_id_1", queryResults)
    )
    );
    qt.evaluateExpectations(pseudoResults);
    assertEquals(table, qt.getTable().toString());
  }

  @Test
  public void instructionsForQueryTable() throws Exception {
    makeQueryTableAndBuildInstructions(queryTableHeader);
    List<Object> expectedInstructions = list(
      list("queryTable_id_0", "make", "queryTable_id", "fixture", "argument"),
      list("queryTable_id_1", "call", "queryTable_id", "query")
    );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void nullResultsForNullTable() throws Exception {
    assertQueryResults("", list(),
      "|!style_pass(Query:fixture)|argument|\n" +
        "|n|2n|\n"
    );
  }

  @Test
  public void oneRowThatMatches() throws Exception {
    assertQueryResults("|2|4|\n",
      list(
        list(list("n", "2"), list("2n", "4"))
      ),
      "|!style_pass(Query:fixture)|argument|\n" +
        "|n|2n|\n" +
        "|!style_pass(2)|!style_pass(4)|\n"
    );
  }

  @Test
  public void oneRowThatFails() throws Exception {
    assertQueryResults("|2|4|\n",
      list(
        list(list("n", "3"), list("2n", "5"))
      ),
      "|!style_pass(Query:fixture)|argument|\n" +
        "|n|2n|\n" +
        "|[2] !style_fail(missing)|4|\n" +
        "|[3] !style_fail(surplus)|5|\n"
    );
  }

  @Test
  public void oneRowWithPartialMatch() throws Exception {
    assertQueryResults("|2|4|\n",
      list(
        list(list("n", "2"), list("2n", "5"))
      ),
      "|!style_pass(Query:fixture)|argument|\n" +
        "|n|2n|\n" +
        "|!style_pass(2)|!style_fail([5] expected [4])|\n"
    );
  }

  @Test
  public void twoMatchingRows() throws Exception {
    assertQueryResults(
      "|2|4|\n" +
        "|3|6|\n",
      list(
        list(list("n", "2"), list("2n", "4")),
        list(list("n", "3"), list("2n", "6"))
      ),
      "|!style_pass(Query:fixture)|argument|\n" +
        "|n|2n|\n" +
        "|!style_pass(2)|!style_pass(4)|\n" +
        "|!style_pass(3)|!style_pass(6)|\n"
    );
  }

  @Test
  public void twoMatchingRowsOutOfOrder() throws Exception {
    assertQueryResults(
      "|3|6|\n" +
        "|2|4|\n",
      list(
        list(list("n", "2"), list("2n", "4")),
        list(list("n", "3"), list("2n", "6"))
      ),
      "|!style_pass(Query:fixture)|argument|\n" +
        "|n|2n|\n" +
        "|!style_pass(3)|!style_pass(6)|\n" +
        "|!style_pass(2)|!style_pass(4)|\n"
    );
  }

  @Test
  public void twoRowsFirstMatchesSecondDoesnt() throws Exception {
    assertQueryResults(
      "|3|6|\n" +
        "|99|99|\n",
      list(
        list(list("n", "2"), list("2n", "4")),
        list(list("n", "3"), list("2n", "6"))
      ),
      "|!style_pass(Query:fixture)|argument|\n" +
        "|n|2n|\n" +
        "|!style_pass(3)|!style_pass(6)|\n" +
        "|[99] !style_fail(missing)|99|\n" +
        "|[2] !style_fail(surplus)|4|\n"
    );
  }

  @Test
  public void twoRowsSecondMatchesFirstDoesnt() throws Exception {
    assertQueryResults(
      "|99|99|\n" +
        "|2|4|\n",
      list(
        list(list("n", "2"), list("2n", "4")),
        list(list("n", "3"), list("2n", "6"))
      ),
      "|!style_pass(Query:fixture)|argument|\n" +
        "|n|2n|\n" +
        "|[99] !style_fail(missing)|99|\n" +
        "|!style_pass(2)|!style_pass(4)|\n" +
        "|[3] !style_fail(surplus)|6|\n"
    );
  }

  @Test
  public void fieldInMatchingRowDoesntExist() throws Exception {
    assertQueryResults(
      "|3|4|\n",
      list(
        list(list("n", "3"))
      ),
      "|!style_pass(Query:fixture)|argument|\n" +
        "|n|2n|\n" +
        "|!style_pass(3)|[4] !style_fail(field not present)|\n"
    );
  }

  @Test
  public void fieldInSurplusRowDoesntExist() throws Exception {
    assertQueryResults(
      "",
      list(
        list(list("n", "3"))
      ),
      "|!style_pass(Query:fixture)|argument|\n" +
        "|n|2n|\n" +
        "|[3] !style_fail(surplus)|!style_fail(field not present)|\n"
    );
  }

  @Test
  public void variablesAreReplacedInMatch() throws Exception {
    makeQueryTableAndBuildInstructions(queryTableHeader + "|2|$V|\n");
    qt.setSymbol("V", "4");
    Map<String, Object> pseudoResults = SlimClient.resultToMap(
      list(
        list("queryTable_id_0", "OK"),
        list("queryTable_id_1",
          list(
            list(list("n", "2"), list("2n", "4"))
          )
        )
      )
    );
    qt.evaluateExpectations(pseudoResults);
    assertEquals("|!style_pass(Query:fixture)|argument|\n" +
      "|n|2n|\n" +
      "|!style_pass(2)|!style_pass($V->[4])|\n", qt.getTable().toString()
    );
  }

  @Test
  public void variablesAreReplacedInExpected() throws Exception {
    makeQueryTableAndBuildInstructions(queryTableHeader + "|2|$V|\n");
    qt.setSymbol("V", "5");
    Map<String, Object> pseudoResults = SlimClient.resultToMap(
      list(
        list("queryTable_id_0", "OK"),
        list("queryTable_id_1",
          list(
            list(list("n", "2"), list("2n", "4"))
          )
        )
      )
    );
    qt.evaluateExpectations(pseudoResults);
    assertEquals("|!style_pass(Query:fixture)|argument|\n" +
      "|n|2n|\n" +
      "|!style_pass(2)|!style_fail([4] expected [$V->[5]])|\n", qt.getTable().toString()
    );
  }

  @Test
  public void variablesAreReplacedInMissing() throws Exception {
    makeQueryTableAndBuildInstructions(queryTableHeader + "|3|$V|\n");
    qt.setSymbol("V", "5");
    Map<String, Object> pseudoResults = SlimClient.resultToMap(
      list(
        list("queryTable_id_0", "OK"),
        list("queryTable_id_1",
          list(
          )
        )
      )
    );
    qt.evaluateExpectations(pseudoResults);
    assertEquals("|!style_pass(Query:fixture)|argument|\n" +
      "|n|2n|\n" +
      "|[3] !style_fail(missing)|$V->[5]|\n", qt.getTable().toString()
    );
  }
}
