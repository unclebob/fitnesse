package fitnesse.responders.editing;

import static org.junit.Assert.*;
import static util.RegexTestCase.assertSubString;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageUtil;
import org.junit.Before;
import org.junit.Test;

public class AddChildPageResponderTest {
  private PageData childPageData;
  private PageCrawler crawler;
  private String childName;
  private MockRequest request;
  private FitNesseContext context;
  private Responder responder;
  private WikiPagePath path;
  private WikiPage root;

  @Before
  public void setUp() throws Exception {
    context = FitNesseUtil.makeTestContext();
    root = context.getRootPage();

    crawler = root.getPageCrawler();
    WikiPageUtil.addPage(root, PathParser.parse("TestPage"), "");
    childName = "ChildPage";
    String childContent = "child content";
    String pagetype = "";
    request = new MockRequest();
    request.setResource("TestPage");
    request.addInput("pageName", childName);
    request.addInput("pageContent", childContent);
    request.addInput("pageType", pagetype);
    responder = new AddChildPageResponder();
    path = PathParser.parse("TestPage.ChildPage");
  }

  @Test
  public void canGetRedirectResponse() throws Exception {
    final SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    final String body = response.getContent();
    assertEquals("", body);
    assertEquals(response.getStatus(), 303);
  }

  @Test
  public void childPageIsMade() throws Exception {
    String helpText = "help text";
    String suites = "tag";
    request.addInput("helpText", helpText);
    request.addInput("suites", suites);
    assertTrue(crawler.getPage(path) == null);
    responder.makeResponse(context, request);
    assertTrue(crawler.getPage(path) != null);
    getChildPage(childName);
    assertEquals(suites, childPageData.getAttribute("Suites"));
    assertEquals(helpText, childPageData.getAttribute("Help"));
  }

  @Test
  public void noPageIsMadeIfNameIsNull() throws Exception {
    request.addInput("pageName", "");
    assertTrue(crawler.getPage(path) == null);
    responder.makeResponse(context, request);
    assertTrue(crawler.getPage(path) == null);
  }

  @Test
  public void noPageIsMadeIfPageAlreadyExists() throws Exception {
    WikiPageUtil.addPage(root, PathParser.parse("TestPage." + childName), "");

    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    assertTrue(response.getStatus() == 409);
    assertSubString("Child page already exists", response.getContent());
  }

  @Test
  public void givesAInvalidNameErrorForAInvalidName() throws Exception {
    request = makeInvalidRequest("");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals(400, response.getStatus());
    assertSubString("Invalid Child Name", response.getContent());

    request = makeInvalidRequest("hello goodbye");
    response = (SimpleResponse) responder.makeResponse(context, request);
    assertSubString("Invalid Child Name", response.getContent());

    request = makeInvalidRequest("1man+1mission");
    response = (SimpleResponse) responder.makeResponse(context, request);
    assertSubString("Invalid Child Name", response.getContent());

    request = makeInvalidRequest("PageOne.PageTwo");
    response = (SimpleResponse) responder.makeResponse(context, request);
    assertSubString("Invalid Child Name", response.getContent());
  }

  private MockRequest makeInvalidRequest(String name) {
    MockRequest request = new MockRequest();
    request.setResource("TestPage");
    request.addInput("pageName", name);
    request.addInput("pageContent", "hello");
    request.addInput("pageType", "");
    return request;
  }

  @Test
  public void withDefaultPageTypeAndPageNameForStaticThenNoAttributeShouldBeSet() throws Exception {
    request.addInput("pageName", "StaticPage");
    responder.makeResponse(context, request);
    getChildPage("StaticPage");
    assertFalse(isSuite());
    assertFalse(isTest());
  }

  @Test
  public void withDefaultPageTypeAndPageNameForTestTheTestAttributeShouldBeSet() throws Exception {
    request.addInput("pageName", "TestPage");
    responder.makeResponse(context, request);
    getChildPage("TestPage");
    assertFalse(isSuite());
    assertTrue(isTest());
  }

  @Test
  public void withDefaultPageTypeAndPageNameForSuiteTheSuiteAttributeShouldBeSet() throws Exception {
    request.addInput("pageName", "SuitePage");
    responder.makeResponse(context, request);
    getChildPage("SuitePage");
    assertTrue(isSuite());
    assertFalse(isTest());
  }

  private boolean isSuite() {
    return childPageData.hasAttribute("Suite");
  }

  @Test
  public void correctAttributeWhenNameHasTestButAttributeIsStatic() throws Exception {
    request.addInput("pageName", "TestChildPage");
    request.addInput("pageType", "Static");
    responder.makeResponse(context, request);
    getChildPage("TestChildPage");
    assertFalse(isTest());
    assertFalse(isSuite());
  }

  @Test
  public void pageTypeShouldBeTestWhenAttributeIsTest() throws Exception {
    request.addInput("pageType", "Test");
    responder.makeResponse(context, request);
    getChildPage(childName);
    assertTrue(isTest());
    assertFalse(isSuite());
  }

  @Test
  public void pageTypeShouldBeSuiteWhenAttributeIsSuite() throws Exception {
    request.addInput("pageType", "Suite");
    responder.makeResponse(context, request);
    getChildPage(childName);
    assertFalse(isTest());
    assertTrue(isSuite());
  }


  @Test
  public void pageTypeShouldBeSuiteWhenPageNameWouldQualifyForTestPage() throws Exception {
    request.addInput("pageType", "Suite");
    request.addInput(EditResponder.PAGE_NAME, "TestChildPage");
    responder.makeResponse(context, request);
    getChildPage("TestChildPage");
    assertFalse(isTest());
    assertTrue(isSuite());
  }

  @Test
  public void createNewPageBasedOnTemplate() throws Exception {
    final String newContent = "To be saved data";
    final String dummyKey = "DummyKey";

    WikiPage template = WikiPageUtil.addPage(root, PathParser.parse("TemplatePage"), "Template data");
    PageData templateData = template.getData();
    templateData.setAttribute(dummyKey, "true");
    template.commit(templateData);

    request.setResource("");
    request.addInput(EditResponder.PAGE_NAME, "TestChildPage");
    request.addInput(EditResponder.CONTENT_INPUT_NAME, newContent);
    request.addInput(EditResponder.TIME_STAMP, "" + SaveRecorder.timeStamp());
    request.addInput(EditResponder.TICKET_ID, "" + SaveRecorder.newTicket());
    request.addInput(NewPageResponder.PAGE_TEMPLATE, ".TemplatePage");

    responder.makeResponse(context, request);

    WikiPage newPage = root.getChildPage("TestChildPage");
    assertNotNull(newPage);
    assertTrue(newPage.getData().hasAttribute(dummyKey));
    assertEquals("true", newPage.getData().getAttribute(dummyKey));
    assertEquals(newContent, newPage.getData().getContent());
  }


  private boolean isTest() {
    return childPageData.hasAttribute("Test");
  }

  private void getChildPage(String childName) throws Exception {
    WikiPage childPage = crawler.getPage(PathParser.parse("TestPage." + childName));
    childPageData = childPage.getData();
  }
}