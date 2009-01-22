// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.slimResponder;

import fitnesse.slim.SlimClient;
import static fitnesse.util.ListUtility.list;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wikitext.Utils;
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
  private MockSlimTestContext testContext;

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
    TableScanner ts = new HtmlTableScanner(root.getData().getHtml());
    Table t = ts.getTable(0);
    testContext = new MockSlimTestContext();
    return new QueryTable(t, "id", testContext);
  }

  private void assertQueryResults(String queryRows, List<Object> queryResults, String table) throws Exception {
    makeQueryTableAndBuildInstructions(queryTableHeader + queryRows);
    Map<String, Object> pseudoResults = SlimClient.resultToMap(list(
      list("queryTable_id_0", "OK"),
      list("queryTable_id_1", queryResults)
    )
    );
    testContext.evaluateExpectations(pseudoResults);
    qt.evaluateReturnValues(pseudoResults);
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
      "[" +
        "[pass(Query:fixture), argument], " +
        "[n, 2n]" +
        "]"
    );
  }

  @Test
  public void oneRowThatMatches() throws Exception {
    assertQueryResults("|2|4|\n",
      list(
        list(list("n", "2"), list("2n", "4"))
      ),
      "[" +
        "[pass(Query:fixture), argument], " +
        "[n, 2n], " +
        "[pass(2), pass(4)]" +
        "]"
    );
  }

  @Test
  public void oneRowThatFails() throws Exception {
    assertQueryResults("|2|4|\n",
      list(
        list(list("n", "3"), list("2n", "5"))
      ),
      "[" +
        "[pass(Query:fixture), argument], " +
        "[n, 2n], " +
        "[[2] fail(missing), 4], " +
        "[[3] fail(surplus), 5]" +
        "]"
    );
  }

  @Test
  public void oneRowWithPartialMatch() throws Exception {
    assertQueryResults("|2|4|\n",
      list(
        list(list("n", "2"), list("2n", "5"))
      ),
      "[" +
        "[pass(Query:fixture), argument], " +
        "[n, 2n], " +
        "[pass(2), [5] fail(expected [4])]" +
        "]"
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
      "[" +
        "[pass(Query:fixture), argument], " +
        "[n, 2n], " +
        "[pass(2), pass(4)], " +
        "[pass(3), pass(6)]" +
        "]"
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
      "[" +
        "[pass(Query:fixture), argument], " +
        "[n, 2n], " +
        "[pass(3), pass(6)], " +
        "[pass(2), pass(4)]" +
        "]"
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
      "[" +
        "[pass(Query:fixture), argument], " +
        "[n, 2n], " +
        "[pass(3), pass(6)], " +
        "[[99] fail(missing), 99], " +
        "[[2] fail(surplus), 4]" +
        "]"
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
      "[" +
        "[pass(Query:fixture), argument], " +
        "[n, 2n], " +
        "[[99] fail(missing), 99], " +
        "[pass(2), pass(4)], " +
        "[[3] fail(surplus), 6]" +
        "]"
    );
  }

  @Test
  public void fieldInMatchingRowDoesntExist() throws Exception {
    assertQueryResults(
      "|3|4|\n",
      list(
        list(list("n", "3"))
      ),
      "[" +
        "[pass(Query:fixture), argument], " +
        "[n, 2n], " +
        "[pass(3), [4] fail(field not present)]" +
        "]"
    );
  }

  @Test
  public void fieldInSurplusRowDoesntExist() throws Exception {
    assertQueryResults(
      "",
      list(
        list(list("n", "3"))
      ),
      "[" +
        "[pass(Query:fixture), argument], " +
        "[n, 2n], " +
        "[[3] fail(surplus), fail(field not present)]" +
        "]"
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
    testContext.evaluateExpectations(pseudoResults);
    qt.evaluateReturnValues(pseudoResults);
    assertEquals(
      "[" +
        "[pass(Query:fixture), argument], " +
        "[n, 2n], " +
        "[pass(2), pass($V->[4])]" +
        "]",
      Utils.unescapeWiki(qt.getTable().toString())
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
    testContext.evaluateExpectations(pseudoResults);
    qt.evaluateReturnValues(pseudoResults);
    assertEquals(
      "[" +
        "[pass(Query:fixture), argument], " +
        "[n, 2n], " +
        "[pass(2), [4] fail(expected [$V->[5]])]" +
        "]",
      Utils.unescapeWiki(qt.getTable().toString())
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
    testContext.evaluateExpectations(pseudoResults);
    qt.evaluateReturnValues(pseudoResults);
    assertEquals(
      "[" +
        "[pass(Query:fixture), argument], " +
        "[n, 2n], " +
        "[[3] fail(missing), $V->[5]]" +
        "]", qt.getTable().toString()
    );
  }
}
