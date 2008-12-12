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

public class TableTableTest {
  private WikiPage root;
  private List<Object> instructions;
  private final String tableTableHeader =
    "|Table:fixture|argument|\n";

  public TableTable tt;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    instructions = new ArrayList<Object>();
  }

  private TableTable makeTableTableAndBuildInstructions(String pageContents) throws Exception {
    tt = makeTableTable(pageContents);
    tt.appendInstructions(instructions);
    return tt;
  }

  private TableTable makeTableTable(String tableText) throws Exception {
    WikiPageUtil.setPageContents(root, tableText);
    TableScanner ts = new HtmlTableScanner(root.getData().getHtml());
    Table t = ts.getTable(0);
    return new TableTable(t, "id");
  }

  private void assertTableResults(String tableRows, List<Object> tableResults, String table) throws Exception {
    makeTableTableAndBuildInstructions(tableTableHeader + tableRows);
    Map<String, Object> pseudoResults = SlimClient.resultToMap(
      list(
        list("tableTable_id_0", "OK"),
        list("tableTable_id_1", tableResults)
      )
    );
    tt.evaluateExpectations(pseudoResults);
    assertEquals(table, tt.getTable().toString());
  }

  @Test
  public void instructionsForTableTable() throws Exception {
    makeTableTableAndBuildInstructions(tableTableHeader + "|a|b|\n|x|y|\n");
    List<Object> expectedInstructions = list(
      list("tableTable_id_0", "make", "tableTable_id", "fixture", "argument"),
      list("tableTable_id_1", "call", "tableTable_id", "doTable", list(list("a", "b"), list("x", "y")))
    );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void oneRowThatPasses() throws Exception {
    assertTableResults("|2|4|\n",
      list(
        list("pass", "pass")
      ),
      "[[pass(Table:fixture), argument], [pass(2), pass(4)]]"
    );
  }

  @Test
  public void oneRowThatFails() throws Exception {
    assertTableResults("|2|4|\n",
      list(
        list("bad", "boy")
      ),
      "[[pass(Table:fixture), argument], [fail(bad), fail(boy)]]"
    );
  }

  @Test
  public void noChange() throws Exception {
    assertTableResults("|2|4|\n",
      list(
        list("no change", "no change")
      ),
      "[[pass(Table:fixture), argument], [2, 4]]"
    );
  }

  @Test
  public void blankNoChange() throws Exception {
    assertTableResults("|2|4|\n",
      list(
        list("", "")
      ),
      "[[pass(Table:fixture), argument], [2, 4]]"
    );
  }

  @Test
  public void error() throws Exception {
    assertTableResults("|2|4|\n",
      list(
        list("error:myError", "error:anError")
      ),
      "[[pass(Table:fixture), argument], [error(myError), error(anError)]]"
    );
  }
}
