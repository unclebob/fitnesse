// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import junit.framework.TestCase;

public class TextWidgetTest extends TestCase
{
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