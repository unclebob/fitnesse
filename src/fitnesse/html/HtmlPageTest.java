// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.html;

import fitnesse.testutil.RegexTest;

public class HtmlPageTest extends RegexTest
{
	private HtmlPage page;
	private String html;

	public void setUp() throws Exception
	{
		page = new HtmlPage();
		html = page.html();
	}

	public void tearDown() throws Exception
	{
	}

	public void testStandardTags() throws Exception
	{
		assertTrue("bad doctype", html.startsWith(HtmlPage.DTD));
		assertSubString("<html>", html);
		assertHasRegexp("</html>", html);
	}

	public void testHead() throws Exception
	{
		assertSubString("<head>", html);
		assertSubString("</head>", html);
		assertSubString("<title>FitNesse</title>", html);
		assertSubString("<link", html);
		assertSubString("rel=\"stylesheet\"", html);
		assertSubString("type=\"text/css\"", html);
		assertSubString("href=\"/files/css/fitnesse.css\"", html);
		assertSubString("src=\"/files/javascript/fitnesse.js\"", html);
	}

	public void testIncludesBody() throws Exception
	{
		assertSubString("<body>", html);
		assertSubString("</body>", html);
	}

	public void testIncludesHeading() throws Exception
	{
		assertSubString("<div class=\"header\"", html);
	}

	public void testMainBar() throws Exception
	{
		assertSubString("<div class=\"mainbar\"", html);
		String mainHtml = page.mainbar.html();
		assertSubString("<div class=\"header", mainHtml);
		assertSubString("<div class=\"main\"", mainHtml);
	}

	public void testSidebar() throws Exception
	{
		assertSubString("<div class=\"sidebar", html);
		assertSubString("<div class=\"art_niche", html);
		assertSubString("<div class=\"actions", html);
	}

	public void testMain() throws Exception
	{
		assertSubString("<div class=\"main", html);
	}

	public void testDivide() throws Exception
	{
		page.main.use(HtmlPage.BreakPoint);
		page.divide();
		assertNotSubString("</html>", page.preDivision);
		assertSubString("</html>", page.postDivision);
		assertNotSubString(HtmlPage.BreakPoint, page.preDivision);
		assertNotSubString(HtmlPage.BreakPoint, page.postDivision);
	}
}