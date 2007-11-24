// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.*;
import fitnesse.wikitext.WidgetBuilder;

public class VariableWidgetTest extends WidgetTestCase
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
      assertMatches("${x.y.z}");
      assertMatches("${.y.z}");
      assertMatches("${x.y.}");
      assertMatches("${.xy.}");
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

   public void testVariableIsExpressedWithPeriods() throws Exception
   {
      widgetRoot.addVariable("x.y.z", "2");
      VariableWidget w = new VariableWidget(widgetRoot, "${x.y.z}");
      assertEquals("2", w.render());
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
   
   public void testAsWikiTextWithPeriods() throws Exception
   {
      VariableWidget w = new VariableWidget(widgetRoot, "${x.y.z}");
      assertEquals("${x.y.z}", w.asWikiText());
   }
   
   public void testLiteralsInheritedFromParent() throws Exception
   {
      WikiPage parent = crawler.addPage
                        ( root,
                          PathParser.parse("ParentPage"),
                          "!define var {!-some literal-!}\n" +
                          "!define paren (!-paren literal-!)\n"
                        );
      WikiPage child = crawler.addPage(parent, PathParser.parse("ChildPage"), "ick");
      WidgetRoot widgetRoot = new WidgetRoot("", child, WidgetBuilder.htmlWidgetBuilder);

      VariableWidget w = new VariableWidget(widgetRoot, "${var}");
      assertEquals("some literal", w.render());
      w = new VariableWidget(widgetRoot, "${paren}");
      assertEquals("paren literal", w.render());
   }
}
