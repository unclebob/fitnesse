// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.*;
import fitnesse.wikitext.WidgetBuilder;

public class VariableWidgetTest extends WidgetTest
{
	private WikiPage root;
	private PageCrawler crawler;
	private WikiPage page;
	private WidgetRoot widgetRoot;

	public void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("root");
		crawler = root.getPageCrawler();
		page = crawler.addPage(root, PathParser.parse("MyPage"));
		widgetRoot = new WidgetRoot("", page);
	}

	public void tearDown() throws Exception
	{
	}

	public void testMatches() throws Exception
	{
		assertMatches("${X}");
		assertMatches("${xyz}");
	}

	protected String getRegexp()
	{
		return VariableWidget.REGEXP;
	}

	public void testVariableIsExpressed() throws Exception
	{
		widgetRoot.addVariable("x", "1");
		VariableWidget w = new VariableWidget(widgetRoot, "${x}");
		assertEquals("1", w.render());
	}

	public void testRenderTwice() throws Exception
	{
		widgetRoot.addVariable("x", "1");
		VariableWidget w = new VariableWidget(widgetRoot, "${x}");
		assertEquals("1", w.render());
		assertEquals("1", w.render());
	}

	public void testVariableInParentPage() throws Exception
	{
		WikiPage parent = crawler.addPage(root, PathParser.parse("ParentPage"), "!define var {zot}\n");
		WikiPage child = crawler.addPage(parent, PathParser.parse("ChildPage"), "ick");

		WidgetRoot widgetRoot = new WidgetRoot("", child, WidgetBuilder.htmlWidgetBuilder);
		VariableWidget w = new VariableWidget(widgetRoot, "${var}");
		assertEquals("zot", w.render());
	}

	public void testUndefinedVariable() throws Exception
	{
		WikiPage page = crawler.addPage(root, PathParser.parse("MyPage"));
		WidgetRoot widgetRoot = new WidgetRoot("", page);
		VariableWidget w = new VariableWidget(widgetRoot, "${x}");
		assertSubString("undefined variable: x", w.render());
	}

	public void testAsWikiText() throws Exception
	{
		VariableWidget w = new VariableWidget(widgetRoot, "${x}");
		assertEquals("${x}", w.asWikiText());
	}
}
