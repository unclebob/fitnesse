// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.html.HtmlElement;
import fitnesse.wikitext.WikiWidget;

public class StandardTableWidgetTest extends WidgetTestCase {
  protected String getRegexp() {
    return StandardTableWidget.REGEXP;
  }

  public void testRegexp() throws Exception {
    assertMatch("|a|\n");
    assertMatch("|a|b|\n");
    assertMatch("|a|b|\n|c|\n");
    assertMatch("|a|\n|b|\n|c|\n");
    assertNoMatch("|abc\n|\n");
  }

  public void testRegexpForLiteralTable() throws Exception {
    assertMatch("!|a|\n");
    assertMatch("!|a|\n|b|\n");
    assertNoMatch(" !|a|\n");
  }

  public void testRegexpForCommentedTable() throws Exception {
    assertMatch("-|comment|\n");
    assertMatch("-|something|\n");
    assertMatch("-!|something|\n");
    assertNoMatch("- |a|\n");
    assertNoMatch(" -|a|\n");
    assertNoMatch("- !|a|\n");
    assertNoMatch("!-|a|\n");
  }
  
  public void testSimpleTable() throws Exception {
    StandardTableWidget table = new StandardTableWidget(new MockWidgetRoot(), "|a|\n");
    assertEquals(1, table.numberOfChildren());
    assertEquals(1, table.getColumns());
    WikiWidget child = table.nextChild();
    assertEquals(TableRowWidget.class, child.getClass());
    TableRowWidget row = (TableRowWidget) child;
    assertEquals(1, row.numberOfChildren());
    child = row.nextChild();
    assertEquals(TableCellWidget.class, child.getClass());
    TableCellWidget cell = (TableCellWidget) child;
    assertEquals(1, cell.numberOfChildren());
    child = cell.nextChild();
    assertEquals(TextWidget.class, child.getClass());
    assertEquals("a", ((TextWidget) child).getText());
  }

  public void testBiggerTable() throws Exception {
    StandardTableWidget table = new StandardTableWidget(new MockWidgetRoot(), "|a|b|c|\n|d|\n|e|f|\n");
    assertEquals(3, table.numberOfChildren());
    assertEquals(3, table.getColumns());
  }

  public void testHtml() throws Exception {
    StandardTableWidget table = new StandardTableWidget(new MockWidgetRoot(), "|a|\n");
    String expected = "<table border=\"1\" cellspacing=\"0\">\n<tr><td>a</td>" + HtmlElement.endl + "</tr>\n</table>\n";
    assertEquals(expected, table.render());
  }

  public void testBiggerHtml() throws Exception {
    StandardTableWidget table = new StandardTableWidget(new MockWidgetRoot(), "|a|\n|b|c|\n");
    String expected = "<table border=\"1\" cellspacing=\"0\">\n<tr><td colspan=\"2\">a</td>" +
      HtmlElement.endl + "</tr>\n<tr><td>b</td>" +
      HtmlElement.endl + "<td>c</td>" +
      HtmlElement.endl + "</tr>\n</table>\n";
    assertEquals(expected, table.render());
  }

  public void testTestTable() throws Exception {
    StandardTableWidget table = new StandardTableWidget(new MockWidgetRoot(), "!|'''bold text'''|\n");
    assertTrue(table.isLiteralTable);
    assertSubString("'''bold text'''", table.render());
  }

  public void testCanBuildWikiTextFromSimpleTable() throws Exception {
    StandardTableWidget table = new StandardTableWidget(new MockWidgetRoot(), "|a|\n");
    assertEquals("|a|\n", table.asWikiText());
  }

  public void testCanBuildWikiTextFromComplexTable() throws Exception {
    String complexTable = "|a|b|c|\n|d|e|f|\n";
    StandardTableWidget table = new StandardTableWidget(new MockWidgetRoot(), complexTable);
    assertEquals(complexTable, table.asWikiText());
  }

  public void testCanBuildWikiTextFromTestTable() throws Exception {
    String testTable = "!|a|b|\n|c|d|\n";
    StandardTableWidget table = new StandardTableWidget(new MockWidgetRoot(), testTable);
    assertEquals(testTable, table.asWikiText());
  }
  
  public void testCommentTableAsHtml() throws Exception {
    StandardTableWidget table = new StandardTableWidget(new MockWidgetRoot(), "-|a|\n|b|c|\n");
    String expected = "<table border=\"1\" cellspacing=\"0\">\n<tr class=\"hidden\"><td colspan=\"2\">a</td>" +
      HtmlElement.endl + "</tr>\n<tr><td>b</td>" +
      HtmlElement.endl + "<td>c</td>" +
      HtmlElement.endl + "</tr>\n</table>\n";
    assertEquals(expected, table.render());
  }

  public void testCanBuildWikiTextFromCommentTable() throws Exception {
    String testTable = "-|a|b|\n|c|d|\n";
    StandardTableWidget table = new StandardTableWidget(new MockWidgetRoot(), testTable);
    assertEquals(testTable, table.asWikiText());
  }

  public void testCanBuildWikiTextFromLiteralCommentTable() throws Exception {
    String testTable = "-!|a|b|\n|c|d|\n";
    StandardTableWidget table = new StandardTableWidget(new MockWidgetRoot(), testTable);
    assertEquals(testTable, table.asWikiText());
  }
}
