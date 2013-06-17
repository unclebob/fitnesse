// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import fitnesse.wiki.WikiPageUtil;
import util.RegexTestCase;
import fitnesse.FitNesseContext;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.wiki.mem.InMemoryPage;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class SearchResponderTest extends RegexTestCase {
  private WikiPage root;
  private SearchResponder responder;
  private MockRequest request;
  private FitNesseContext context;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    WikiPageUtil.addPage(root, PathParser.parse("SomePage"), "has something in it");
    request = new MockRequest();
    request.addInput("searchString", "blah");
    request.addInput("searchType", "blah");
    context = FitNesseUtil.makeTestContext(root);
    responder = new SearchResponder();
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
    // test only small part, since output is chunked
    assertSubString("tableSorter = new TableSorter('searchResultsTable'", content); //, new DateParser(/^(\\w+) (jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec) (\\d+) (\\d+).(\\d+).(\\d+) (\\w+) (\\d+)$/,8,2,3,4,5,6));", content);
  }

  public void testClientSideSortScript() throws Exception {
    String content = getResponseContentUsingSearchString("something");
    assertHasRegexp("<script src=\"/files/fitnesse/javascript/clientSideSort.js\"> </script>", content);
  }

  public void testPageSortLink() throws Exception {
    String content = getResponseContentUsingSearchString("something");
    assertSubString("<a href=\"javascript:void(tableSorter.sort(1));\">Page</a>", content);
  }

  public void testLastModifiedSortLink() throws Exception {
    String content = getResponseContentUsingSearchString("something");
    assertSubString("<a href=\"javascript:void(tableSorter.sort(3, 'date'));\">LastModified</a>", content);
  }

  public void testNoSearchStringBringsUpNoResults() throws Exception {
    String content = getResponseContentUsingSearchString("");
    assertSubString("No pages matched your search criteria.", content);
  }

  public void testEscapesSearchString() throws Exception {
    String content = getResponseContentUsingSearchString("!+-<&>");
    assertSubString("!+-<&>", content);
  }

  private String getResponseContentUsingSearchString(String searchString) throws Exception {
    request.addInput("searchString", searchString);

    Response response = responder.makeResponse(context, request);
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

}
