// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.http.*;
import fitnesse.testutil.RegexTest;
import fitnesse.wiki.*;

public class NameResponderTest extends RegexTest
{
	private WikiPage root;
	private NameWikiPageResponder responder;
	private MockRequest request;
	private PageCrawler crawler;
	private String pageOneName;
	private String pageTwoName;
	private String frontPageName;
	private WikiPagePath pageOnePath;
	private WikiPagePath pageTwoPath;
	private WikiPagePath frontPagePath;

	protected void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		crawler = root.getPageCrawler();
		responder = new NameWikiPageResponder();
		request = new MockRequest();

		pageOneName = "PageOne";
		pageTwoName = "PageTwo";
		frontPageName = "FrontPage";

		pageOnePath = PathParser.parse(pageOneName);
		pageTwoPath = PathParser.parse(pageTwoName);
		frontPagePath = PathParser.parse(frontPageName);
	}

	public void testTextPlain() throws Exception
	{

		Response r = responder.makeResponse(new FitNesseContext(root), request);
		assertEquals("text/plain", r.getContentType());
	}

	public void testPageNamesFromRoot() throws Exception
	{
		crawler.addPage(root, pageOnePath);
		crawler.addPage(root, pageTwoPath);
		request.setResource("");
		SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
		assertHasRegexp(pageOneName, response.getContent());
		assertHasRegexp(pageTwoName, response.getContent());
	}

	public void testPageNamesFromASubPage() throws Exception
	{
		WikiPage frontPage = crawler.addPage(root, frontPagePath);
		crawler.addPage(frontPage, pageOnePath);
		crawler.addPage(frontPage, pageTwoPath);
		request.setResource("");
		SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
		assertHasRegexp(frontPageName, response.getContent());
		assertDoesntHaveRegexp(pageOneName, response.getContent());
		assertDoesntHaveRegexp(pageTwoName, response.getContent());

		request.setResource(frontPageName);
		response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
		assertHasRegexp(pageOneName, response.getContent());
		assertHasRegexp(pageTwoName, response.getContent());
		assertDoesntHaveRegexp(frontPageName, response.getContent());
	}
}
