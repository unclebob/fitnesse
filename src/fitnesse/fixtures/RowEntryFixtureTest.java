// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import java.text.ParseException;

import junit.framework.TestCase;
import fit.Parse;

public class RowEntryFixtureTest extends TestCase {
  private RowEntryFixture fixture;
  private Parse simpleTable;
  private String ERROR_MESSAGE = "bad input";

  @SuppressWarnings("unused")
  protected void setUp() throws ParseException {
    fixture = new RowEntryFixture() {
      public int a = 0;
      public int b = 0;

      public void enterRow() throws Exception {
        throw new Exception(ERROR_MESSAGE);
      }
    };
    simpleTable = new Parse("<table><tr><td>a</td></tr><tr><td>b</td></tr></table>");
  }

  public void testExplore() {
    assertEquals("<table>", simpleTable.tag);
    assertEquals("<tr>", simpleTable.parts.tag);
    assertEquals("<td>", simpleTable.parts.parts.tag);
    assertEquals("a", simpleTable.parts.parts.body);

    assertEquals("<tr>", simpleTable.parts.more.tag);
    assertEquals("<td>", simpleTable.parts.more.parts.tag);
    assertEquals("b", simpleTable.parts.more.parts.body);
  }

  public void testInsertRowAfter() {
    Parse errorCell = new Parse("td", "error", null, null);
    Parse row = new Parse("tr", null, errorCell, null);
    fixture.insertRowAfter(simpleTable.parts.more, row);

    assertEquals("<tr>", simpleTable.parts.more.more.tag);
    assertEquals("error", simpleTable.parts.more.more.parts.body);
  }

  public void testInsertRowBetween() {
    Parse errorCell = new Parse("td", "error", null, null);
    Parse row = new Parse("tr", null, errorCell, null);
    fixture.insertRowAfter(simpleTable.parts, row);

    assertEquals("<tr>", simpleTable.parts.more.tag);
    assertEquals("error", simpleTable.parts.more.parts.body);

    assertEquals("<tr>", simpleTable.parts.more.more.tag);
    assertEquals("b", simpleTable.parts.more.more.parts.body);
  }

  public void testBadInput() throws ParseException {
    Parse table = new Parse("<table>" +
      "<tr><td>Fixture</td></tr>" +
      "<tr><td>a</td><td>b</td></tr>" +
      "<tr><td>1</td><td>2</td></tr>" +
      "</table>");
    fixture.doTable(table);
    assertTrue(table.at(0, 3, 1).body.indexOf(ERROR_MESSAGE) != -1);
  }

  public void testMessageFormat() throws ParseException {
    Parse table = new Parse("<table>" +
      "<tr><td>Fixture</td></tr>" +
      "<tr><td>a</td><td>b</td></tr>" +
      "<tr><td>1</td><td>2</td></tr>" +
      "</table>");
    fixture.doTable(table);
    assertTrue(table.at(0, 3, 1).tag.indexOf("colspan=\"3\"") != -1);
  }
}
