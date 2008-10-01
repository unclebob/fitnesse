package fitnesse.responders.run.slimResponder;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import static fitnesse.util.ListUtility.list;
import fitnesse.wiki.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class SlimResponderTest {
  private WikiPage root;
  private PageCrawler crawler;
  private FitNesseContext context;
  private MockRequest request;
  private SlimResponder responder;
  private WikiPage testPage;
  public String testResults;

  private void assertTestResultsContain(String fragment) {
    assertTrue(testResults.indexOf(fragment) != -1);
  }

  private void getResultsForTable(String testTable) throws Exception {
    request.setResource("TestPage");
    PageData data = testPage.getData();
    data.setContent(data.getContent() + "\n" + testTable);
    testPage.commit(data);
    responder.makeResponse(context, request);
    PageData afterTest = responder.getTestResults();
    testResults = afterTest.getContent();
  }

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
  public void unrecognizedTableType() throws Exception {
    getResultsForTable("|XX|\n");
    String fragment = "\"XX\" is not a valid table type";
    assertTestResultsContain(fragment);
  }


  @Test
  public void simpleDecisionTable() throws Exception {
    getResultsForTable(
      "|DT:fitnesse.slim.test.TestSlim|\n" +
        "|returnInt?|\n" +
        "|7|\n"
    );
    assertTestResultsContain("!style_pass(7)");
  }

  @Test
  public void tableWithException() throws Exception {
    getResultsForTable(
      "|DT:NoSuchClass|\n" +
        "|returnInt?|\n" +
        "|7|\n"
    );
    assertTestResultsContain("!anchor");
    assertTestResultsContain(".#");
    assertTestResultsContain("SlimError");
  }

  @Test
  public void tableWithBadConstructorHasException() throws Exception {
    getResultsForTable(
      "|DT:fitnesse.slim.test.TestSlim|badArgument|\n" +
        "|returnConstructorArgument?|\n" +
        "|3|\n"
    );
    assertTestResultsContain("Could not invoke constructor");
    assertTestResultsContain("expected <DT:fitnesse.slim.test.TestSlim>");
  }

  @Test
  public void importTable() throws Exception {
    getResultsForTable(
      "|Import|\n" +
        "|fitnesse.slim.test|\n" +
        "|x.y.z|\n"
    );
    List<Object> instructions = responder.getInstructions();
    assertEquals(
      list(
        list("import_0_0", "import", "fitnesse.slim.test"),
        list("import_0_1", "import", "x.y.z")
      ), instructions    
    );
  }


}
