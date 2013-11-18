// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import java.util.Arrays;

import org.htmlparser.util.ParserException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static util.RegexTestCase.assertHasRegexp;

public class HtmlTableScannerTest {
  private HtmlTableScanner ts;

  private void scan(String page) throws ParserException {
    ts = new HtmlTableScanner(page);
  }

  @Test
  public void canHandleEmptyInput() throws Exception {
    scan("");
    assertEquals(0, ts.getTableCount());
  }

  @Test
  public void canParseOneSimpleTable() throws Exception {
    scan("<table><tr><td>x</td></tr></table>");
    assertEquals(1, ts.getTableCount());
    Table t = ts.getTable(0);
    assertEquals(1, t.getRowCount());
    assertEquals(1, t.getColumnCountInRow(0));
    assertEquals("x", t.getCellContents(0, 0));
  }


  @Test
  public void canParseOneSimpleTableWithGunkAroundIt() throws Exception {
    scan(
      "" +
        "<body>Gunk" +
        "<table>gunk" +
        "  <tr> gunk" +
        "     <td>x</td>gunk" +
        "   </tr>gunk" +
        "</table>gunk" +
        "</body>");
    assertEquals(1, ts.getTableCount());
    Table t = ts.getTable(0);
    assertEquals(1, t.getRowCount());
    assertEquals(1, t.getColumnCountInRow(0));
    assertEquals("x", t.getCellContents(0, 0));
  }

  @Test
  public void canParseComplexTable() throws Exception {
    scan(
      "  <table>" +
        "  <th>" +
        "    <td>Name</td>" +
        "    <td>Address</td>" +
        "  </th>" +
        "  <tr>" +
        "    <td>Bob</td>" +
        "    <td>Here</td>" +
        "  </tr>" +
        "</table>  ");
    assertEquals(1, ts.getTableCount());
    Table t = ts.getTable(0);
    assertEquals(2, t.getRowCount());
    assertEquals(2, t.getColumnCountInRow(0));
    assertEquals(2, t.getColumnCountInRow(1));
    assertEquals("Name", t.getCellContents(0, 0));
    assertEquals("Address", t.getCellContents(1, 0));
    assertEquals("Bob", t.getCellContents(0, 1));
    assertEquals("Here", t.getCellContents(1, 1));
  }

  @Test
  public void canParseMultipleTables() throws Exception {
    scan(
      "" +
        "<table><tr><td>1</td></tr></table>" +
        "<table><tr><td>2</td></tr></table>"
    );

    assertEquals(2, ts.getTableCount());
    Table t1 = ts.getTable(0);
    Table t2 = ts.getTable(1);
    assertEquals(1, t1.getRowCount());
    assertEquals(1, t2.getRowCount());
    assertEquals(1, t1.getColumnCountInRow(0));
    assertEquals(1, t2.getColumnCountInRow(0));
    assertEquals("1", t1.getCellContents(0, 0));
    assertEquals("2", t2.getCellContents(0, 0));
  }

  @Test
  public void canSetCellContents() throws Exception {
    scan("<table><tr><td>x</td></tr></table>");
    Table t = ts.getTable(0);
    t.substitute(0, 0, "Wow");
    assertEquals("Wow", t.getCellContents(0, 0));
  }

  @Test
  public void canAppendCellToRow() throws Exception {
    scan("<table><tr><td>x</td></tr></table>");
    Table t = ts.getTable(0);
    t.addColumnToRow(0, "ray");
    assertEquals("ray", t.getCellContents(1, 0));
  }

  @Test
  public void canAddRow() throws Exception {
    scan("<table><tr><td>x</td></tr></table>");
    Table t = ts.getTable(0);
    t.addRow(Arrays.asList("y", "z"));
    assertEquals("y", t.getCellContents(0, 1));
    assertEquals("z", t.getCellContents(1, 1));
  }

  @Test
  public void canConvertBackToHtmlAfterAddingRows() throws Exception {
    scan("<table><tr><td>x</td></tr></table>");
    Table t = ts.getTable(0);
    t.addRow(Arrays.asList("y", "z"));
    assertHasRegexp("<table><tr><td>x</td></tr><tr><td>y</td><td>z</td></tr></table>",ts.toHtml().toLowerCase());
  }


  @Test
  public void canConvertGunkBackToHtml() throws Exception {
    String html = "gunk<body>gunk<table>gunk<tr>gunk<td>x</td>gunk<br>gunk</tr>gunk</table>gunk</body>";
    scan(html);
    assertHasRegexp(html, ts.toHtml());
  }

  @Test
  public void nonBreakingSpaceInTableCellReturnsEmptyString() throws Exception {
    //Some browsers need &nbsp; in empty table cells.  We should detect this and
    //return an empty string.
    String html = "<table><tr><td>&nbsp;</td></tr></table>";
    scan(html);
    assertEquals("", ts.getTable(0).getCellContents(0, 0));
  }

  @Test
  public void tablesCanBeRepresentedAsStringLists() throws Exception {
    String html = "<table><tr><td>a</td><td>b</td></tr><tr><td>c</td><td>d</td></tr></table>";
    scan(html);
    assertEquals("[[a, b], [c, d]]", ts.getTable(0).toString());
  }

  @Test
  public void canExtractTablesFromHtml() throws Exception {
    String table1_fmt = "<body>GunkHeader gunk<table>gunk</table>gunk middle directions";
    String table2_fmt = "<table>gunk 2</table>gunk middle directions2";
    String table3_fmt = "<table>gunk 3</table>gunkend gunk</body>";


    String MULTI_TABLE_HTML = String.format(table1_fmt + table2_fmt + table3_fmt, "", "", "");
    scan(MULTI_TABLE_HTML);
    assertHasRegexp(table1_fmt + table2_fmt + table3_fmt, ts.toHtml(null, null));
    assertHasRegexp(table1_fmt, ts.toHtml(null, ts.getTable(1)));
    assertHasRegexp(table2_fmt, ts.toHtml(ts.getTable(1), ts.getTable(2)));
    assertHasRegexp(table3_fmt, ts.toHtml(ts.getTable(2), null));
  }
}
