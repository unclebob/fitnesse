// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.html;

import fitnesse.testutil.RegexTest;
import fitnesse.wiki.*;

public class HtmlUtilTest extends RegexTest
{
	private static final String endl = HtmlElement.endl;

	public void testBreadCrumbsTitle() throws Exception
	{
		String trail = "1.2.3.4";
		HtmlTag breadcrumbs = HtmlUtil.makeBreadCrumbs(trail);
		String expected = "<a href=\"/1\">1</a>." + endl +
		  "<a href=\"/1.2\">2</a>." + endl +
		  "<a href=\"/1.2.3\">3</a>." + endl +
		  "<br/><a href=\"/1.2.3.4\" class=\"page_title\">4</a>";

		String html = breadcrumbs.html();
		assertSubString(expected, html);
		assertSubString("href=\"/1.2.3.4\"", html);
		assertSubString("class=\"page_title\"", html);
		assertTrue(html.endsWith(">4</a>" + endl));
	}

	public void testMakeFormTag() throws Exception
	{
		HtmlTag formTag = HtmlUtil.makeFormTag("method", "action");
		assertSubString("method", formTag.getAttribute("method"));
		assertSubString("action", formTag.getAttribute("action"));
	}

	public void testTestableHtml() throws Exception
	{
		WikiPage root = InMemoryPage.makeRoot("RooT");
		PageCrawler crawler = root.getPageCrawler();
		crawler.addPage(root, PathParser.parse("SetUp"), "setup");
		crawler.addPage(root, PathParser.parse("TearDown"), "teardown");
		WikiPage page = crawler.addPage(root, PathParser.parse("TestPage"), "the content");

		String html = HtmlUtil.testableHtml(page.getData());
		assertSubString(".SetUp", html);
		assertSubString("setup", html);
		assertSubString(".TearDown", html);
		assertSubString("teardown", html);
		assertSubString("the content", html);
		assertSubString("class=\"collapsable\"", html);
	}

	public void testMakeDivTag() throws Exception
	{
		String expected = "<div class=\"myClass\"></div>" + HtmlElement.endl;
		assertEquals(expected, HtmlUtil.makeDivTag("myClass").html());
	}
}
