package fitnesse.responders.editing;

import util.RegexTestCase;
import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class NewPageResponderTest extends RegexTestCase {

  private WikiPage root;
  private MockRequest request;
  private NewPageResponder responder;
  private PageCrawler crawler;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    FitNesseUtil.makeTestContext(root);
    crawler = root.getPageCrawler();
    request = new MockRequest();
    responder = new NewPageResponder();
  }

  public void testResponse() throws Exception {
    request.setResource("root");

    SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root),
        request);
    assertEquals(200, response.getStatus());

    String body = response.getContent();
    assertSubString("<html>", body);
    assertSubString("<form", body);
    assertSubString("method=\"post\"", body);
    assertSubString("name=\"responder\"", body);
    assertSubString("name=\"" + EditResponder.HELP_TEXT + "\"", body);
    assertSubString("type=\"submit\"", body);
    assertSubString("textarea class=\"wikitext no_wrap\"", body);
  }

}
