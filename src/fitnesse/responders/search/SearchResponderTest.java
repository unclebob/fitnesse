// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.search;

import fitnesse.testutil.*;
import fitnesse.http.*;
import fitnesse.wiki.*;
import fitnesse.FitNesseContext;
import fitnesse.components.*;

public class SearchResponderTest extends RegexTest
{
	private WikiPage root;
	private PageCrawler crawler;
	private SearchResponder responder;
	private MockRequest request;

	public void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		crawler = root.getPageCrawler();
		crawler.addPage(root, PathParser.parse("SomePage"), "has something in it");
		responder = new SearchResponder();
		request = new MockRequest();
		request.addInput("searchString", "blah");
		request.addInput("searchType", "blah");
	}

	public void tearDown() throws Exception
	{
	}

	public void testHtml() throws Exception
	{
		String content = getResponseContentUsingSearchString("something");

		assertHasRegexp("something", content);
		assertHasRegexp("SomePage", content);
	}

	public void testEscapesSearchString() throws Exception
	{
		String content = getResponseContentUsingSearchString("!+-<&>");
		assertSubString("!+-<&>", content);
	}

	private String getResponseContentUsingSearchString(String searchString) throws Exception
	{
		request.addInput("searchString", searchString);

		Response response = responder.makeResponse(new FitNesseContext(root), request);
   	MockResponseSender sender = new MockResponseSender();
		response.readyToSend(sender);
		sender.waitForClose(5000);
		return sender.sentData();
	}

	public void testTitle() throws Exception
	{
		request.addInput("searchType", "something with the word title in it");
		responder.setRequest(request);
		String title = responder.getTitle();
		assertSubString("Title Search Results", title);

		request.addInput("searchType", "something with the word content in it");
		title = responder.getTitle();
		assertSubString("Content Search Results", title);
	}

	public void testActivatingProperSearch() throws Exception
	{
		TestableSearcher searcher = new TestableSearcher();
		responder.setSearcher(searcher);
		responder.setRequest(request);

		request.addInput("searchType", "something with the word title in it");
		new MockResponseSender(responder.makeResponse(new FitNesseContext(root), request));
		assertTrue(searcher.titleSearchCalled);

		request.addInput("searchType", "something with the word content in it");
		new MockResponseSender(responder.makeResponse(new FitNesseContext(root), request));
		assertTrue(searcher.contentSearchCalled);
	}

	private static class TestableSearcher extends Searcher
	{
		boolean contentSearchCalled = false;
		boolean titleSearchCalled = false;

		public TestableSearcher() throws Exception
		{
			super("", null);
		}

		public void searchContent(SearchObserver observer) throws Exception
		{
			contentSearchCalled = true;
		}

		public void searchTitles(SearchObserver observer)
		{
			titleSearchCalled = true;
		}
	}
}
