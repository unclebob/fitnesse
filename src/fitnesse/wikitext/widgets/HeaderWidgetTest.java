// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import junit.framework.TestCase;
import junit.swingui.TestRunner;

import java.util.regex.Pattern;

public class HeaderWidgetTest extends TestCase
{
	public static void main(String[] args)
	{
		TestRunner.main(new String[]{"fitnesse.wikitext.widgets.HeaderWidgetTest"});
	}

	public void setUp() throws Exception
	{
	}

	public void tearDown() throws Exception
	{
	}

	public void testRegexp() throws Exception
	{
		assertTrue("match1", Pattern.matches(HeaderWidget.REGEXP, "!1 some text\n"));
		assertTrue("match2", Pattern.matches(HeaderWidget.REGEXP, "!2 \n"));
		assertTrue("match3", !Pattern.matches(HeaderWidget.REGEXP, "!3text\n"));
		assertTrue("match4", !Pattern.matches(HeaderWidget.REGEXP, "!4 text\n"));
		assertTrue("match5", Pattern.matches(HeaderWidget.REGEXP, "!3 text\n"));
	}

	public void testGetSize() throws Exception
	{
		HeaderWidget widget = new HeaderWidget(new MockWidgetRoot(), "!1 text \n");
		assertEquals(1, widget.size());
		widget = new HeaderWidget(new MockWidgetRoot(), "!3 text \n");
		assertEquals(3, widget.size());
	}

	public void testHtml() throws Exception
	{
		HeaderWidget widget = new HeaderWidget(new MockWidgetRoot(), "!1 some text\n");
		assertEquals("<h1>some text</h1>", widget.render());
	}
}