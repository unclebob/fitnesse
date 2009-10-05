// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slimTables;

import org.junit.Test;

public class SubsetQueryTableTest extends QueryTableBaseTest {

  protected String tableType() {
    return "subset query";
  }

  protected Class<SubsetQueryTable> queryTableClass() {
    return SubsetQueryTable.class;
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

  @Test
  public void oneMatchingRowOutOfTwo() throws Exception {
    assertQueryResults(
        "|2|4|\n",
      util.ListUtility.list(
        util.ListUtility.list(util.ListUtility.list("n", "2"), util.ListUtility.list("2n", "4")),
        util.ListUtility.list(util.ListUtility.list("n", "3"), util.ListUtility.list("2n", "6"))
      ),
      "[" +
        headRow +
        "[n, 2n], " +
        "[pass(2), pass(4)]" +
        "]"
    );
  }

  @Test
  @Override
  public void oneRowThatFails() throws Exception {
    assertQueryResults("|2|4|\n",
      util.ListUtility.list(
        util.ListUtility.list(util.ListUtility.list("n", "3"), util.ListUtility.list("2n", "5"))
      ),
      "[" +
        headRow +
        "[n, 2n], " +
        "[[2] fail(missing), 4]" +
        "]"
    );
  }

  @Test
  @Override
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
        "[[99] fail(missing), 99]" +
        "]"
    );
  }

  @Test
  @Override
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
        "[pass(2), pass(4)]" +
        "]"
    );
  }

  @Test
  @Override
  public void fieldInSurplusRowDoesntExist() throws Exception {
    assertQueryResults(
      "",
      util.ListUtility.list(
        util.ListUtility.list(util.ListUtility.list("n", "3"))
      ),
      "[" +
        headRow +
        "[n, 2n]" +
        "]"
    );
  }
}
