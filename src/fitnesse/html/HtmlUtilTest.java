// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.html;

import fitnesse.testutil.RegexTest;
import fitnesse.wiki.*;

public class HtmlUtilTest extends RegexTest
{
	private static final String endl = HtmlElement.endl;

	public void testBreadCrumbsWithCurrentPageLinked() throws Exception
	{
		String trail = "1.2.3.4";
		HtmlTag breadcrumbs = HtmlUtil.makeBreadCrumbsWithCurrentPageLinked(trail);
		String expected = getBreadCrumbsWithLastOneLinked();
		assertEquals(expected, breadcrumbs.html());
	}

	public void testBreadCrumbsWithCurrentPageNotLinked() throws Exception
	{
		String trail = "1.2.3.4";
		HtmlTag breadcrumbs = HtmlUtil.makeBreadCrumbsWithCurrentPageNotLinked(trail);
		String expected = getBreadCrumbsWithLastOneNotLinked();
		assertEquals(expected, breadcrumbs.html());
	}

	public void testBreadCrumbsWithPageType() throws Exception
	{
		String trail = "1.2.3.4";
		HtmlTag breadcrumbs = HtmlUtil.makeBreadCrumbsWithPageType(trail, "Some Type");
		String expected = getBreadCrumbsWithLastOneLinked() +
		  "<br/><span class=\"page_type\">Some Type</span>" + endl;
		assertEquals(expected, breadcrumbs.html());
	}

	private String getBreadCrumbsWithLastOneLinked()
	{
		return getFirstThreeBreadCrumbs() +
		  "<br/><a href=\"/1.2.3.4\" class=\"page_title\">4</a>" + endl;
	}

	private String getBreadCrumbsWithLastOneNotLinked()
	{
		return getFirstThreeBreadCrumbs() +
		  "<br/><span class=\"page_title\">4</span>" + endl;
	}

	private String getFirstThreeBreadCrumbs()
	{
		return "<a href=\"/1\">1</a>." + endl +
		  "<a href=\"/1.2\">2</a>." + endl +
		  "<a href=\"/1.2.3\">3</a>." + endl;
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

	public void testMakeBreadCrumbsWithCurrentPageLinkedWithEmptyArray() throws Exception
	{
		try
		{
			HtmlUtil.makeBreadCrumbsWithCurrentPageLinked(".");
			HtmlUtil.makeBreadCrumbsWithCurrentPageLinked("");
		}
		catch(Exception e)
		{
			fail("should not throw exception");
		}
	}

	public void testMakeActionsDefault() throws Exception
	{
		WikiPage root = InMemoryPage.makeRoot("root");
		root.addChildPage("SomePage");
		PageData pageData = new PageData(root.getChildPage("SomePage"));
		String expected = "<!--Edit button-->" + endl +
		  "<a href=\"SomePage?edit\" accesskey=\"e\">Edit</a>" + endl +
		  "<!--Versions button-->" + endl +
		  "<a href=\"SomePage?versions\" accesskey=\"v\">Versions</a>" + endl +
		  "<!--Properties button-->" + endl +
		  "<a href=\"SomePage?properties\" accesskey=\"p\">Properties</a>" + endl +
		  "<!--Refactor button-->" + endl +
		  "<a href=\"SomePage?refactor\" accesskey=\"r\">Refactor</a>" + endl +
		  "<!--Where Used button-->" + endl +
		  "<a href=\"SomePage?whereUsed\" accesskey=\"w\">Where Used</a>" + endl +
		  "<div class=\"nav_break\">&nbsp;</div>" + endl +
		  "<!--Files button-->" + endl +
		  "<a href=\"/files\" accesskey=\"f\">Files</a>" + endl +
		  "<!--Search button-->" + endl +
		  "<a href=\"?searchForm\" accesskey=\"s\">Search</a>" + endl;
		assertEquals(expected, HtmlUtil.makeActions(pageData, "SomePage", "SomePage", false).html());
	}

	public void testMakeActionsWithTestButton() throws Exception
	{
		WikiPage root = InMemoryPage.makeRoot("root");
		root.addChildPage("TestSomething");
		PageData pageData = new PageData(root.getChildPage("TestSomething"));
		String expected = "<!--Test button-->" + endl +
		  "<a href=\"TestSomething?test\" accesskey=\"t\">Test</a>" + endl +
		  "<div class=\"nav_break\">&nbsp;</div>" + endl +
		  "<!--Edit button-->" + endl +
		  "<a href=\"TestSomething?edit\" accesskey=\"e\">Edit</a>" + endl +
		  "<!--Versions button-->" + endl +
		  "<a href=\"TestSomething?versions\" accesskey=\"v\">Versions</a>" + endl +
		  "<!--Properties button-->" + endl +
		  "<a href=\"TestSomething?properties\" accesskey=\"p\">Properties</a>" + endl +
		  "<!--Refactor button-->" + endl +
		  "<a href=\"TestSomething?refactor\" accesskey=\"r\">Refactor</a>" + endl +
		  "<!--Where Used button-->" + endl +
		  "<a href=\"TestSomething?whereUsed\" accesskey=\"w\">Where Used</a>" + endl +
		  "<div class=\"nav_break\">&nbsp;</div>" + endl +
		  "<!--Files button-->" + endl +
		  "<a href=\"/files\" accesskey=\"f\">Files</a>" + endl +
		  "<!--Search button-->" + endl +
		  "<a href=\"?searchForm\" accesskey=\"s\">Search</a>" + endl;
		assertEquals(expected, HtmlUtil.makeActions(pageData, "TestSomething", "TestSomething", false).html());
	}

	public void testMakeActionsWithSuiteButton() throws Exception
	{
		WikiPage root = InMemoryPage.makeRoot("root");
		root.addChildPage("SuiteNothings");
		PageData pageData = new PageData(root.getChildPage("SuiteNothings"));
		String expected = "<!--Suite button-->" + endl +
		  "<a href=\"SuiteNothings?suite\" accesskey=\"\">Suite</a>" + endl +
		  "<div class=\"nav_break\">&nbsp;</div>" + endl +
		  "<!--Edit button-->" + endl +
		  "<a href=\"SuiteNothings?edit\" accesskey=\"e\">Edit</a>" + endl +
		  "<!--Versions button-->" + endl +
		  "<a href=\"SuiteNothings?versions\" accesskey=\"v\">Versions</a>" + endl +
		  "<!--Properties button-->" + endl +
		  "<a href=\"SuiteNothings?properties\" accesskey=\"p\">Properties</a>" + endl +
		  "<!--Refactor button-->" + endl +
		  "<a href=\"SuiteNothings?refactor\" accesskey=\"r\">Refactor</a>" + endl +
		  "<!--Where Used button-->" + endl +
		  "<a href=\"SuiteNothings?whereUsed\" accesskey=\"w\">Where Used</a>" + endl +
		  "<div class=\"nav_break\">&nbsp;</div>" + endl +
		  "<!--Files button-->" + endl +
		  "<a href=\"/files\" accesskey=\"f\">Files</a>" + endl +
		  "<!--Search button-->" + endl +
		  "<a href=\"?searchForm\" accesskey=\"s\">Search</a>" + endl;
		assertEquals(expected, HtmlUtil.makeActions(pageData, "SuiteNothings", "SuiteNothings", false).html());
	}
}
