// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;
import fitnesse.wikitext.WidgetBuilder;

public class IncludeWidgetTest extends WidgetTest
{

	protected WikiPage root;
	protected WikiPage page1;
	protected PageCrawler crawler;

	public void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		crawler = root.getPageCrawler();
		crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
		page1 = crawler.addPage(root, PathParser.parse("PageOne"), "page one");
		crawler.addPage(root, PathParser.parse("PageTwo"), "page '''two'''");
		crawler.addPage(root, PathParser.parse("PageTwo.ChildOne"), "child page");
	}

	public void tearDown() throws Exception
	{
	}

	private IncludeWidget createIncludeWidget(WikiPage wikiPage, String includedPageName) throws Exception
	{
		return createIncludeWidget(new WidgetRoot(wikiPage), includedPageName);
	}

	private IncludeWidget createIncludeWidget(WidgetRoot widgetRoot, String includedPageName) throws Exception
	{
		return new IncludeWidget(widgetRoot, "!include " + includedPageName);
	}

	protected String getRegexp()
	{
		return IncludeWidget.REGEXP;
	}

	public void testIsCollapsable() throws Exception
	{
		IncludeWidget widget = createIncludeWidget(page1, "PageOne");
		final String result = widget.render();
		assertSubString("class=\"collapsable\"", result);
	}

	public void testSeamlessIsNotCollapsable() throws Exception
	{
		IncludeWidget widget = createIncludeWidget(page1, "-seamless PageOne");
		final String result = widget.render();
		assertNotSubString("class=\"collapsable\"", result);
	}

	public void testRegexp() throws Exception
	{
		assertMatchEquals("!include SomePage", "!include SomePage");
		assertMatchEquals("!include SomePage\n", "!include SomePage\n");
		assertMatchEquals("abc\n" + "!include SomePage\nxyz", "!include SomePage\n");
		assertMatchEquals("!include .SomePage.ChildPage", "!include .SomePage.ChildPage");
		assertNoMatch("!include nonWikiWord");
		assertNoMatch(" " + "!include WikiWord");
	}

	public void testRegexpWithOptions() throws Exception
	{
		assertMatchEquals("!include -setup SomePage", "!include -setup SomePage");
		assertMatchEquals("!include  -setup SomePage", "!include  -setup SomePage");
		assertMatchEquals("!include -teardown SomePage", "!include -teardown SomePage");
		assertMatchEquals("!include  -teardown SomePage", "!include  -teardown SomePage");
		assertMatchEquals("!include -seamless SomePage", "!include -seamless SomePage");
		assertMatchEquals("!include  -seamless SomePage", "!include  -seamless SomePage");
	}

	public void testSetUpParts() throws Exception
	{
		IncludeWidget widget = new IncludeWidget(new WidgetRoot(root), "!include -setup SomePage");
		assertSubString("class=\"setup\"", widget.render());
		assertSubString("Set Up: ", widget.render());
	}

	public void testTearDownParts() throws Exception
	{
		IncludeWidget widget = new IncludeWidget(new WidgetRoot(root), "!include -teardown SomePage");
		assertSubString("class=\"teardown\"", widget.render());
		assertSubString("Tear Down: ", widget.render());
	}

	public void testLiteralsGetRendered() throws Exception
	{
		verifyLiteralsGetRendered("", "LiteralPage");
	}

	public void testLiteralsGetRenderedSeamless() throws Exception
	{
		verifyLiteralsGetRendered("-seamless ", "LiteralPage");
	}

	private void verifyLiteralsGetRendered(String option, String pageName)
	  throws Exception
	{
		crawler.addPage(root, PathParser.parse(pageName), "!-one-!, !-two-!, !-three-!");
		WidgetRoot widgetRoot = new WidgetRoot(page1);
		IncludeWidget widget = createIncludeWidget(widgetRoot, option + pageName);
		final String result = widget.render();
		assertSubString("one, two, three", result);
		assertEquals("one", widgetRoot.getLiteral(0));
		assertEquals("two", widgetRoot.getLiteral(1));
		assertEquals("three", widgetRoot.getLiteral(2));
	}

	public void testRenderWhenMissing() throws Exception
	{
		verifyRenderWhenMissing("MissingPage");
	}

	public void testRenderWhenMissingSeamless() throws Exception
	{
		verifyRenderWhenMissing("-seamless MissingPage");
	}

	private void verifyRenderWhenMissing(String optionAndPageName)
	  throws Exception
	{
		IncludeWidget widget = createIncludeWidget(page1, optionAndPageName);
		assertHasRegexp("MissingPage.*does not exist", widget.render());
	}

	public void testNoNullPointerWhenIncludingFromRootPage() throws Exception
	{
		verifyNoNullPointerWhenIncludingFromRootPage(".PageOne");
	}

	public void testNoNullPointerWhenIncludingFromRootPageSeamless() throws Exception
	{
		verifyNoNullPointerWhenIncludingFromRootPage("-seamless .PageOne");
	}

	private void verifyNoNullPointerWhenIncludingFromRootPage(String optionAndPageName)
	  throws Exception
	{
		IncludeWidget widget = createIncludeWidget(root, optionAndPageName);
		assertHasRegexp("page one", widget.render());
	}

	public void testIncludingVariables() throws Exception
	{
		verifyIncludingVariables("");
	}

	public void testIncludingVariablesSeamless() throws Exception
	{
		verifyIncludingVariables("-seamless ");
	}

	private void verifyIncludingVariables(String option)
	  throws Exception
	{
		crawler.addPage(root, PathParser.parse("VariablePage"), "This is VariablePage\n!define X {blah!}\n");
		crawler.addPage(root, PathParser.parse("IncludingPage"));
		WidgetRoot widgetRoot = new WidgetRoot("This is IncludingPage\n" + "!include " + option + ".VariablePage\nX=${X}",
		                                       root.getChildPage("IncludingPage"), WidgetBuilder.htmlWidgetBuilder);
		String content = widgetRoot.render();
		assertHasRegexp("X=blah!", content);
	}

	public void testVirtualIncludeNotFound() throws Exception
	{
		verifyVirtualIncludeNotFound("IncludedPage");
	}

	public void testVirtualIncludeNotFoundSeamless() throws Exception
	{
		verifyVirtualIncludeNotFound("-seamless IncludedPage");
	}

	private void verifyVirtualIncludeNotFound(String optionAndPageName)
	  throws Exception
	{
		ProxyPage virtualPage = new ProxyPage("VirtualPage", root, "localhost", 9999, PathParser.parse("RealPage.VirtualPage"));
		IncludeWidget widget = createIncludeWidget(virtualPage, optionAndPageName);
		String output = widget.render();
		assertHasRegexp("IncludedPage.* does not exist", output);
	}

	public void testVirtualInclude() throws Exception
	{
		String virtualWikiURL = "http://localhost:" + FitNesseUtil.port + "/PageTwo";
		VirtualCouplingExtensionTest.setVirtualWiki(page1, virtualWikiURL);
		FitNesseUtil.startFitnesse(root);
		try
		{
			IncludeWidget widget = createIncludeWidget(page1, ".PageOne.ChildOne");
			String result = widget.render();
			verifySubstrings(new String[]{"child page", ".PageOne.ChildOne"}, result);
		}
		finally
		{
			FitNesseUtil.stopFitnesse();
		}
	}

	public void testDeepVirtualInclude() throws Exception
	{
		WikiPagePath atPath = PathParser.parse("AcceptanceTestPage");
		WikiPagePath includedPagePath = PathParser.parse("AcceptanceTestPage.IncludedPage");
		WikiPagePath includingPagePath = PathParser.parse("AcceptanceTestPage.IncludingPage");
		WikiPagePath childOfIncludingPagePath = PathParser.parse("AcceptanceTestPage.IncludingPage.ChildIncludingPage");
		crawler.addPage(root, atPath);
		crawler.addPage(root, includedPagePath, "included page");
		crawler.addPage(root, includingPagePath, "!include .AcceptanceTestPage.IncludedPage");
		crawler.addPage(root, childOfIncludingPagePath, "!include .AcceptanceTestPage.IncludedPage");

		String virtualWikiURL = "http://localhost:" + FitNesseUtil.port + "/AcceptanceTestPage";
		WikiPage alternateRoot = InMemoryPage.makeRoot("RooT");
		WikiPagePath virtualPagePath = PathParser.parse("VirtualPage");
		WikiPage virtualHost = crawler.addPage(alternateRoot, virtualPagePath, "virtual host\n!contents\n");
		VirtualCouplingExtensionTest.setVirtualWiki(virtualHost, virtualWikiURL);

		FitNesseUtil.startFitnesse(root);
		try
		{
			WikiPage virtualChild = crawler.getPage(alternateRoot, PathParser.parse("VirtualPage.IncludingPage"));
			PageData data = virtualChild.getData();
			String result = data.getHtml();
			verifySubstrings(new String[]{"included page", "AcceptanceTestPage.IncludedPage"}, result);
		}
		finally
		{
			FitNesseUtil.stopFitnesse();
		}
	}

	public void testRenderIncludedSibling() throws Exception
	{
		IncludeWidget widget = createIncludeWidget(page1, "PageOne");
		final String result = widget.render();
		verifyRegexes(new String[]{"page one", "Included page: .*PageOne"}, result);
	}

	public void testRenderIncludedSiblingSeamless() throws Exception
	{
		IncludeWidget widget = createIncludeWidget(page1, "-seamless PageOne");
		final String result = widget.render();
		verifySubstrings(new String[]{"page one<br>"}, result);
	}

	public void testRenderIncludedNephew() throws Exception
	{
		IncludeWidget widget = createIncludeWidget(page1, ".PageTwo.ChildOne");
		String result = widget.render();
		verifyRegexes(new String[]{"child page", "class=\"included\""}, result);
	}

	private void verifySubstrings(String[] subStrings, String result)
	{
		for(int i = 0; i < subStrings.length; i++)
		{
			assertSubString(subStrings[i], result);
		}
	}

	private void verifyRegexes(String[] regexes, String result)
	{
		for(int i = 0; i < regexes.length; i++)
		{
			assertHasRegexp(regexes[i], result);
		}
	}

}
