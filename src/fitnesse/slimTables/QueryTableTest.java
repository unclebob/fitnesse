// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slimTables;

import org.junit.Test;

public class QueryTableTest extends QueryTableBaseTest {

  protected String tableType() {
    return "query";
  }

  protected Class<QueryTable> queryTableClass() {
    return QueryTable.class;
  }

  @Test
  public void twoMatchingRowsOutOfOrder() throws Exception {
    assertQueryResults(
      "|3|6|\n" +
        "|2|4|\n",
      util.ListUtility.list(
        util.ListUtility.list(util.ListUtility.list("n", "2"), util.ListUtility.list("2n", "4")),
        util.ListUtility.list(util.ListUtility.list("n", "3"), util.ListUtility.list("2n", "6"))
      ),
      "[" +
        headRow +
        "[n, 2n], " +
        "[pass(3), pass(6)], " +
        "[pass(2), pass(4)]" +
        "]"
    );
  }
}
