// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import junit.framework.TestCase;
import junit.swingui.TestRunner;

public class ListItemWidgetTest extends TestCase
{
	public static void main(String[] args)
	{
		TestRunner.main(new String[]{"ListItemWidgetTest"});
	}

	public void setUp() throws Exception
	{
	}

	public void tearDown() throws Exception
	{
	}

	public void testHtml() throws Exception
	{
		ListItemWidget widget = new ListItemWidget(new MockWidgetRoot(), "some text", 0);
		assertEquals("<li>some text</li>\n", widget.render());
		widget = new ListItemWidget(new MockWidgetRoot(), "some text", 3);
		assertEquals("\t\t\t<li>some text</li>\n", widget.render());
	}
}