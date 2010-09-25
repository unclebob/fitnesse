// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.wikitext.WikiWidget;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TableRowWidgetTest {
  @Test
  public void testSimpleRow() throws Exception {
    StandardTableWidget table = new StandardTableWidget(new MockWidgetRoot(), "");
    TableRowWidget row = new TableRowWidget(table, "|a", false);
    assertEquals(1, row.numberOfChildren());
    WikiWidget child = row.nextChild();
    assertEquals(TableCellWidget.class, child.getClass());
    TableCellWidget cell = (TableCellWidget) child;
    assertEquals(1, cell.numberOfChildren());
    child = cell.nextChild();
    assertEquals(TextWidget.class, child.getClass());
    assertEquals("a", ((TextWidget) child).getText());
  }

  @Test
  public void testPlainTextRow() throws Exception {
    PlainTextTableWidget table = new PlainTextTableWidget(new MockWidgetRoot(), "");
    table.maximizeColumns(3);
    TableRowWidget row = new TableRowWidget(table, new String[] {"a", "b", "c"});
    assertEquals("<tr><td>a</td><td>b</td><td>c</td></tr>", row.render().replaceAll("\n", "").replaceAll("\r", ""));
  }
}
