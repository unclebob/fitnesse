// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wikitext.WikiWidget;
import junit.framework.TestCase;

import java.util.regex.Pattern;

public class BoldWidgetTest extends TestCase
{
	public void setUp() throws Exception
	{
	}

	public void tearDown() throws Exception
	{
	}

	public void testRegexp() throws Exception
	{
		assertTrue("match1", Pattern.matches(BoldWidget.REGEXP, "'''bold'''"));
		assertTrue("match2", Pattern.matches(BoldWidget.REGEXP, "''''bold''''"));
		assertTrue("match3", !Pattern.matches(BoldWidget.REGEXP, "'' 'not bold' ''"));
	}

	public void testBadConstruction() throws Exception
	{
		BoldWidget widget = new BoldWidget(new MockWidgetRoot(), "''''some text' '''");
		assertEquals(1, widget.numberOfChildren());
		WikiWidget child = widget.nextChild();
		assertEquals(TextWidget.class, child.getClass());
		assertEquals("'some text' ", ((TextWidget) child).getText());
	}

	public void testHtml() throws Exception
	{
		BoldWidget widget = new BoldWidget(new MockWidgetRoot(), "'''bold text'''");
		assertEquals("<b>bold text</b>", widget.render());
	}

}