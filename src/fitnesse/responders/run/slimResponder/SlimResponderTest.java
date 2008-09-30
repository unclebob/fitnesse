package fitnesse.responders.run.slimResponder;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.wiki.*;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class SlimResponderTest {
  private WikiPage root;
  private PageCrawler crawler;
  private FitNesseContext context;
  private MockRequest request;
  private SlimResponder responder;
  private WikiPage testPage;


  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    crawler = root.getPageCrawler();
    context = new FitNesseContext(root);
    request = new MockRequest();
    responder = new SlimResponder();
    testPage = crawler.addPage(root, PathParser.parse("TestPage"), "!path classes");
  }


  @Test
  public void slimResponderStartsAndQuitsSlim() throws Exception {
    request.setResource("TestPage");
    responder.makeResponse(context, request);
    assertTrue(!responder.slimOpen());
  }

  @Test
  public void pageHasStandardInAndOutSections() throws Exception {
    request.setResource("TestPage");
    responder.makeResponse(context, request);
    PageData afterTest = responder.getTestResults();
    String testResults = afterTest.getContent();
    assertTrue(testResults.indexOf("!* Standard Output\n\n") != -1);
    assertTrue(testResults.indexOf("!* Standard Error\n\n") != -1);
  }

  @Test
  public void simpleDecisionTable() throws Exception {
    request.setResource("TestPage");
    PageData data = testPage.getData();
    data.setContent(data.getContent() + "\n" +
      "|fitnesse.slim.test.TestSlim|\n" +
      "|returnInt?|\n" +
      "|7|\n"
    );
    testPage.commit(data);
    responder.makeResponse(context, request);
    PageData afterTest = responder.getTestResults();
    String testResults = afterTest.getContent();
    assertTrue(testResults.indexOf("!style_pass(7)") != -1);
  }

  @Test
  public void tableWithException() throws Exception {
    request.setResource("TestPage");
    PageData data = testPage.getData();
    data.setContent(data.getContent() + "\n" +
      "|NoSuchClass|\n" +
      "|returnInt?|\n" +
      "|7|\n"
    );
    testPage.commit(data);
    responder.makeResponse(context, request);
    PageData afterTest = responder.getTestResults();
    String testResults = afterTest.getContent();
    assertTrue(testResults.indexOf("!anchor") != -1);
    assertTrue(testResults.indexOf(".#") != -1);
    assertTrue(testResults.indexOf("SlimError") != -1);
  }
}
