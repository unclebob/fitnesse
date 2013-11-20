// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import static util.RegexTestCase.assertHasRegexp;
import static util.RegexTestCase.assertSubString;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.mem.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

public class SearchResponderTest {
  private SearchResponder responder;
  private MockRequest request;
  private FitNesseContext context;

  @Before
  public void setUp() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    WikiPageUtil.addPage(root, PathParser.parse("SomePage"), "has something in it");
    request = new MockRequest();
    request.addInput("searchString", "blah");
    request.addInput("searchType", "blah");
    context = FitNesseUtil.makeTestContext(root);
    responder = new SearchResponder();
  }

  @Test
  public void testHtml() throws Exception {
    String content = getResponseContentUsingSearchString("something");

    assertHasRegexp("something", content);
    assertHasRegexp("SomePage", content);
  }

  @Test
  public void testTableSorterScript() throws Exception {
    String content = getResponseContentUsingSearchString("something");
    // test only small part, since output is chunked
    assertSubString("tableSorter = new TableSorter('searchResultsTable'", content); //, new DateParser(/^(\\w+) (jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec) (\\d+) (\\d+).(\\d+).(\\d+) (\\w+) (\\d+)$/,8,2,3,4,5,6));", content);
  }

  @Test
  public void testClientSideSortScript() throws Exception {
    String content = getResponseContentUsingSearchString("something");
    assertHasRegexp("<script src=\"/files/fitnesse/javascript/clientSideSort.js\"> </script>", content);
  }

  @Test
  public void testPageSortLink() throws Exception {
    String content = getResponseContentUsingSearchString("something");
    assertSubString("<a href=\"javascript:void(tableSorter.sort(1));\">Page</a>", content);
  }

  @Test
  public void testLastModifiedSortLink() throws Exception {
    String content = getResponseContentUsingSearchString("something");
    assertSubString("<a href=\"javascript:void(tableSorter.sort(3, 'date'));\">LastModified</a>", content);
  }

  @Test
  public void testNoSearchStringBringsUpNoResults() throws Exception {
    String content = getResponseContentUsingSearchString("");
    assertSubString("No pages matched your search criteria.", content);
  }

  @Test
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

  @Test
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
