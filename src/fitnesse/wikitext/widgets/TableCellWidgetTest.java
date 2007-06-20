// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.testutil.RegexTest;
import fitnesse.wikitext.WikiWidget;
import junit.swingui.TestRunner;

public class TableCellWidgetTest extends RegexTest
{
	public TableRowWidget row;
	private TableWidget table;

	public static void main(String[] args)
	{
		TestRunner.main(new String[]{"fitnesse.wikitext.widgets.TableCellWidgetTest"});
	}

	public void setUp() throws Exception
	{
		table = new TableWidget(new MockWidgetRoot(), "");
		row = new TableRowWidget(table, "", false);
	}

	public void testSimpleCell() throws Exception
	{
		TableCellWidget cell = new TableCellWidget(row, "a", false);
		assertEquals(1, cell.numberOfChildren());
		WikiWidget child = cell.nextChild();
		assertEquals(TextWidget.class, child.getClass());
		assertEquals("a", ((TextWidget) child).getText());
	}

	public void testTrimsWhiteSpace() throws Exception
	{
		TableCellWidget cell = new TableCellWidget(row, " 1 item ", false);
		assertEquals(1, cell.numberOfChildren());
		WikiWidget child = cell.nextChild();
		assertEquals(TextWidget.class, child.getClass());
		assertEquals("1 item", ((TextWidget) child).getText());
	}

	public void testLiteralCell() throws Exception
	{
		TableCellWidget cell = new TableCellWidget(row, "''italic'' '''bold''", true);
		assertEquals(1, cell.numberOfChildren());
		assertSubString("''italic'' '''bold''", cell.render());
	}

	public void testLiteralInLiteralCell() throws Exception
	{
		WidgetRoot root = new MockWidgetRoot();
		root.defineLiteral("blah");
		table = new TableWidget(root, "");
		row = new TableRowWidget(table, "", true);
		TableCellWidget cell = new TableCellWidget(row, "''!lit(0)''", true);
		assertSubString("''blah''", cell.render());
	}

	public void testVariableInLiteralCell() throws Exception
	{
		WidgetRoot root = new MockWidgetRoot();
		root.addVariable("X", "abc");
		table = new TableWidget(root, "");
		row = new TableRowWidget(table, "", true);
		TableCellWidget cell = new TableCellWidget(row, "''${X}''", true);
		assertSubString("''abc''", cell.render());
	}
}