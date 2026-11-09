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
import fitnesse.testutil.FitNesseUtil;

import java.util.Properties;

public class SearchReplaceResponderTest extends ResponderTestCase {
  private WikiPagePath pagePath;
  private WikiPage somePage;

  @Override
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

  @Test
  public void replacesInPagesBehindSymbolicLinks() throws Exception {
    addSymbolicLinkToLinkedPage();

    String content = getResponseContentUsingSearchReplaceString("something", "replacedthing");

    assertThat(content, containsString("SomePage.SymLink"));
    assertEquals("linked page has replacedthing too", root.getChildPage("LinkedPage").getData().getContent());
  }

  @Test
  public void replacesWithOneThreadOnly() {
    request.addInput("searchThreads", "4");
    SearchReplaceResponder searchReplaceResponder = (SearchReplaceResponder) responder;
    searchReplaceResponder.setRequest(request);
    assertEquals(1, searchReplaceResponder.getSearchThreads());
  }

  @Test
  public void skipsPagesBehindSymbolicLinksWhenConfigured() throws Exception {
    Properties properties = new Properties();
    properties.setProperty("Search.includeSymLinks", "false");
    context = FitNesseUtil.makeTestContext(properties);
    root = context.getRootPage();
    somePage = WikiPageUtil.addPage(root, pagePath, "has something in it");
    addSymbolicLinkToLinkedPage();

    String content = getResponseContentUsingSearchReplaceString("something", "replacedthing");

    assertThat(content, not(containsString("SomePage.SymLink")));
    assertEquals("linked page has something too", root.getChildPage("LinkedPage").getData().getContent());
    assertEquals("has replacedthing in it", somePage.getData().getContent());
  }

  private void addSymbolicLinkToLinkedPage() {
    WikiPageUtil.addPage(root, PathParser.parse("LinkedPage"), "linked page has something too");
    PageData data = somePage.getData();
    data.getProperties().set(SymbolicPage.PROPERTY_NAME).set("SymLink", ".LinkedPage");
    somePage.commit(data);
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
