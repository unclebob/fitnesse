// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.*;

public class XRefWidgetTest extends WidgetTest
{
	private WikiPage root;
	private WikiPage page;
	private WidgetRoot wroot;

	public void testRegexp() throws Exception
	{
		assertMatchEquals("!see SomePage", "!see SomePage");
		assertMatchEquals("!see SomePage.SubPage", "!see SomePage.SubPage");
		assertMatchEquals("!see SomePage.SubPage junk", "!see SomePage.SubPage");
	}

	protected void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		page = root.getPageCrawler().addPage(root, PathParser.parse("SomePage"));
		wroot = new WidgetRoot(page);
	}

	public void testHtml() throws Exception
	{
		XRefWidget widget = new XRefWidget(wroot, "!see SomePage");
		assertHasRegexp("<b>See: <a href=.*SomePage</a></b>", widget.render());

		widget = new XRefWidget(wroot, "!see NoPage");
		assertHasRegexp("<b>See: NoPage<a href=.*>?</a></b>", widget.render());
	}

	public void testAsWikiText() throws Exception
	{
		final String TEST_WIDGET = "!see SomePage";
		XRefWidget w = new XRefWidget(wroot, TEST_WIDGET);
		assertEquals(TEST_WIDGET, w.asWikiText());
	}

	protected String getRegexp()
	{
		return XRefWidget.REGEXP;
	}
}
