// Copyright (C) 2013 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fit;

import java.text.ParseException;

import junit.framework.TestCase;


public class DispatcherTest extends TestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testRelationalMatching() throws Exception {
    final String[][] table = {
      {"fitnesse.fixtures.ColumnFixtureTestFixture"},
      {"input", "output?"},
      {"1", "_>0"}
    };
    Parse page = executeFixture(table);
    String colTwoResult = page.at(0, 2, 1).body;
    assertTrue(colTwoResult.contains("<b>1</b>>0"));
    String colTwoTag = page.at(0, 2, 1).tag;
    assertTrue(colTwoTag.contains("pass"));
  }

  public static Parse executeFixture(String[][] table) throws ParseException {
    String pageString = makeFixtureTable(table);
    Parse page = new Parse(pageString);
    Dispatcher dispatcher = new Dispatcher();
    dispatcher.doTables(page);
    return page;
  }

  private static String makeFixtureTable(String table[][]) {
    StringBuffer buf = new StringBuffer();
    buf.append("<table>\n");
    for (int ri = 0; ri < table.length; ri++) {
      buf.append("  <tr>");
      String[] row = table[ri];
      for (int ci = 0; ci < row.length; ci++) {
        String cell = row[ci];
        buf.append("<td>").append(cell).append("</td>");
      }
      buf.append("</tr>\n");
    }
    buf.append("</table>\n");
    return buf.toString();
  }
}