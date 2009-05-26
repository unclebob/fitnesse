package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.*;
import org.junit.Before;
import org.junit.Test;
import static util.RegexTestCase.*;

public class AddChildPageResponderTest {
  private WikiPage root;
  private WikiPage childPage;
  private PageData childPageData;
  private PageCrawler crawler;
  private String childName;
  private String childContent;
  private String pagetype;
  private MockRequest request;
  private FitNesseContext context;
  private Responder responder;
  private WikiPagePath path;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    crawler = root.getPageCrawler();
    crawler.addPage(root,PathParser.parse("TestPage"));
    childName = "ChildPage";
    childContent = "child content";
    pagetype = "Test";
    request = new MockRequest();
    request.setResource("TestPage");
    request.addInput("name",childName);
    request.addInput("content",childContent);
    request.addInput("pagetype", pagetype);
    context = new FitNesseContext(root);
    responder = new AddChildPageResponder();
    path = PathParser.parse("TestPage.ChildPage");
  }

  @Test
  public void canGetRedirectResponse() throws Exception {
    final SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    final String body = response.getContent();
    assertEquals("",body);
    assertEquals(response.getStatus(),303);

  }

  @Test
  public void childPageIsMade() throws Exception {
    assertTrue(crawler.getPage(root,path) == null);
    responder.makeResponse(context, request);
    assertTrue(crawler.getPage(root,path) != null);
  }

  @Test
  public void noPageIsMadeIfNameIsNull() throws Exception {
    MockRequest request2 = new MockRequest();
    request2.setResource("TestPage");
    request2.addInput("name","");
    request2.addInput("content",childContent);
    request2.addInput("pagetype", pagetype);


    assertTrue(crawler.getPage(root,path) == null);
    responder.makeResponse(context, request2);
    assertTrue(crawler.getPage(root,path) == null);
  }

  @Test
  public void correctAttributeSetWhenPageTypeIsNull() throws Exception {
    MockRequest request2 = new MockRequest();
    request2.setResource("TestPage");
    request2.addInput("name",childName);
    request2.addInput("content",childContent);
    request2.addInput("pagetype", "");
    responder.makeResponse(context,request2);
    getChildPage();
    assertFalse(childPageData.hasAttribute("Test"));
    assertFalse(childPageData.hasAttribute("Suite"));

    MockRequest request3 = new MockRequest();
    request3.setResource("TestPage");
    request3.addInput("name","TestChildPage");
    path = PathParser.parse("TestPage.TestChildPage");
    request3.addInput("content",childContent);
    request3.addInput("pagetype", "");
    responder.makeResponse(context,request3);
    getChildPage();
    assertTrue(childPageData.hasAttribute("Test"));
    assertFalse(childPageData.hasAttribute("Suite"));

  }

  @Test
  public void correctAttributeWhenNameHasTestButAttributeIsNormal() throws Exception {
    MockRequest request3 = new MockRequest();
    request3.setResource("TestPage");
    request3.addInput("name","TestChildPage");
    path = PathParser.parse("TestPage.TestChildPage");
    request3.addInput("content",childContent);
    request3.addInput("pagetype", "Normal");
    responder.makeResponse(context,request3);
    getChildPage();
    assertFalse(childPageData.hasAttribute("Test"));
    assertFalse(childPageData.hasAttribute("Suite"));
  }
  @Test
  public void childPageHasCorrectType() throws Exception {
    responder.makeResponse(context,request);
    getChildPage();
    assertTrue(childPageData.hasAttribute("Test"));
    assertFalse(childPageData.hasAttribute("Suite"));
  }

  private void getChildPage() throws Exception {
    childPage = crawler.getPage(root,path);
    childPageData = childPage.getData();
  }
}