// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.MockWikiPage;
import junit.framework.TestCase;

import java.util.regex.Pattern;

public class CommentWidgetTest extends TestCase
{
	private WidgetRoot root;

	public void setUp() throws Exception
	{
		MockWikiPage page = new MockWikiPage();
		root = new WidgetRoot(page);
	}

	public void tearDown() throws Exception
	{
	}

	public void testRegexp() throws Exception
	{
		assertTrue("match1", Pattern.matches(CommentWidget.REGEXP, "# Comment text\n"));
		assertTrue("match2", Pattern.matches(CommentWidget.REGEXP, "#\n"));
		assertTrue("match3", !Pattern.matches(CommentWidget.REGEXP, " #\n"));
	}

	public void testHtml() throws Exception
	{
		CommentWidget widget = new CommentWidget(root, "# some text\n");
		assertEquals("", widget.render());
	}

	public void testAsWikiText() throws Exception
	{
		CommentWidget widget = new CommentWidget(root, "# some text\n");
		assertEquals("# some text\n", widget.asWikiText());
	}
}