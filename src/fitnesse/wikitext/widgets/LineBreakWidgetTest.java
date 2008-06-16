// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

public class LineBreakWidgetTest extends WidgetTestCase
{

	public void testRegexp() throws Exception
	{
		assertMatch("\n");
		assertMatch("\r");
		assertMatch("\r\n");
	}

	public void testHtml() throws Exception
	{
		LineBreakWidget widget = new LineBreakWidget(new MockWidgetRoot(), "\n");
		assertEquals("<br/>", widget.render());
	}

	protected String getRegexp()
	{
		return LineBreakWidget.REGEXP;
	}
}