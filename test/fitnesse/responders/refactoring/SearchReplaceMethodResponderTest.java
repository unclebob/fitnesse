package fitnesse.responders.refactoring;

import fitnesse.Responder;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.responders.ResponderTestCase;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageUtil;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class SearchReplaceMethodResponderTest extends ResponderTestCase {
  private WikiPagePath pagePath;
  private WikiPage somePage;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    pagePath = PathParser.parse("SomePage");
    somePage = WikiPageUtil.addPage(root, pagePath, "|update with|param|");
    request.setResource("SomePage");
  }

  @Override
  protected Responder responderInstance() {
    return new SearchReplaceResponder();
  }

  @Test
  public void testSingleReplacementHtml() throws Exception {
    String content = getResponseContentUsingSearchReplace("|update with|param|", "|updated with|param|");

    assertThat(content, containsString("|updated with|param|"));
    assertThat(content, containsString("SomePage"));
  }

  @Test
  public void replacementsWithDifferentContentEvaluatedToSameMethodNoParam() throws Exception {

    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage1"), "|update no param|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage2"), "|updateNoParam|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage3"), "|Update      no param|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage4"), "|ensure|update no param|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage5"), "|reject|update no param|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage6"), "|check|update no param|someValue|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage7"), "|check not|update no param|someValue|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage8"), "|note|update no param|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage9"), "| |update no param|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage10"), "|show|update no param|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage11"), "|$value=|update no param|");

    String content = getResponseContentUsingSearchReplace("|update no param|", "|updated with no param|");

    assertThat(content, containsString("ChildPage1"));
    assertThat(content, containsString("ChildPage2"));
    assertThat(content, containsString("ChildPage3"));
    assertThat(content, containsString("ChildPage4"));
    assertThat(content, containsString("ChildPage5"));
    assertThat(content, containsString("ChildPage6"));
    assertThat(content, containsString("ChildPage7"));
    assertThat(content, containsString("ChildPage8"));
    assertThat(content, containsString("ChildPage9"));
    assertThat(content, containsString("ChildPage10"));
    assertThat(content, containsString("ChildPage11"));
  }

  @Test
  public void replacementsWithDifferentTextEvaluatedToSameMethodOneParam() throws Exception {

    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage1"), "|update with|param|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage2"), "|update      with|param|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage3"), "|ensure|update with|param|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage4"), "|reject|update with|param|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage5"), "|check|update with|param|someValue|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage6"), "|check not|update with|param|someValue|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage7"), "|note|update with|param|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage8"), "| |update with|param|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage9"), "|show|update with|param|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage10"), "|$value=|update with|param|");

    String content = getResponseContentUsingSearchReplace("|update with|param|", "|updated with|param|");

    assertThat(content, containsString("ChildPage1"));
    assertThat(content, containsString("ChildPage2"));
    assertThat(content, containsString("ChildPage3"));
    assertThat(content, containsString("ChildPage4"));
    assertThat(content, containsString("ChildPage5"));
    assertThat(content, containsString("ChildPage6"));
    assertThat(content, containsString("ChildPage7"));
    assertThat(content, containsString("ChildPage8"));
    assertThat(content, containsString("ChildPage9"));
    assertThat(content, containsString("ChildPage10"));
  }

  @Test
  public void replacementsWithDifferentTextEvaluatedToSameMethodTwoParams() throws Exception {

    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage1"), "|update value|param|with value|newParam|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage2"), "|update|param|value with|newParam|value|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage3"), "|update|param|value with value|newParam|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage4"), "|ensure|update value|param|with value|newParam|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage5"), "|reject|update value|param|with value|newParam|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage6"), "|check|update value|param|with value|newParam|someValue|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage7"), "|check not|update value|param|with value|newParam|someValue|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage8"), "|note|update value|param|with value|newParam|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage9"), "| |update value|param|with value|newParam|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage10"), "|show|update value|param|with value|newParam|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage11"), "|$value=|update value|param|with value|newParam|");

    String content = getResponseContentUsingSearchReplace("|update value|param|with value|newParam|", "|updated value|param|with value|newParam|");

    assertThat(content, containsString("ChildPage1"));
    assertThat(content, containsString("ChildPage2"));
    assertThat(content, containsString("ChildPage3"));
    assertThat(content, containsString("ChildPage4"));
    assertThat(content, containsString("ChildPage5"));
    assertThat(content, containsString("ChildPage6"));
    assertThat(content, containsString("ChildPage7"));
    assertThat(content, containsString("ChildPage8"));
    assertThat(content, containsString("ChildPage9"));
    assertThat(content, containsString("ChildPage10"));
    assertThat(content, containsString("ChildPage11"));
  }

  @Test
  public void replacementsWithDifferentTextEvaluatedToSameMethodThreeParams() throws Exception {

    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage1"), "|update value|param1|with value|param2|and|param3|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage2"), "|update|param1|value with|param2|value and|param3|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage3"), "|update|param|value with value|newParam|and|param3|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage4"), "|ensure|update value|param|with value|newParam|and|param3|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage5"), "|reject|update value|param|with value|newParam|and|param3|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage6"), "|check|update value|param|with value|newParam|and|param3|someValue|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage7"), "|check not|update value|param|with value|newParam|and|param3|someValue|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage8"), "|note|update value|param|with value|newParam|and|param3|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage9"), "| |update value|param|with value|newParam|and|param3|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage10"), "|show|update value|param|with value|newParam|and|param3|");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage11"), "|$value=|update value|param|with value|newParam|and|param3|");

    String content = getResponseContentUsingSearchReplace("|update value|param|with value|newParam|and|param3|", "|updated value|param|with value|newParam|and|param3|");

    assertThat(content, containsString("ChildPage1"));
    assertThat(content, containsString("ChildPage2"));
    assertThat(content, containsString("ChildPage3"));
    assertThat(content, containsString("ChildPage4"));
    assertThat(content, containsString("ChildPage5"));
    assertThat(content, containsString("ChildPage6"));
    assertThat(content, containsString("ChildPage7"));
    assertThat(content, containsString("ChildPage8"));
    assertThat(content, containsString("ChildPage9"));
    assertThat(content, containsString("ChildPage10"));
    assertThat(content, containsString("ChildPage11"));
  }

  @Test
  public void onlyReplacedPagesAreListed() throws Exception {
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage"), "|Already update no param|");
    String content = getResponseContentUsingSearchReplace("|update no param|", "|updated no param|");
    assertThat(content, containsString("SomePage"));
    assertThat(content, not(containsString("ChildPage")));
  }

  @Test
  public void testReplacement() throws Exception {
    getResponseContentUsingSearchReplace("|update with|", "|updated with|param|");
    WikiPage page = root.getPageCrawler().getPage(pagePath);
    assertThat(page.getData().getContent(), containsString("|updated with|param|"));
    assertThat(page.getData().getContent(), not(containsString("|update with|param|")));
  }

  @Test
  public void noPageMatched() throws Exception {
    String content = getResponseContentUsingSearchReplace("|update with no|param|", "|updated with|param|");

    assertThat(content, containsString("No pages matched your search criteria."));
  }

  @Test
  public void onlySelectedPageAndChildrenAreSearched() throws Exception {
    request.setResource("SomePage.ChildPage");
    WikiPageUtil.addPage(somePage, PathParser.parse("ChildPage"), "|update with no param|");
    String content = getResponseContentUsingSearchReplace("|update with no param|", "|updated with no param|");
    assertThat(content, not(containsString("<a href=\"SomePage\">")));
    assertThat(content, containsString("SomePage.ChildPage"));
  }

  private String getResponseContentUsingSearchReplace(String searchString, String replacementString) throws Exception {
    request.addInput("searchString", searchString);
    request.addInput("replacementString", replacementString);
    request.addInput("isMethodReplace", "true");
    Response response = responder.makeResponse(context, request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    return sender.sentData();
  }

}
