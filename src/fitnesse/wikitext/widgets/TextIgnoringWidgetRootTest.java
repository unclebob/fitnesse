// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import junit.framework.*;
import fitnesse.wiki.MockWikiPage;
import fitnesse.wikitext.WidgetBuilder;

import java.util.List;

public class TextIgnoringWidgetRootTest extends TestCase
{
	public void setUp() throws Exception
	{
	}

	public void tearDown() throws Exception
	{
	}

	public void testNoTextWidgetAreCreated() throws Exception
	{
		String text = "Here is some text with '''bold''' and ''italics''.";
		MockWikiPage page = new MockWikiPage("SomePage", text);
		WidgetRoot root = new TextIgnoringWidgetRoot(text, page, WidgetBuilder.htmlWidgetBuilder);
		List widgets = root.getChildren();
		assertEquals(2, widgets.size());
		assertTrue(widgets.get(0) instanceof BoldWidget);
		assertTrue(widgets.get(1) instanceof ItalicWidget);
	}

}
