package fitnesse.slim.test;

import static fitnesse.util.ListUtility.list;

import java.util.List;

public class TestQuery {
  private int n;

  public TestQuery(int n) {
    this.n = n;
  }

  public List<Object> query() {
    List<Object> table = list();
    for (int i = 1; i <= n; i++) {
      List<String> ncol = list("n", String.valueOf(i));
      List<String> n2col = list("2n", String.valueOf(2 * i));
      List<Object> row = list(ncol, n2col);
      table.add(row);
    }
    return table;
  }
}
