// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wikitext.WikiWidget;
import junit.framework.TestCase;
import junit.swingui.TestRunner;

public class TableRowWidgetTest extends TestCase
{
	public static void main(String[] args)
	{
		TestRunner.main(new String[]{"fitnesse.wikitext.widgets.TableRowWidgetTest"});
	}

	public void setUp() throws Exception
	{
	}

	public void tearDown() throws Exception
	{
	}

	public void testSimpleRow() throws Exception
	{
		TableWidget table = new TableWidget(new MockWidgetRoot(), "");
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
}