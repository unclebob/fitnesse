package fitnesse.responders.refactoring;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import fitnesse.Responder;
import fitnesse.responders.ResponderTestCase;
import fitnesse.wiki.*;
import org.junit.Before;
import org.junit.Test;

import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;

public class SearchReplaceResponderTest extends ResponderTestCase {
  private WikiPagePath pagePath;
  private WikiPage somePage;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    pagePath = PathParser.parse("SomePage");
    somePage = WikiPageUtil.addPage(root, pagePath, "has something in it");
    request.setResource("SomePage");
  }

  @Override
  protected Responder responderInstance() {
    return new SearchReplaceResponder();
  }

  @Test
  public void testSingleReplacementHtml() throws Exception {
    String content = getResponseContentUsingSearchReplaceString("something", "replacedthing");

    assertThat(content, containsString("replacedthing"));
    assertThat(content, containsString("SomePage"));
  }

  @Test
  public void multipleReplacements() throws Exception {
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage"), "this page has something too.");
    String content = getResponseContentUsingSearchReplaceString("something", "replacedthing");
    assertThat(content, containsString("SomePage"));
    assertThat(content, containsString("ChildPage"));
  }

  @Test
  public void onlyReplacedPagesAreListed() throws Exception {
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage"), "this page has nothing to replace.");
    String content = getResponseContentUsingSearchReplaceString("something", "replacedthing");
    assertThat(content, containsString("SomePage"));
    assertThat(content, not(containsString("ChildPage")));
  }

  @Test
  public void testReplacement() throws Exception {
    getResponseContentUsingSearchReplaceString("something", "replacedthing");
    WikiPage page = root.getPageCrawler().getPage(pagePath);
    assertThat(page.getData().getContent(), containsString("has replacedthing in it"));
  }

  @Test
  public void noPageMatched() throws Exception {
    String content = getResponseContentUsingSearchReplaceString("non-available text", "replaced");

    assertThat(content, containsString("No pages matched your search criteria."));
  }

  @Test
  public void onlySelectedPageAndChildrenAreSearched() throws Exception {
    request.setResource("SomePage.ChildPage");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage"), "this page has something to replace.");
    String content = getResponseContentUsingSearchReplaceString("something", "replacedthing");
    assertThat(content, not(containsString("<a href=\"SomePage\">")));
    assertThat(content, containsString("SomePage.ChildPage"));
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
