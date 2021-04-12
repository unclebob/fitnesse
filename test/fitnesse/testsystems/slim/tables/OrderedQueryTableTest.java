package fitnesse.testsystems.slim.tables;

import java.util.Map;

import fitnesse.testsystems.slim.SlimCommandRunningClient;
import org.junit.Test;

import static java.util.Arrays.asList;

public class OrderedQueryTableTest extends QueryTableTestBase {

  @Override
  protected String tableType() {
    return "ordered query";
  }

  @Test
  public void twoMatchingRowsOutOfOrder() throws Exception {
    assertQueryResults(
      "|3|6|\n" +
        "|2|4|\n",
            asList(
                    asList(
                            asList("n", "2"),
                            asList("2n", "4")),
                    asList(
                            asList("n", "3"),
                            asList("2n", "6"))),
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
            asList(
                    asList(
                            asList("n", "2"),
                            asList("2n", "4")),
                    asList(
                            asList("n", "7"),
                            asList("2n", "5")),
                    asList(
                            asList("n", "3"),
                            asList("2n", "6"))),
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
            asList(
                    asList(
                            asList("n", "2"),
                            asList("2n", "4")),
                    asList(
                            asList("n", "3"),
                            asList("2n", "6"))),
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
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(
                    asList("queryTable_id_0", "OK"),
                    asList("queryTable_id_1", "blah"),
                    asList("queryTable_id_2",
                            asList(
                                    asList(
                                            asList("a", "010301"),
                                            asList("b", "201107"),
                                            asList("c", "201105"),
                                            asList("d", "201105"),
                                            asList("e", "L")),
                                    asList(
                                            asList("a", "010301"),
                                            asList("b", "201107"),
                                            asList("c", "201106"),
                                            asList("d", "201106"),
                                            asList("e", "L")),
                                    asList(
                                            asList("a", "010301"),
                                            asList("b", "201107"),
                                            asList("c", "201107"),
                                            asList("d", "201107"),
                                            asList("e", "V")),
                                    asList(
                                            asList("a", "010302"),
                                            asList("b", "201107"),
                                            asList("c", "201107"),
                                            asList("d", "201105"),
                                            asList("e", "G")),
                                    asList(
                                            asList("a", "010302"),
                                            asList("b", "201107"),
                                            asList("c", "201107"),
                                            asList("d", "201106"),
                                            asList("e", "G")),
                                    asList(
                                            asList("a", "010302"),
                                            asList("b", "201107"),
                                            asList("c", "201107"),
                                            asList("d", "201107"),
                                            asList("e", "G"))))));
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
