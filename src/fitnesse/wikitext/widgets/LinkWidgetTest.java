// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import junit.swingui.TestRunner;

public class LinkWidgetTest extends WidgetTest
{
	public static void main(String[] args)
	{
		TestRunner.main(new String[]{"fitnesse.wikitext.widgets.LinkWidgetTest"});
	}

	public void setUp() throws Exception
	{
	}

	public void tearDown() throws Exception
	{
	}

	public void testRegexp() throws Exception
	{
		assertMatchEquals("http://www.objectmentor.com/resources/bookstore/books/PPPCoverIcon.html", "http://www.objectmentor.com/resources/bookstore/books/PPPCoverIcon.html");
		assertMatchEquals("http://files/someFile", "http://files/someFile");
		assertMatchEquals("http://files", "http://files");
		assertMatchEquals("http://objectmentor.com", "http://objectmentor.com");
		assertMatchEquals("(http://objectmentor.com)", "http://objectmentor.com");
		assertMatchEquals("http://objectmentor.com.", "http://objectmentor.com");
		assertMatchEquals("(http://objectmentor.com).", "http://objectmentor.com");
		assertMatchEquals("https://objectmentor.com", "https://objectmentor.com");
	}

	public void testHtml() throws Exception
	{
		LinkWidget widget = new LinkWidget(new MockWidgetRoot(), "http://host.com/file.html");
		assertEquals("<a href=\"http://host.com/file.html\">http://host.com/file.html</a>", widget.render());

		widget = new LinkWidget(new MockWidgetRoot(), "http://files/somePage");
		assertEquals("<a href=\"/files/somePage\">http://files/somePage</a>", widget.render());

		widget = new LinkWidget(new MockWidgetRoot(), "http://www.objectmentor.com");
		assertEquals("<a href=\"http://www.objectmentor.com\">http://www.objectmentor.com</a>", widget.render());
	}

	public void testAsWikiText() throws Exception
	{
		final String LINK_TEXT = "http://xyz.com";
		LinkWidget widget = new LinkWidget(new MockWidgetRoot(), LINK_TEXT);
		assertEquals(LINK_TEXT, widget.asWikiText());
	}

	public void testHttpsLink() throws Exception
	{
		String link = "https://link.com";
		LinkWidget widget = new LinkWidget(new MockWidgetRoot(), link);
		assertEquals("<a href=\"https://link.com\">https://link.com</a>", widget.render());
		assertEquals(link, widget.asWikiText());
	}

	protected String getRegexp()
	{
		return LinkWidget.REGEXP;
	}
}
