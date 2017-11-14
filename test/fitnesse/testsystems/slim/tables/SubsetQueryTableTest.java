// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import org.junit.Test;

import static java.util.Arrays.asList;

public class SubsetQueryTableTest extends QueryTableTestBase {

  @Override
  protected String tableType() {
    return "subset query";
  }

  @Test
  public void twoMatchingRowsOutOfOrder() throws Exception {
    assertQueryResults(
      "|3|6|\n" +
        "|2|4|\n",
            asList(
              asList(asList("n", "2"), asList("2n", "4")),
              asList(asList("n", "3"), asList("2n", "6"))
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
            asList(
              asList(asList("n", "2"), asList("2n", "4")),
              asList(asList("n", "3"), asList("2n", "6"))
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
            asList(
              asList(asList("n", "3"), asList("2n", "5"))
            ),
      "[" +
        headRow +
        "[n, 2n], " +
        "[fail(e=2;missing), 4]" +
        "]"
    );
  }

  @Test
  @Override
  public void twoRowsFirstMatchesSecondDoesnt() throws Exception {
    assertQueryResults(
            "|3|6|\n" +
            "|99|99|\n",
            asList(
              asList(asList("n", "2"), asList("2n", "4")),
              asList(asList("n", "3"), asList("2n", "6"))
            ),
            "[" +
            headRow +
            "[n, 2n], " +
            "[pass(3), pass(6)], " +
            "[fail(e=99;missing), 99]" +
            "]"
    );
  }

  @Test
  @Override
  public void twoRowsSecondMatchesFirstDoesnt() throws Exception {
    assertQueryResults(
           "|99|99|\n" +
           "|2|4|\n",
            asList(
              asList(asList("n", "2"), asList("2n", "4")),
              asList(asList("n", "3"), asList("2n", "6"))
            ),
            "[" +
              headRow +
              "[n, 2n], " +
              "[fail(e=99;missing), 99], " +
              "[pass(2), pass(4)]" +
              "]"
    );
  }

  @Test
  @Override
  public void fieldInSurplusRowDoesntExist() throws Exception {
    assertQueryResults(
            "",
            asList(
              asList(asList("n", "3"))
            ),
            "[" +
            headRow +
            "[n, 2n]" +
            "]"
    );
  }
}
