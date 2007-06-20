// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.html.HtmlElement;
import fitnesse.wikitext.WikiWidget;
import junit.swingui.TestRunner;

public class TableWidgetTest extends WidgetTest
{
	public static void main(String[] args)
	{
		TestRunner.main(new String[]{"fitnesse.wikitext.widgets.TableWidgetTest"});
	}

	protected String getRegexp()
	{
		return TableWidget.REGEXP;
	}

	public void setUp() throws Exception
	{
	}

	public void tearDown() throws Exception
	{
	}

	public void testRegexp() throws Exception
	{
		assertMatches("|a|\n");
		assertMatches("|a|b|\n");
		assertMatches("|a|b|\n|c|\n");
		assertMatches("|a|\n|b|\n|c|\n");
		assertNoMatch("|abc\n|\n");
		assertMatches("|a|\r");
		assertMatches("|a|b|\r");
		assertMatches("|a|b|\r|c|\r");
		assertMatches("|a|\r|b|\r|c|\r");
		assertNoMatch("|abc\r|\r");
		assertMatches("|a|\r\n");
		assertMatches("|a|b|\r\n");
		assertMatches("|a|b|\r\n|c|\r\n");
		assertMatches("|a|\r\n|b|\r\n|c|\r\n");
		assertNoMatch("|abc\r\n|\r\n");
	}

	public void testRegexpForLiteralTable() throws Exception
	{
		assertMatches("!|a|\n");
		assertMatches("!|a|\n|b|\n");
		assertNoMatch(" !|a|\n");
	}

	public void testSimpleTable() throws Exception
	{
		TableWidget table = new TableWidget(new MockWidgetRoot(), "|a|\n");
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

	public void testBiggerTable() throws Exception
	{
		TableWidget table = new TableWidget(new MockWidgetRoot(), "|a|b|c|\n|d|\n|e|f|\n");
		assertEquals(3, table.numberOfChildren());
		assertEquals(3, table.getColumns());
	}

	public void testHtml() throws Exception
	{
		TableWidget table = new TableWidget(new MockWidgetRoot(), "|a|\n");
		String expected = "<table border=\"1\" cellspacing=\"0\">\n<tr><td>a</td>" + HtmlElement.endl + "</tr>\n</table>\n";
		assertEquals(expected, table.render());
	}

	public void testBiggerHtml() throws Exception
	{
		TableWidget table = new TableWidget(new MockWidgetRoot(), "|a|\n|b|c|\n");
		String expected = "<table border=\"1\" cellspacing=\"0\">\n<tr><td colspan=\"2\">a</td>" +
			HtmlElement.endl + "</tr>\n<tr><td>b</td>" +
			HtmlElement.endl + "<td>c</td>" +
			HtmlElement.endl + "</tr>\n</table>\n";
		assertEquals(expected, table.render());
	}

	public void testTestTable() throws Exception
	{
		TableWidget table = new TableWidget(new MockWidgetRoot(), "!|'''bold text'''|\n");
		assertTrue(table.isTestTable);
		assertSubString("'''bold text'''", table.render());
	}
}