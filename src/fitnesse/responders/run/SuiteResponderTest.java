// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.testutil.FitSocketReceiver;
import fitnesse.wiki.*;
import static junit.framework.Assert.fail;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import static util.RegexTestCase.*;
import util.XmlUtil;

public class SuiteResponderTest {
  private MockRequest request;
  private SuiteResponder responder;
  private WikiPage root;
  private WikiPage suite;
  private FitNesseContext context;
  private FitSocketReceiver receiver;
  private PageCrawler crawler;
  private String suitePageName;
  private final String fitPassFixture = "|!-fitnesse.testutil.PassFixture-!|\n";
  private final String simpleSlimDecisionTable = "!define TEST_SYSTEM {slim}\n" +
    "|!-DT:fitnesse.slim.test.TestSlim-!|\n" +
    "|string|get string arg?|\n" +
    "|wow|wow|\n";

  @Before
  public void setUp() throws Exception {
    suitePageName = "SuitePage";
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    PageData data = root.getData();
    data.setContent(classpathWidgets());
    root.commit(data);
    suite = crawler.addPage(root, PathParser.parse(suitePageName), "This is the test suite\n");
    addTestToSuite("TestOne", fitPassFixture);

    request = new MockRequest();
    request.setResource(suitePageName);
    responder = new SuiteResponder();
    responder.turnOffChunkingForTests();
    responder.setFastTest(true);
    responder.page = suite;
    context = new FitNesseContext(root);

    receiver = new FitSocketReceiver(0, context.socketDealer);
  }

  private WikiPage addTestToSuite(String name, String content) throws Exception {
    return addTestPage(suite, name, content);
  }

  private WikiPage addTestPage(WikiPage page, String name, String content) throws Exception {
    WikiPage testPage = crawler.addPage(page, PathParser.parse(name), content);
    PageData data = testPage.getData();
    data.setAttribute("Test");
    testPage.commit(data);
    return testPage;
  }

  @After
  public void tearDown() throws Exception {
    receiver.close();
  }

