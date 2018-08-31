// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import java.util.Map;

import fitnesse.testsystems.slim.SlimCommandRunningClient;
import org.junit.Test;

import static java.util.Arrays.asList;

public class QueryTableTest extends QueryTableTestBase {

  @Override
  protected String tableType() {
    return "query";
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
        "[pass(2), pass(4)]" +
        "]"
    );
  }

  /* When one row is missing and the other is only partially matched, choose the right one to be marked 'missing'. */
  @Test
  public void oneRowMissingOtherPartiallyMatched() throws Exception {
    makeQueryTableAndBuildInstructions("|" + tableType() + ":fixture|argument|\n" +
      "|x|n|2n|\n" +
      "|1|2|4|\n" +
      "|1|3|6|\n");
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(
                    asList("queryTable_id_0", "OK"),
                    asList("queryTable_id_1", "blah"),
                    asList("queryTable_id_2",
                            asList(
                                    asList(
                                            asList("x", "1"),
                                            asList("n", "3"),
                                            asList("2n", "5"))))));
    evaluateResults(pseudoResults, "[" +
        headRow +
        "[x, n, 2n], " +
        "[fail(e=1;missing), 2, 4], " +
        "[pass(1), pass(3), fail(a=5;e=6)]" +
        "]");
  }
}
