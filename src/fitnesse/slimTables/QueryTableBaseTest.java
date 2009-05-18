package fitnesse.slimTables;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import fitnesse.responders.run.slimResponder.MockSlimTestContext;
import fitnesse.responders.run.slimResponder.SlimTestContext;
import fitnesse.slim.SlimClient;
import fitnesse.slim.converters.VoidConverter;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wikitext.Utils;

public abstract class QueryTableBaseTest {
  private WikiPage root;
  private List<Object> instructions;
  private String queryTableHeader;
  public QueryTable qt;
  private MockSlimTestContext testContext;
  protected String headRow;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    instructions = new ArrayList<Object>();
    queryTableHeader =
      "|" + tableType() + ":fixture|argument|\n" +
        "|n|2n|\n";
    headRow = "[pass(" + tableType() + ":fixture), argument], ";
  }

  protected abstract String tableType();

  protected abstract Class<? extends QueryTable> queryTableClass();

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
    return constructQueryTable(t);
  }

  private QueryTable constructQueryTable(Table t) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
    Class<? extends QueryTable> queryTableClass = queryTableClass();
    Constructor<? extends QueryTable> constructor = queryTableClass.getConstructor(Table.class, String.class, SlimTestContext.class);
    return constructor.newInstance(t, "id", testContext);
  }

  protected void assertQueryResults(String queryRows, List<Object> queryResults, String table) throws Exception {
    makeQueryTableAndBuildInstructions(queryTableHeader + queryRows);
    Map<String, Object> pseudoResults = SlimClient.resultToMap(util.ListUtility.list(
      util.ListUtility.list("queryTable_id_0", "OK"),
      util.ListUtility.list("queryTable_id_1", "blah"),
      util.ListUtility.list("queryTable_id_2", queryResults)
    )
    );
    testContext.evaluateExpectations(pseudoResults);
    qt.evaluateReturnValues(pseudoResults);
    org.junit.Assert.assertEquals(table, qt.getTable().toString());
  }

  @Test
  public void instructionsForQueryTable() throws Exception {
    makeQueryTableAndBuildInstructions(queryTableHeader);
    List<Object> expectedInstructions = util.ListUtility.list(
      util.ListUtility.list("queryTable_id_0", "make", "queryTable_id", "fixture", "argument"),
      util.ListUtility.list("queryTable_id_1", "call", "queryTable_id", "table", util.ListUtility.list(util.ListUtility.list("n", "2n"))),
      util.ListUtility.list("queryTable_id_2", "call", "queryTable_id", "query")
    );
    org.junit.Assert.assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void nullResultsForNullTable() throws Exception {
    assertQueryResults("", util.ListUtility.list(),
      "[" +
        headRow +
        "[n, 2n]" +
        "]"
    );
  }

  @Test
  public void oneRowThatMatches() throws Exception {
    assertQueryResults("|2|4|\n",
      util.ListUtility.list(
        util.ListUtility.list(util.ListUtility.list("n", "2"), util.ListUtility.list("2n", "4"))
      ),
      "[" +
        headRow +
        "[n, 2n], " +
        "[pass(2), pass(4)]" +
        "]"
    );
  }

  @Test
  public void oneRowThatFails() throws Exception {
    assertQueryResults("|2|4|\n",
      util.ListUtility.list(
        util.ListUtility.list(util.ListUtility.list("n", "3"), util.ListUtility.list("2n", "5"))
      ),
      "[" +
        headRow +
        "[n, 2n], " +
        "[[2] fail(missing), 4], " +
        "[[3] fail(surplus), 5]" +
        "]"
    );
  }

  @Test
  public void oneRowWithPartialMatch() throws Exception {
    assertQueryResults("|2|4|\n",
      util.ListUtility.list(
        util.ListUtility.list(util.ListUtility.list("n", "2"), util.ListUtility.list("2n", "5"))
      ),
      "[" +
        headRow +
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
      util.ListUtility.list(
        util.ListUtility.list(util.ListUtility.list("n", "2"), util.ListUtility.list("2n", "4")),
        util.ListUtility.list(util.ListUtility.list("n", "3"), util.ListUtility.list("2n", "6"))
      ),
      "[" +
        headRow +
        "[n, 2n], " +
        "[pass(2), pass(4)], " +
        "[pass(3), pass(6)]" +
        "]"
    );
  }

  @Test
  public void twoRowsFirstMatchesSecondDoesnt() throws Exception {
    assertQueryResults(
      "|3|6|\n" +
        "|99|99|\n",
      util.ListUtility.list(
        util.ListUtility.list(util.ListUtility.list("n", "2"), util.ListUtility.list("2n", "4")),
        util.ListUtility.list(util.ListUtility.list("n", "3"), util.ListUtility.list("2n", "6"))
      ),
      "[" +
        headRow +
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
      util.ListUtility.list(
        util.ListUtility.list(util.ListUtility.list("n", "2"), util.ListUtility.list("2n", "4")),
        util.ListUtility.list(util.ListUtility.list("n", "3"), util.ListUtility.list("2n", "6"))
      ),
      "[" +
        headRow +
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
      util.ListUtility.list(
        util.ListUtility.list(util.ListUtility.list("n", "3"))
      ),
      "[" +
        headRow +
        "[n, 2n], " +
        "[pass(3), [4] fail(field not present)]" +
        "]"
    );
  }

  @Test
  public void fieldInSurplusRowDoesntExist() throws Exception {
    assertQueryResults(
      "",
      util.ListUtility.list(
        util.ListUtility.list(util.ListUtility.list("n", "3"))
      ),
      "[" +
        headRow +
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
      util.ListUtility.list(
        util.ListUtility.list("queryTable_id_0", "OK"),
        util.ListUtility.list("queryTable_id_1", VoidConverter.VOID_TAG),
        util.ListUtility.list("queryTable_id_2",
          util.ListUtility.list(
            util.ListUtility.list(util.ListUtility.list("n", "2"), util.ListUtility.list("2n", "4"))
          )
        )
      )
    );
    testContext.evaluateExpectations(pseudoResults);
    qt.evaluateReturnValues(pseudoResults);
    org.junit.Assert.assertEquals(
      "[" +
        headRow +
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
      util.ListUtility.list(
        util.ListUtility.list("queryTable_id_0", "OK"),
        util.ListUtility.list("queryTable_id_1", VoidConverter.VOID_TAG),
        util.ListUtility.list("queryTable_id_2",
          util.ListUtility.list(
            util.ListUtility.list(util.ListUtility.list("n", "2"), util.ListUtility.list("2n", "4"))
          )
        )
      )
    );
    testContext.evaluateExpectations(pseudoResults);
    qt.evaluateReturnValues(pseudoResults);
    org.junit.Assert.assertEquals(
      "[" +
        headRow +
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
      util.ListUtility.list(
        util.ListUtility.list("queryTable_id_0", "OK"),
        util.ListUtility.list("queryTable_id_1", VoidConverter.VOID_TAG),
        util.ListUtility.list("queryTable_id_2",
          util.ListUtility.list(
          )
        )
      )
    );
    testContext.evaluateExpectations(pseudoResults);
    qt.evaluateReturnValues(pseudoResults);
    org.junit.Assert.assertEquals(
      "[" +
        headRow +
        "[n, 2n], " +
        "[[3] fail(missing), $V->[5]]" +
        "]", qt.getTable().toString()
    );
  }
}
