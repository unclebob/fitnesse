// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slimTables;

import static org.junit.Assert.assertEquals;
import static util.ListUtility.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import fitnesse.responders.run.slimResponder.MockSlimTestContext;
import fitnesse.slim.SlimClient;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;

public class TableTableTest {
  private WikiPage root;
  private List<Object> instructions;
  private final String tableTableHeader =
    "|Table:fixture|argument|\n";

  public TableTable tt;
  private MockSlimTestContext testContext;

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
    testContext = new MockSlimTestContext();
    return new TableTable(t, "id", testContext);
  }

  private void assertTableResults(String tableRows, List<Object> tableResults, String table) throws Exception {
    makeTableTableAndBuildInstructions(tableTableHeader + tableRows);
    Map<String, Object> pseudoResults = SlimClient.resultToMap(
      list(
        list("tableTable_id_0", "OK"),
        list("tableTable_id_1", tableResults)
      )
    );
    testContext.evaluateExpectations(pseudoResults);
    tt.evaluateReturnValues(pseudoResults);
    assertEquals(table, tt.getTable().toString());
  }

  @Test
  public void instructionsForEmptyTableTable() throws Exception {
    makeTableTableAndBuildInstructions(tableTableHeader);
    List<Object> expectedInstructions = list(
      list("tableTable_id_0", "make", "tableTable_id", "fixture", "argument"),
      list("tableTable_id_1", "call", "tableTable_id", "doTable", list())
    );
    assertEquals(expectedInstructions, instructions);
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
  public void oneRowThatPassesUnchanged() throws Exception {
    assertTableResults("|2|4|\n",
      list(
        list("pass", "pass")
      ),
      "[[pass(Table:fixture), argument], [pass(2), pass(4)]]"
    );
  }

  @Test
  public void oneRowThatPassesChanged() throws Exception {
    assertTableResults("|2|4|\n",
      list(
        list("pass:x", "pass:y")
      ),
      "[[pass(Table:fixture), argument], [pass(x), pass(y)]]"
    );
  }

  @Test
  public void oneRowThatImplicitlyFails() throws Exception {
    assertTableResults("|2|4|\n",
      list(
        list("bad", "boy")
      ),
      "[[pass(Table:fixture), argument], [fail(bad), fail(boy)]]"
    );
  }

  @Test
  public void oneRowThatImplicitlyFailsWithColon() throws Exception {
    assertTableResults("|2|4|\n",
      list(
        list("x:bad", "x:boy")
      ),
      "[[pass(Table:fixture), argument], [fail(x:bad), fail(x:boy)]]"
    );
  }

  @Test
  public void oneRowThatExplicitlyFails() throws Exception {
    assertTableResults("|2|4|\n",
      list(
        list("fail:bad", "fail:boy")
      ),
      "[[pass(Table:fixture), argument], [fail(bad), fail(boy)]]"
    );
  }

  @Test
  public void oneRowThatExplicitlyFailsNoChange() throws Exception {
    assertTableResults("|2|4|\n",
      list(
        list("fail", "fail")
      ),
      "[[pass(Table:fixture), argument], [fail(2), fail(4)]]"
    );
  }

  @Test
  public void oneRowThatExplicitlyIgnoresNoChange() throws Exception {
    assertTableResults("|2|4|\n",
      list(
        list("ignore", "ignore")
      ),
      "[[pass(Table:fixture), argument], [ignore(2), ignore(4)]]"
    );
  }

  @Test
  public void oneRowThatExplicitlyIgnoresWithChange() throws Exception {
    assertTableResults("|2|4|\n",
      list(
        list("ignore:x", "ignore:y")
      ),
      "[[pass(Table:fixture), argument], [ignore(x), ignore(y)]]"
    );
  }

  @Test
  public void oneRowThatReports() throws Exception {
    assertTableResults("|2|4|\n",
      list(
        list("report:x", "report:y")
      ),
      "[[pass(Table:fixture), argument], [x, y]]"
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

  @Test
  public void surplusErrors() throws Exception {
    assertTableResults("|2|4|\n",
      list(
        list("", "", "error:surplus A"),
        list("error:surplus B", "error:surplus C")
      ),
      "[[pass(Table:fixture), argument], [2, 4, error(surplus A)], [error(surplus B), error(surplus C)]]"
    );
  }

  @Test
  public void surplusFailures() throws Exception {
    assertTableResults("|2|4|\n",
      list(
        list("", "", "fail:surplus A"),
        list("fail:surplus B", "fail:surplus C")
      ),
      "[[pass(Table:fixture), argument], [2, 4, fail(surplus A)], [fail(surplus B), fail(surplus C)]]"
    );
  }

  @Test
  public void surplusImplicitFailures() throws Exception {
    assertTableResults("|2|4|\n",
      list(
        list("", "", "fail"),
        list("fail", "fail")
      ),
      "[[pass(Table:fixture), argument], [2, 4, fail(fail)], [fail(fail), fail(fail)]]"
    );
  }

  @Test
  public void surplusImplicitPasses() throws Exception {
    assertTableResults("|2|4|\n",
      list(
        list("", "", "pass"),
        list("pass", "pass")
      ),
      "[[pass(Table:fixture), argument], [2, 4, pass(pass)], [pass(pass), pass(pass)]]"
    );
  }

  @Test
  public void surplusExplicitPasses() throws Exception {
    assertTableResults("|2|4|\n",
      list(
        list("", "", "pass:x"),
        list("pass:y", "pass:z")
      ),
      "[[pass(Table:fixture), argument], [2, 4, pass(x)], [pass(y), pass(z)]]"
    );
  }

  @Test
  public void emptyTableWithResults() throws Exception {
    assertTableResults("",
      list(
        list("", "pass:x"),
        list("pass:y", "pass:z")
      ),
      "[[pass(Table:fixture), argument], [, pass(x)], [pass(y), pass(z)]]"
    );
  }
}
