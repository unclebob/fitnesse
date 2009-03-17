// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import util.RegexTestCase;
import fitnesse.FitNesseContext;
import fitnesse.components.SearchObserver;
import fitnesse.components.Searcher;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class SearchResponderTest extends RegexTestCase {
  private WikiPage root;
  private PageCrawler crawler;
  private SearchResponder responder;
  private MockRequest request;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    crawler.addPage(root, PathParser.parse("SomePage"), "has something in it");
    responder = new SearchResponder();
    request = new MockRequest();
    request.addInput("searchString", "blah");
    request.addInput("searchType", "blah");
  }

  public void tearDown() throws Exception {
  }

  public void testHtml() throws Exception {
    String content = getResponseContentUsingSearchString("something");

    assertHasRegexp("something", content);
    assertHasRegexp("SomePage", content);
  }

  public void testTableSorterScript() throws Exception {
    String content = getResponseContentUsingSearchString("something");
    assertSubString("<script language=\"javascript\">tableSorter = new TableSorter('searchResultsTable', new DateParser(/^(\\w+) (jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec) (\\d+) (\\d+).(\\d+).(\\d+) (\\w+) (\\d+)$/,8,2,3,4,5,6));</script>", content);
  }

  public void testClientSideSortScript() throws Exception {
    String content = getResponseContentUsingSearchString("something");
    assertHasRegexp("<script src=\"/files/javascript/clientSideSort.js\"> </script>", content);
  }

  public void testPageSortLink() throws Exception {
    String content = getResponseContentUsingSearchString("something");
    assertSubString("<a href=\"javascript:void(tableSorter.sort(0));\">Page</a>", content);
  }

  public void testLastModifiedSortLink() throws Exception {
    String content = getResponseContentUsingSearchString("something");
    assertSubString("<a href=\"javascript:void(tableSorter.sort(1, 'date'));\">LastModified</a>", content);
  }

  public void testNoSearchStringBringsUpNoResults() throws Exception {
    String content = getResponseContentUsingSearchString("");
    assertSubString("Found 0 results for your search.", content);
  }

  public void testEscapesSearchString() throws Exception {
    String content = getResponseContentUsingSearchString("!+-<&>");
    assertSubString("!+-<&>", content);
  }

  private String getResponseContentUsingSearchString(String searchString) throws Exception {
    request.addInput("searchString", searchString);

    Response response = responder.makeResponse(new FitNesseContext(root), request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    return sender.sentData();
  }

  public void testTitle() throws Exception {
    request.addInput("searchType", "something with the word title in it");
    responder.setRequest(request);
    String title = responder.getTitle();
    assertSubString("Title Search Results", title);

    request.addInput("searchType", "something with the word content in it");
    title = responder.getTitle();
    assertSubString("Content Search Results", title);
  }

  public void testActivatingProperSearch() throws Exception {
    TestableSearcher searcher = new TestableSearcher();
    responder.setSearcher(searcher);
    responder.setRequest(request);

    request.addInput("searchType", "something with the word title in it");
    Response response = responder.makeResponse(new FitNesseContext(root), request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    assertTrue(searcher.titleSearchCalled);

    request.addInput("searchType", "something with the word content in it");
    response = responder.makeResponse(new FitNesseContext(root), request);
    sender = new MockResponseSender();
    sender.doSending(response);
    assertTrue(searcher.contentSearchCalled);
  }

  public void testJavascriptDateFormatRegex() {
    assertEquals("/^(\\w+) (jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec) (\\d+) (\\d+).(\\d+).(\\d+) (\\w+) (\\d+)$/", SearchResponder.getDateFormatJavascriptRegex());
  }

  private static class TestableSearcher extends Searcher {
    boolean contentSearchCalled = false;
    boolean titleSearchCalled = false;

    public TestableSearcher() throws Exception {
      super("", null);
    }

    public void searchContent(SearchObserver observer) throws Exception {
      contentSearchCalled = true;
    }

    public void searchTitles(SearchObserver observer) {
      titleSearchCalled = true;
    }
  }
}
