// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import junit.framework.TestCase;
import junit.swingui.TestRunner;

public class TextWidgetTest extends TestCase
{
	public static void main(String[] args)
	{
		TestRunner.main(new String[]{"fitnesse.wikitext.widgets.TextWidgetTest"});
	}

	public void setUp() throws Exception
	{
	}

	public void tearDown() throws Exception
	{
	}

	public void testGetText() throws Exception
	{
		TextWidget widget = new TextWidget(new MockWidgetRoot(), "some text");
		assertEquals("some text", widget.getText());
	}

	public void testHtml() throws Exception
	{
		TextWidget widget = new TextWidget(new MockWidgetRoot(), "some text");
		assertEquals("some text", widget.render());
	}

	public void testAsWikiText() throws Exception
	{
		TextWidget widget = new TextWidget(new MockWidgetRoot(), "some text");
		assertEquals("some text", widget.asWikiText());
	}

}