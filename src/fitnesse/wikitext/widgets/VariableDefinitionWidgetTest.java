// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.*;
import fitnesse.wikitext.WikiWidget;

public class VariableDefinitionWidgetTest extends WidgetTest
{
	public WikiPage root;
	private PageCrawler crawler;
	private WidgetRoot widgetRoot;

	protected String getRegexp()
	{
		return VariableDefinitionWidget.REGEXP;
	}

	public void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		crawler = root.getPageCrawler();
	}

	public void tearDown() throws Exception
	{
	}

	public void testRegexp() throws Exception
	{
		assertMatches("!define xyz {\n123\r\n456\r\n}");
		assertMatches("!define abc {1}");
		assertMatches("!define abc (1)");
		assertNoMatch("!define");
		assertNoMatch("!define x");
		assertNoMatch(" !define x {1}");
		assertMatches("!define x (!define y {123})");
	}

	public void testHtml() throws Exception
	{
		WikiPage page = crawler.addPage(root, PathParser.parse("MyPage"), "content");
		WikiPage page2 = crawler.addPage(root, PathParser.parse("SecondPage"), "content");

		widgetRoot = new WidgetRoot(page);
		VariableDefinitionWidget widget = new VariableDefinitionWidget(widgetRoot, "!define x {1}\n");
		assertEquals("<span class=\"meta\">variable defined: x=1</span>", widget.render());
		assertEquals("1", widgetRoot.getVariable("x"));

		widgetRoot = new WidgetRoot(page2);
		widget = new VariableDefinitionWidget(widgetRoot, "!define xyzzy (\nbird\n)\n");
		widget.render();
		assertEquals("\nbird\n", widgetRoot.getVariable("xyzzy"));
	}

	public void testRenderedText() throws Exception
	{
		WikiWidget widget = new VariableDefinitionWidget(new WidgetRoot(root), "!define x (1)\n");
		String renderedText = widget.render();
		assertSubString("x", renderedText);
		assertSubString("1", renderedText);
	}

	public void testDefinePrecedingClasspath() throws Exception
	{
		WikiPage root = InMemoryPage.makeRoot("RooT");
		PageData data = root.getData();
		String content = "!define SOME_VARIABLE {Variable #1}\n!path c:\\dotnet\\*.dll";
		data.setContent(content);
		root.commit(data);
		assertEquals("Variable #1", data.getVariable("SOME_VARIABLE"));
		assertEquals(1, data.getClasspaths().size());
		assertEquals("c:\\dotnet\\*.dll", data.getClasspaths().get(0));
	}

	public void testNoExtraLineBreakInHtml() throws Exception
	{
		WikiPage root = InMemoryPage.makeRoot("RooT");
		PageData data = root.getData();
		String content = "!define SOME_VARIABLE {Variable #1}\n!define ANOTHER_VARIABLE {Variable #2}";
		data.setContent(content);
		assertSubString("SOME_VARIABLE=Variable #1</span><br><span", data.getHtml());
		assertNotSubString("SOME_VARIABLE=Variable #1</span><br><br><span", data.getHtml());
	}

	public void testAsWikiText() throws Exception
	{
		VariableDefinitionWidget widget = new VariableDefinitionWidget(new MockWidgetRoot(), "!define x {1}\n");
		assertEquals("!define x {1}", widget.asWikiText());
		widget = new VariableDefinitionWidget(new MockWidgetRoot(), "!define x ({1})\n");
		assertEquals("!define x ({1})", widget.asWikiText());
	}
}
