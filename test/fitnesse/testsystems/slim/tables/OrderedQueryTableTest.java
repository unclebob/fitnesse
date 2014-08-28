package fitnesse.testsystems.slim.tables;

import fitnesse.testsystems.slim.SlimCommandRunningClient;
import org.junit.Test;
import util.ListUtility;

import java.util.Map;

import static util.ListUtility.list;

public class OrderedQueryTableTest extends QueryTableTestBase {
  @Override
  protected Class<? extends QueryTable> queryTableClass() {
    return OrderedQueryTable.class;
  }

  @Override
  protected String tableType() {
    return "ordered query";
  }

  @Test
  public void twoMatchingRowsOutOfOrder() throws Exception {
    assertQueryResults(
      "|3|6|\n" +
        "|2|4|\n",
            ListUtility.<Object>list(
                    list(list("n", "2"), list("2n", "4")),
                    list(list("n", "3"), list("2n", "6"))
            ),
      "[" +
        headRow +
        "[n, 2n], " +
        "[pass(3), pass(6)], " +
        "[fail(e=2;out of order: row 1), pass(4)]" +
        "]"
    );
  }

  @Test
  public void threeMatchingRowsOutOfOrder() throws Exception {
    assertQueryResults(
      "|3|6|\n" +
        "|7|5|\n" +
        "|2|4|\n",
            ListUtility.<Object>list(
                    list(list("n", "2"), list("2n", "4")),
                    list(list("n", "7"), list("2n", "5")),
                    list(list("n", "3"), list("2n", "6"))
            ),
      "[" +
        headRow +
        "[n, 2n], " +
        "[pass(3), pass(6)], " +
        "[fail(e=7;out of order: row 2), pass(5)], " +
        "[fail(e=2;out of order: row 1), pass(4)]" +
        "]"
    );
  }

  @Test
  public void threeRowsOneMissingTwoOutOfOrder() throws Exception {
    assertQueryResults(
      "|3|6|\n" +
        "|7|5|\n" +
        "|2|4|\n",
            ListUtility.<Object>list(
                    list(list("n", "2"), list("2n", "4")),
                    list(list("n", "3"), list("2n", "6"))
            ),
      "[" +
        headRow +
        "[n, 2n], " +
        "[pass(3), pass(6)], " +
        "[fail(e=7;missing), 5], " +
        "[fail(e=2;out of order: row 1), pass(4)]]"
    );
  }

  @Test
  public void githubIssue481() throws Exception {
    makeQueryTableAndBuildInstructions("|" + tableType() + ":fixture|argument|\n" +
      "|a|b|c|d|e|\n" +
      "|010301|201107|201105|201105|L|\n" +
      "|010301|201107|201106|201106|L|\n" +
      "|010301|201107|201107|201107|V|\n" +
      "|010302|201107|201107|201105|G|\n" +
      "|010302|201107|201107|201106|G|\n" +
      "|010302|201107|201107|201107|G|\n");
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(list(
      list("queryTable_id_0", "OK"),
      list("queryTable_id_1", "blah"),
      list("queryTable_id_2",
        util.ListUtility.<Object>list(
          util.ListUtility.list(util.ListUtility.list("a", "010301"), util.ListUtility.list("b", "201107"), util.ListUtility.list("c", "201105"), util.ListUtility.list("d", "201105"), util.ListUtility.list("e", "L")),
          util.ListUtility.list(util.ListUtility.list("a", "010301"), util.ListUtility.list("b", "201107"), util.ListUtility.list("c", "201106"), util.ListUtility.list("d", "201106"), util.ListUtility.list("e", "L")),
          util.ListUtility.list(util.ListUtility.list("a", "010301"), util.ListUtility.list("b", "201107"), util.ListUtility.list("c", "201107"), util.ListUtility.list("d", "201107"), util.ListUtility.list("e", "V")),
          util.ListUtility.list(util.ListUtility.list("a", "010302"), util.ListUtility.list("b", "201107"), util.ListUtility.list("c", "201107"), util.ListUtility.list("d", "201105"), util.ListUtility.list("e", "G")),
          util.ListUtility.list(util.ListUtility.list("a", "010302"), util.ListUtility.list("b", "201107"), util.ListUtility.list("c", "201107"), util.ListUtility.list("d", "201106"), util.ListUtility.list("e", "G")),
          util.ListUtility.list(util.ListUtility.list("a", "010302"), util.ListUtility.list("b", "201107"), util.ListUtility.list("c", "201107"), util.ListUtility.list("d", "201107"), util.ListUtility.list("e", "G")))
      )));
    evaluateResults(pseudoResults, "[" +
      headRow +
      "[a, b, c, d, e], " +
      "[pass(010301), pass(201107), pass(201105), pass(201105), pass(L)], " +
      "[pass(010301), pass(201107), pass(201106), pass(201106), pass(L)], " +
      "[pass(010301), pass(201107), pass(201107), pass(201107), pass(V)], " +
      "[pass(010302), pass(201107), pass(201107), pass(201105), pass(G)], " +
      "[pass(010302), pass(201107), pass(201107), pass(201106), pass(G)], " +
      "[pass(010302), pass(201107), pass(201107), pass(201107), pass(G)]]");
  }
}
