// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.html.HtmlElement;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;

public class TOCWidgetTest extends WidgetTest
{
	private WikiPage root;
	private WikiPage parent;
	private PageCrawler crawler;
	private String endl = HtmlElement.endl;

	public void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		crawler = root.getPageCrawler();
		parent = crawler.addPage(root, PathParser.parse("ParenT"), "parent");
		crawler.addPage(root, PathParser.parse("ParentTwo"), "parent two");
		crawler.addPage(parent, PathParser.parse("ChildOne"), "content");
		crawler.addPage(parent, PathParser.parse("ChildTwo"), "content");
	}

	public void tearDown() throws Exception
	{
	}

	public void testMatch() throws Exception
	{
		assertMatchEquals("!contents\n", "!contents");
		assertMatchEquals("!contents -R\n", "!contents -R");
		assertMatchEquals("!contents\r", "!contents");
		assertMatchEquals("!contents -R\r", "!contents -R");
		assertMatchEquals(" !contents\n", null);
		assertMatchEquals(" !contents -R\n", null);
		assertMatchEquals("!contents zap\n", null);
		assertMatchEquals("!contents \n", "!contents ");
	}

	public void testNoGrandchildren() throws Exception
	{
		assertEquals(getHtmlWithNoHierarchy(), renderNormalTOCWidget());
		assertEquals(getHtmlWithNoHierarchy(), renderHierarchicalTOCWidget());
	}

	public void testWithGrandchildren() throws Exception
	{
		addGrandChild();
		assertEquals(getHtmlWithNoHierarchy(), renderNormalTOCWidget());
		assertEquals(getHtmlWithGrandChild(), renderHierarchicalTOCWidget());
	}

	public void testWithGreatGrandchildren() throws Exception
	{
		addGrandChild();
		addGreatGrandChild();
		assertEquals(getHtmlWithNoHierarchy(), renderNormalTOCWidget());
		assertEquals(getHtmlWithGreatGrandChild(), renderHierarchicalTOCWidget());
	}

	private void addGrandChild()
		throws Exception
	{
		crawler.addPage(parent.getChildPage("ChildOne"), PathParser.parse("GrandChild"), "content");
	}

	private void addGreatGrandChild()
		throws Exception
	{
		crawler.addPage(parent.getChildPage("ChildOne").getChildPage("GrandChild"), PathParser.parse("GreatGrandChild"), "content");
	}

	public void testTocOnRoot() throws Exception
	{
		TOCWidget widget = new TOCWidget(new WidgetRoot(root), "!contents\n");
		String html = widget.render();
		assertHasRegexp("ParenT", html);
		assertHasRegexp("ParentTwo", html);
	}

	public void testDisplaysVirtualChildren() throws Exception
	{
		WikiPage page = crawler.addPage(root, PathParser.parse("VirtualParent"));
		PageData data = page.getData();
		data.setAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE, "http://localhost:" + FitNesseUtil.port + "/ParenT");
		page.commit(data);
		try
		{
			FitNesseUtil.startFitnesse(root);
			TOCWidget widget = new TOCWidget(new WidgetRoot(page), "!contents\n");
			String html = widget.render();
			assertEquals(virtualChildrenHtml(), html);
//			assertSubString("<li><a href=\"VirtualParent.ChildOne\"><i>ChildOne</i></a></li>\n", html);
//			assertSubString("<li><a href=\"VirtualParent.ChildTwo\"><i>ChildTwo</i></a></li>\n", html);
		}
		finally
		{
			FitNesseUtil.stopFitnesse();
		}
	}

	public void testIsNotHierarchical() throws Exception
	{
		assertFalse(new TOCWidget(new WidgetRoot(parent), "!contents\n").isRecursive());
	}

	public void testIsHierarchical() throws Exception
	{
		assertTrue(new TOCWidget(new WidgetRoot(parent), "!contents -R\n").isRecursive());
	}

	private String renderNormalTOCWidget()
		throws Exception
	{
		return new TOCWidget(new WidgetRoot(parent), "!contents\n").render();
	}

	private String renderHierarchicalTOCWidget()
		throws Exception
	{
		return new TOCWidget(new WidgetRoot(parent), "!contents -R\n").render();
	}

	private String getHtmlWithNoHierarchy()
	{
		return
			"<div class=\"toc1\">" + endl +
				"\t<ul>" + endl +
				"\t\t<li>" + endl +
				"\t\t\t<a href=\"ParenT.ChildOne\">ChildOne</a>" + endl +
				"\t\t</li>" + endl +
				"\t\t<li>" + endl +
				"\t\t\t<a href=\"ParenT.ChildTwo\">ChildTwo</a>" + endl +
				"\t\t</li>" + endl +
				"\t</ul>" + endl +
				"</div>" + endl;
	}

	private String getHtmlWithGrandChild()
	{
		return
			"<div class=\"toc1\">" + endl +
				"\t<ul>" + endl +
				"\t\t<li>" + endl +
				"\t\t\t<a href=\"ParenT.ChildOne\">ChildOne</a>" + endl +
				"\t\t\t<div class=\"toc2\">" + endl +
				"\t\t\t\t<ul>" + endl +
				"\t\t\t\t\t<li>" + endl +
				"\t\t\t\t\t\t<a href=\"ParenT.ChildOne.GrandChild\">GrandChild</a>" + endl +
				"\t\t\t\t\t</li>" + endl +
				"\t\t\t\t</ul>" + endl +
				"\t\t\t</div>" + endl +
				"\t\t</li>" + endl +
				"\t\t<li>" + endl +
				"\t\t\t<a href=\"ParenT.ChildTwo\">ChildTwo</a>" + endl +
				"\t\t</li>" + endl +
				"\t</ul>" + endl +
				"</div>" + endl;
	}

	private String getHtmlWithGreatGrandChild()
	{
		String expected =
			"<div class=\"toc1\">" + endl +
				"\t<ul>" + endl +
				"\t\t<li>" + endl +
				"\t\t\t<a href=\"ParenT.ChildOne\">ChildOne</a>" + endl +
				"\t\t\t<div class=\"toc2\">" + endl +
				"\t\t\t\t<ul>" + endl +
				"\t\t\t\t\t<li>" + endl +
				"\t\t\t\t\t\t<a href=\"ParenT.ChildOne.GrandChild\">GrandChild</a>" + endl +
				"\t\t\t\t\t\t<div class=\"toc3\">" + endl +
				"\t\t\t\t\t\t\t<ul>" + endl +
				"\t\t\t\t\t\t\t\t<li>" + endl +
				"\t\t\t\t\t\t\t\t\t<a href=\"ParenT.ChildOne.GrandChild.GreatGrandChild\">GreatGrandChild</a>" + endl +
				"\t\t\t\t\t\t\t\t</li>" + endl +
				"\t\t\t\t\t\t\t</ul>" + endl +
				"\t\t\t\t\t\t</div>" + endl +
				"\t\t\t\t\t</li>" + endl +
				"\t\t\t\t</ul>" + endl +
				"\t\t\t</div>" + endl +
				"\t\t</li>" + endl +
				"\t\t<li>" + endl +
				"\t\t\t<a href=\"ParenT.ChildTwo\">ChildTwo</a>" + endl +
				"\t\t</li>" + endl +
				"\t</ul>" + endl +
				"</div>" + endl;
		return expected;
	}

	private String virtualChildrenHtml()
	{
		return "<div class=\"toc1\">" + endl +
			"\t<ul>" + endl +
			"\t\t<li>" + endl +
			"\t\t\t<a href=\"VirtualParent.ChildOne\">" + endl +
			"\t\t\t\t<i>ChildOne</i>" + endl +
			"\t\t\t</a>" + endl +
			"\t\t</li>" + endl +
			"\t\t<li>" + endl +
			"\t\t\t<a href=\"VirtualParent.ChildTwo\">" + endl +
			"\t\t\t\t<i>ChildTwo</i>" + endl +
			"\t\t\t</a>" + endl +
			"\t\t</li>" + endl +
			"\t</ul>" + endl +
			"</div>" + endl;

	}

	protected String getRegexp()
	{
		return TOCWidget.REGEXP;
	}
}
