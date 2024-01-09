package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static util.RegexTestCase.assertHasRegexp;

public class ScopeVariablesResponderTest {
  protected WikiPage root;
  protected MockRequest request;
  protected Responder responder;
  protected FitNesseContext context;

  @Before
  public void setUp() throws Exception {
    context = FitNesseUtil.makeTestContext();
    root = context.getRootPage();
    request = new MockRequest();
    responder = new ScopeVariablesResponder();
    WikiPageUtil.addPage(root, PathParser.parse("SimplePageForInclude"), "!define y {Y}");
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("SimplePage"), "!define x {X}\n!include .SimplePageForInclude");
    WikiPage childPage = WikiPageUtil.addPage(page, PathParser.parse("ChildPage"), "!define z {Z}\n!define a {A}");
    WikiPageUtil.addPage(childPage, PathParser.parse("GrandChildPage"), "!define a {B}\n!define b {BB}");
  }

  @Test
  public void shouldListVariables() throws Exception {
    request.setResource("SimplePage.ChildPage.GrandChildPage");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context,request);
    assertHasRegexp("<tr>.*?<td>x</td>.*?<td>X</td>.*<td><a href=\"SimplePage\">SimplePage</a></td>.*?</tr>", response.getContent());
    assertHasRegexp("<tr>.*?<td>y</td>.*?<td>Y</td>.*<td><a href=\"SimplePage\">SimplePage</a></td>.*?</tr>", response.getContent());
    assertHasRegexp("<tr>.*?<td>z</td>.*?<td>Z</td>.*?<td><a href=\"SimplePage.ChildPage\">SimplePage.ChildPage</a></td>.*?</tr>", response.getContent());
    assertHasRegexp("<tr>.*?<td>a</td>.*?<td>B</td>.*?<td><a href=\"SimplePage.ChildPage.GrandChildPage\">SimplePage.ChildPage.GrandChildPage</a></td>.*?</tr>", response.getContent());
    assertHasRegexp("<tr>.*?<td>b</td>.*?<td>BB</td>.*?<td><a href=\"SimplePage.ChildPage.GrandChildPage\">SimplePage.ChildPage.GrandChildPage</a></td>.*?</tr>", response.getContent());
  }
}
