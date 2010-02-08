package fitnesse.responders.search;

import static org.junit.Assert.*;
import static org.junit.internal.matchers.StringContains.*;

import java.util.regex.Pattern;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class SearchReplaceResponderTest {
  private WikiPage root;
  private PageCrawler crawler;
  private SearchReplaceResponder responder;
  private MockRequest request;
  private FitNesseContext context;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    crawler.addPage(root, PathParser.parse("SomePage"), "has something in it");
    responder = new SearchReplaceResponder();
    request = new MockRequest();
    context = FitNesseUtil.makeTestContext(root);
  }

  @Test
  public void testHtml() throws Exception {
    String content = getResponseContentUsingSearchReplaceString("(.*)something(.*)", "$1replacedthing$2");

    assertThat(content, containsString("replacedthing"));
    assertThat(content, containsString("SomePage"));
  }

  @Test
  public void noPageMatched() throws Exception {
    String content = getResponseContentUsingSearchReplaceString("non-available text", "replaced");

    assertThat(content, containsString("No pages matched your search criteria."));
  }

  private String getResponseContentUsingSearchReplaceString(String searchString, String replacementString) throws Exception {
    request.addInput("searchString", searchString);
    request.addInput("replacementString", replacementString);

    Response response = responder.makeResponse(context, request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    return sender.sentData();
  }

}
