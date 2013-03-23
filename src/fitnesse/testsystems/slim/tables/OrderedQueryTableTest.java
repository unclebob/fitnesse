package fitnesse.testsystems.slim.tables;

import org.junit.Test;
import util.ListUtility;

import static util.ListUtility.list;

public class OrderedQueryTableTest extends QueryTableBaseTest {
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
}
