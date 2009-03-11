// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import util.RegexTestCase;
import fitnesse.wikitext.WikiWidget;

public class TableCellWidgetTest extends RegexTestCase {
  public TableRowWidget row;
  private TableWidget table;

  public void setUp() throws Exception {
    table = new TableWidget(new MockWidgetRoot(), "");
    row = new TableRowWidget(table, "", false);
  }

  public void testSimpleCell() throws Exception {
    TableCellWidget cell = new TableCellWidget(row, "a", false);
    assertEquals(1, cell.numberOfChildren());
    WikiWidget child = cell.nextChild();
    assertEquals(TextWidget.class, child.getClass());
    assertEquals("a", ((TextWidget) child).getText());
  }

  public void testTrimsWhiteSpace() throws Exception {
    TableCellWidget cell = new TableCellWidget(row, " 1 item ", false);
    assertEquals(1, cell.numberOfChildren());
    WikiWidget child = cell.nextChild();
    assertEquals(TextWidget.class, child.getClass());
    assertEquals("1 item", ((TextWidget) child).getText());
  }

  public void testLiteralCell() throws Exception {
    TableCellWidget cell = new TableCellWidget(row, "''italic'' '''bold''", true);
    assertEquals(1, cell.numberOfChildren());
    assertSubString("''italic'' '''bold''", cell.render());
  }

  public void testLiteralInLiteralCell() throws Exception {
    ParentWidget root = new MockWidgetRoot();
    root.defineLiteral("blah");
    table = new TableWidget(root, "");
    row = new TableRowWidget(table, "", true);
    //Paren Literal: () -> ??
    TableCellWidget cell = new TableCellWidget(row, "''!lit?0?''", true);
    assertSubString("''blah''", cell.render());
  }

  public void testVariableInLiteralCell() throws Exception {
    ParentWidget root = new MockWidgetRoot();
    root.addVariable("X", "abc");
    table = new TableWidget(root, "");
    row = new TableRowWidget(table, "", true);
    TableCellWidget cell = new TableCellWidget(row, "''${X}''", true);
    assertSubString("''abc''", cell.render());
  }
}