  private String runSuite() throws Exception {
    context.port = receiver.receiveSocket();
    Response response = responder.makeResponse(context, request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    String results = sender.sentData();
    return results;
  }


  @Test
  public void testWithOneTest() throws Exception {
    String results = runSuite();
    assertSubString("href=\\\"#TestOne1\\\"", results);
    assertSubString("1 right", results);
    assertSubString("id=\"TestOne1\"", results);
    assertSubString(" href=\"SuitePage.TestOne\"", results);
    assertSubString("PassFixture", results);
  }

  @Test
  public void testPageWithXref() throws Exception {
    PageData data = suite.getData();
    data.setContent("!see XrefOne\r\n!see XrefTwo\n!see XrefThree\n");
    suite.commit(data);
    addTestPage(root, "XrefOne", fitPassFixture);
    addTestPage(root, "XrefTwo", fitPassFixture);

    String results = runSuite();
    assertSubString("href=\\\"#XrefOne2\\\"", results);
    assertSubString("href=\\\"#XrefTwo3\\\"", results);
  }

  @Test
  public void testWithTwoTests() throws Exception {
    addTestToSuite("TestTwo", "|!-fitnesse.testutil.FailFixture-!|\n\n|!-fitnesse.testutil.FailFixture-!|\n");
    String results = runSuite();

    assertSubString("href=\\\"#TestOne1\\\"", results);
    assertSubString("href=\\\"#TestTwo2\\\"", results);
    assertSubString("1 right", results);
    assertSubString("2 wrong", results);
    assertSubString("id=\"TestOne1\"", results);
    assertSubString("id=\"TestTwo2\"", results);
    assertSubString("PassFixture", results);
    assertSubString("FailFixture", results);
  }

  @Test
  public void testWithPrunedPage() throws Exception {
    WikiPage pageTwo = addTestToSuite("TestTwo",
      "|!-fitnesse.testutil.FailFixture-!|\n\n|!-fitnesse.testutil.FailFixture-!|\n"
    );
    PageData data = pageTwo.getData();
    data.setAttribute("Prune");
    pageTwo.commit(data);
    String results = runSuite();

    assertSubString("href=\\\"#TestOne1\\\"", results);
    assertNotSubString("href=\\\"#TestTwo2\\\"", results);
    assertSubString("1 right", results);
    assertSubString("0 wrong", results);
    assertSubString("id=\"TestOne1\"", results);
    assertNotSubString("id=\"TestTwo2\"", results);
    assertSubString("PassFixture", results);
    assertNotSubString("FailFixture", results);
  }

  @Test
  public void testSuiteWithEmptyPage() throws Exception {
    suite = crawler.addPage(root, PathParser.parse("SuiteWithEmptyPage"), "This is the empty page test suite\n");
    addTestPage(suite, "TestThatIsEmpty", "");
    request.setResource("SuiteWithEmptyPage");
    runSuite();

    WikiPagePath errorLogPath = PathParser.parse("ErrorLogs.SuiteWithEmptyPage");
    WikiPage errorLog = crawler.getPage(root, errorLogPath);
    PageData data = errorLog.getData();
    String errorLogContent = data.getContent();
    assertNotSubString("Exception", errorLogContent);
  }

  @Test
  public void testSuiteWithOneTestWithoutTable() throws Exception {
    addTestToSuite("TestWithoutTable", "This test has not table");
    addTestToSuite("TestTwo", fitPassFixture);
    addTestToSuite("TestThree", fitPassFixture);
    String results = runSuite();

    assertSubString("TestOne", results);
    assertSubString("TestTwo", results);
    assertSubString("TestThree", results);
    assertSubString("TestWithoutTable", results);
  }

  @Test
  public void testExitCodeHeader() throws Exception {
    String results = runSuite();
    assertSubString("Exit-Code: 0", results);
  }


  @Test
  public void testExecutionStatusAppears() throws Exception {
    String results = runSuite();
    assertHasRegexp(divWithIdAndContent("execution-status", ".*?"), results);
  }

  @Test
  public void testTestSummaryInformationIncludesPageSummary() throws Exception {
    String results = runSuite();
    assertHasRegexp(divWithIdAndContent("test-summary",
      ".*?Test Pages:.*?&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.*?Assertions:.*?"
    ), results
    );
  }

  @Test
  public void testFormatTestSummaryInformation() throws Exception {
    String results = runSuite();
    assertHasRegexp(divWithIdAndContent("test-summary",
      ".*?<strong>Test Pages:</strong>.*?<strong>Assertions:</strong>.*?"
    ), results
    );
  }

  private String classpathWidgets() {
    return "!path classes\n" +
      "!path lib/dummy.jar\n";
  }

  @Test
  public void testNonMatchingSuiteFilter() throws Exception {
    addTestPagesWithSuiteProperty();
    request.setQueryString("suiteFilter=xxx");
    String results = runSuite();
    assertDoesntHaveRegexp(".*href=\\\"#TestOne\\\".*", results);
    assertDoesntHaveRegexp(".*href=\\\"#TestTwo\\\".*", results);
    assertDoesntHaveRegexp(".*href=\\\"#TestThree\\\".*", results);
  }

  @Test
  public void testSimpleMatchingSuiteQuery() throws Exception {
    addTestPagesWithSuiteProperty();
    request.setQueryString("suiteFilter=foo");
    String results = runSuite();
    assertDoesntHaveRegexp(".*href=\\\"#TestOne.*", results);
    assertSubString("href=\\\"#TestTwo1\\\"", results);
    assertDoesntHaveRegexp(".*href=\\\"#TestThree.*", results);
  }

  @Test
  public void testSecondMatchingSuiteQuery() throws Exception {
    addTestPagesWithSuiteProperty();
    request.setQueryString("suiteFilter=smoke");
    String results = runSuite();
    assertDoesntHaveRegexp(".*href=\\\"#TestOne.*", results);
    assertDoesntHaveRegexp(".*href=\\\"#TestTwo.*", results);
    assertSubString("href=\\\"#TestThree1\\\"", results);
  }

  @Test
  public void multipleSuiteQuery() throws Exception {
    addTestPagesWithSuiteProperty();
    request.setQueryString("suiteFilter=smoke,foo");
    String results = runSuite();
    assertDoesntHaveRegexp("#TestOne", results);
    assertHasRegexp("#TestTwo", results);
    assertHasRegexp("#TestThree", results);
  }

  @Test
  public void testTagsShouldBeInheritedFromSuite() throws Exception {
    PageData suiteData = suite.getData();
    suiteData.setAttribute(PageData.PropertySUITES, "tag");
    suite.commit(suiteData);
    addTestToSuite("TestInheritsTag", fitPassFixture);

    request.setQueryString("suiteFilter=tag");
    String results = runSuite();
    assertHasRegexp("#TestInheritsTag", results);
  }

  private void addTestPagesWithSuiteProperty() throws Exception {
    WikiPage test2 = addTestToSuite("TestTwo", fitPassFixture);
    WikiPage test3 = addTestToSuite("TestThree", fitPassFixture);
    PageData data2 = test2.getData();
    PageData data3 = test3.getData();
    data2.setAttribute(PageData.PropertySUITES, "foo");
    data3.setAttribute(PageData.PropertySUITES, "bar, smoke");
    test2.commit(data2);
    test3.commit(data3);
  }

  @Test
  public void testCanMixSlimAndFitTests() throws Exception {
    addTestToSuite("SlimTest", simpleSlimDecisionTable);
    String results = runSuite();
    assertHasRegexp("<td>fitnesse.testutil.PassFixture</td>", results);
    assertHasRegexp("<td><span class=\"pass\">wow</span></td>", results);
    assertHasRegexp("<h3>fit:fit.FitServer</h3>", results);
    assertHasRegexp("<h3>slim:fitnesse.slim.SlimService", results);
  }

  @Test
  public void xmlFormat() throws Exception {
    request.addInput("format", "xml");
    addTestToSuite("SlimTest", simpleSlimDecisionTable);
    String results = runSuite();
    Document testResultsDocument = TestResponderTest.getXmlDocumentFromResults(results);
    Element testResultsElement = testResultsDocument.getDocumentElement();
    assertEquals("testResults", testResultsElement.getNodeName());
    NodeList resultList = testResultsElement.getElementsByTagName("result");
    assertEquals(2, resultList.getLength());
    Element testResult;

    for (int elementIndex = 0; elementIndex < 2; elementIndex++) {
      testResult = (Element) resultList.item(elementIndex);
      String pageName = XmlUtil.getTextValue(testResult, "relativePageName");
      if ("SlimTest".equals(pageName)) {
        TestResponderTest.assertCounts(testResult, "2", "0", "0", "0");
        assertSubString("DT:fitnesse.slim.test.TestSlim", XmlUtil.getTextValue(testResult, "content"));
        Element instructions = XmlUtil.getElementByTagName(testResult, "instructions");
        assertTrue(instructions != null);
      } else if ("TestOne".equals(pageName)) {
        TestResponderTest.assertCounts(testResult, "1", "0", "0", "0");
        assertSubString("PassFixture", XmlUtil.getTextValue(testResult, "content"));
      } else {
        fail(pageName);
      }
    }
    Element finalCounts = XmlUtil.getElementByTagName(testResultsElement, "finalCounts");
    TestResponderTest.assertCounts(finalCounts, "2", "0", "0", "0");
  }

  @Test
  public void xmlForSingleTestPageNameIsParenthetic() throws Exception {
    request.setResource("SuitePage.TestOne");
    request.addInput("format", "xml");
    String results = runSuite();
    Document testResultsDocument = TestResponderTest.getXmlDocumentFromResults(results);
    Element testResultsElement = testResultsDocument.getDocumentElement();
    assertEquals("testResults", testResultsElement.getNodeName());
    NodeList resultList = testResultsElement.getElementsByTagName("result");
    assertEquals(1, resultList.getLength());
    Element result = (Element) resultList.item(0);
    String pageName = XmlUtil.getTextValue(result, "relativePageName");
    assertEquals("(TestOne)", pageName);
  }
}
