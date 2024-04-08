// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.reporting.BaseFormatter;
import fitnesse.responders.run.TestResponderTest.JunitTestUtilities;
import fitnesse.responders.run.TestResponderTest.XmlTestUtilities;
import fitnesse.testsystems.TestSummary;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.util.Clock;
import fitnesse.util.DateAlteringClock;
import fitnesse.util.DateTimeUtil;
import fitnesse.util.XmlUtil;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperty;
import fitnesse.wiki.WikiPageUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static fitnesse.wiki.WikiPageProperty.SUITES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static util.RegexTestCase.assertDoesntHaveRegexp;
import static util.RegexTestCase.assertHasRegexp;
import static util.RegexTestCase.assertNotSubString;
import static util.RegexTestCase.assertSubString;

public class SuiteResponderTest {
  private static final String TEST_TIME = "2008-12-05T14:19:00+14:00";
  private MockRequest request;
  private SuiteResponder responder;
  private WikiPage root;
  private WikiPage suite;
  private FitNesseContext context;
  private final String fitPassFixture = "|!-fitnesse.testutil.PassFixture-!|\n";
  private final String fitFailFixture = "|!-fitnesse.testutil.FailFixture-!|\n";
  private final String simpleSlimDecisionTable = "!define TEST_SYSTEM {slim}\n" +
    "|!-DT:fitnesse.slim.test.TestSlim-!|\n" +
    "|string|get string arg?|\n" +
    "|wow|wow|\n";

  @Before
  public void setUp() throws Exception {
    String suitePageName = "SuitePage";
    context = FitNesseUtil.makeTestContext();
    root = context.getRootPage();
    PageData data = root.getData();
    data.setContent(classpathWidgets());
    root.commit(data);
    suite = WikiPageUtil.addPage(root, PathParser.parse(suitePageName), "This is the test suite\n");
    addTestToSuite("TestOne", fitPassFixture);

    request = new MockRequest();
    request.setResource(suitePageName);
    request.addInput("debug", "");
    responder = new SuiteResponder();
    responder.page = suite;

    new DateAlteringClock(DateTimeUtil.getDateFromString(TEST_TIME)).freeze();
  }

  @After
  public void restoreDefaultClock() {
    Clock.restoreDefaultClock();
  }

  private WikiPage addTestToSuite(String name, String content) {
    return addTestPage(suite, name, content);
  }

  private WikiPage addTestPage(WikiPage page, String name, String content) {
    WikiPage testPage = WikiPageUtil.addPage(page, PathParser.parse(name), content);
    PageData data = testPage.getData();
    data.setAttribute("Test");
    testPage.commit(data);
    return testPage;
  }

  @After
  public void tearDown() throws Exception {
    FitNesseUtil.destroyTestContext();
  }

  @Test
  public void nonExistingReturns404() throws Exception {
    request.setResource("DoesNotExist");
    Response response = responder.makeResponse(context, request);

    int status = response.getStatus();
    assertEquals(404, status);

    String results = getResponseContent(response);
    assertSubString("Not Found:DoesNotExist", results);
  }

  @Test
  public void testWithOneTest() throws Exception {
    String results = runSuite();
    assertSubString("href=\\\"#TestOne1\\\"", results);
    assertSubString("1 right", results);
    assertSubString("name=\"TestOne1\"", results);
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
    assertSubString("name=\"TestOne1\"", results);
    assertSubString("name=\"TestTwo2\"", results);
    assertSubString("PassFixture", results);
    assertSubString("FailFixture", results);

    assertSubString("RerunLastFailures", results);
    assertSubString("Rerun Failed", results);
    File rerunPage = responder.getRerunPageFile();
    assertTrue(rerunPage.exists());
    String rerunPageContent = FileUtil.getFileContent(rerunPage);
    assertSubString("SuitePage.TestTwo", rerunPageContent);
    assertNotSubString("TestOne", rerunPageContent);

    // execute rerun suite
    String rerunPageName = responder.getRerunPageName();
    request = new MockRequest();
    request.setResource(rerunPageName);
    responder = new SuiteResponder();
    suite = WikiPageUtil.addPage(root, PathParser.parse(rerunPageName), rerunPageContent);
    responder.page = suite;

    String rerunresults = runSuite();
    assertSubString("href=\\\"#SuitePage.TestTwo1\\\"", rerunresults);
    assertNotSubString("TestOne", rerunresults);
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
    assertSubString("name=\"TestOne1\"", results);
    assertNotSubString("id=\"TestTwo2\"", results);
    assertSubString("PassFixture", results);
    assertNotSubString("FailFixture", results);

    File rerunPage = responder.getRerunPageFile();
    assertFalse(rerunPage.exists());
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
  public void exitCodeHeaderIsErrorCount() throws Exception {
    addTestToSuite("TestFailingTest", fitFailFixture);
    String results = runSuite();
    assertSubString("Exit-Code: 1", results);
  }

  @Test
  public void testNoExitCodeHeaderIfNotChunked() throws Exception {
    responder.turnOffChunking();
    String results = runSuite();
    assertFalse(results.contains("Exit-Code: 0"));
  }

  @Test
  public void testExecutionLogLinkAppears() throws Exception {
    String results = runSuite();
    // Lots of escaping: the content is escaped, since it's written from Javascript.
    // Everything needs to be double escaped because it's handled as a regexp.
    assertHasRegexp("class=\\\\\"ok\\\\\">Execution Log", results);
  }

  @Test
  public void testTestSummaryInformationIncludesPageSummary() throws Exception {
    String results = runSuite();
    assertHasRegexp(".*?Test Pages:.*?&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.*?Assertions:.*?", results);
  }

  @Test
  public void testFormatTestSummaryInformation() throws Exception {
    String results = runSuite();
    assertHasRegexp(".*?<strong>Test Pages:</strong>.*?<strong>Assertions:</strong>.*?", results);
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
  public void testEmptySuiteFilter() throws Exception {
    addTestPagesWithSuiteProperty();
    request.setQueryString("suiteFilter=");
    String results = runSuite();
    assertSubString("href=\\\"#TestTwo3\\\"", results);
    assertSubString("href=\\\"#TestThree2\\\"", results);
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
  public void canRunPartition0() throws Exception {
    addTestPagesWithSuiteProperty();
    request.setQueryString("suiteFilter=smoke,foo&partitionCount=2&partitionIndex=0");
    String results = runSuite();
    assertDoesntHaveRegexp("#TestOne", results);
    assertDoesntHaveRegexp("#TestTwo", results);
    assertHasRegexp("#TestThree", results);
  }

  @Test
  public void canRunPartition1() throws Exception {
    addTestPagesWithSuiteProperty();
    request.setQueryString("suiteFilter=smoke,foo&partitionCount=2&partitionIndex=1");
    String results = runSuite();
    assertDoesntHaveRegexp("#TestOne", results);
    assertHasRegexp("#TestTwo", results);
    assertDoesntHaveRegexp("#TestThree", results);
  }

  @Test
  public void excludeSuiteQuery() throws Exception {
    addTestPagesWithSuiteProperty();
    request.setQueryString("excludeSuiteFilter=foo");
    String results = runSuite();
    assertHasRegexp("#TestOne", results);
    assertDoesntHaveRegexp("#TestTwo", results);
    assertHasRegexp("#TestThree", results);
  }


  @Test
  public void excludeSuiteWithSuiteFilterQuery() throws Exception {
    addTestPagesWithSuiteProperty();
    request.setQueryString("excludeSuiteFilter=bar&suiteFilter=smoke,foo");
    String results = runSuite();
    assertDoesntHaveRegexp("#TestOne", results);
    assertHasRegexp("#TestTwo", results);
    assertDoesntHaveRegexp("#TestThree", results);
  }


  @Test
  public void testFirstTest() throws Exception {
    addTestPagesWithSuiteProperty();
    request.setQueryString("firstTest=TestThree");
    String results = runSuite();
    assertDoesntHaveRegexp("#TestOne", results);
    assertHasRegexp("#TestTwo", results);
    assertHasRegexp("#TestThree", results);
  }

  @Test
  public void testFirstTestWholePath() throws Exception {
    addTestPagesWithSuiteProperty();
    request.setQueryString("firstTest=SuitePage.TestThree");
    String results = runSuite();
    assertDoesntHaveRegexp("#TestOne", results);
    assertHasRegexp("#TestTwo", results);
    assertHasRegexp("#TestThree", results);
  }


  @Test
  public void testTagsShouldBeInheritedFromSuite() throws Exception {
    PageData suiteData = suite.getData();
    suiteData.setAttribute(SUITES, "tag");
    suite.commit(suiteData);
    addTestToSuite("TestInheritsTag", fitPassFixture);

    request.setQueryString("suiteFilter=tag");
    String results = runSuite();
    assertHasRegexp("#TestInheritsTag", results);
  }

  private void addTestPagesWithSuiteProperty() {
    WikiPage test2 = addTestToSuite("TestTwo", fitPassFixture);
    WikiPage test3 = addTestToSuite("TestThree", fitPassFixture);
    PageData data2 = test2.getData();
    PageData data3 = test3.getData();
    data2.setAttribute(SUITES, "foo");
    data3.setAttribute(SUITES, "bar, smoke");
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
    assertHasRegexp("<h3>slim:in-process", results);
  }

  @Test
  public void xmlFormat() throws Exception {
    responder.turnOffChunking();
    request.addInput("format", "xml");
    addTestToSuite("SlimTest", simpleSlimDecisionTable);
    String results = runSuite();
    Document testResultsDocument = XmlTestUtilities.getXmlDocumentFromResults(results);
    Element testResultsElement = testResultsDocument.getDocumentElement();
    assertEquals("testResults", testResultsElement.getNodeName());
    assertEquals("SuitePage", XmlUtil.getTextValue(testResultsElement, "rootPath"));
    assertEquals("2008-12-05T14:19:00+14:00", XmlUtil.getTextValue(testResultsElement, "date"));
    assertEquals("SuitePage?pageHistory&resultDate=20081205141900", XmlUtil.getTextValue(testResultsElement, "pageHistoryLink"));

    NodeList resultList = testResultsElement.getElementsByTagName("result");
    assertEquals(2, resultList.getLength());
    Element testResult;

    for (int elementIndex = 0; elementIndex < 2; elementIndex++) {
      testResult = (Element) resultList.item(elementIndex);
      String pageName = XmlUtil.getTextValue(testResult, "relativePageName");
      assertSubString(pageName + "?pageHistory&resultDate=", XmlUtil.getTextValue(testResult, "pageHistoryLink"));
      if ("SlimTest".equals(pageName)) {
    	  XmlTestUtilities.assertCounts(testResult, "1", "0", "0", "0");
      } else if ("TestOne".equals(pageName)) {
    	  XmlTestUtilities.assertCounts(testResult, "1", "0", "0", "0");
      } else {
        fail(pageName);
      }
    }
    Element finalCounts = XmlUtil.getElementByTagName(testResultsElement, "finalCounts");
    XmlTestUtilities.assertCounts(finalCounts, "2", "0", "0", "0");
  }

  @Test
  public void junitFormat() throws Exception {
    responder.turnOffChunking();
    request.addInput("format", "junit");
    addTestToSuite("SlimTest", simpleSlimDecisionTable);
    String results = runSuite();
    Document testResultsDocument = JunitTestUtilities.getXmlDocumentFromResults(results);
    Element testResultsElement = testResultsDocument.getDocumentElement();
    assertEquals("testsuite", testResultsElement.getNodeName());
    assertEquals("SuitePage",testResultsElement.getAttribute("name"));
    assertEquals("2",testResultsElement.getAttribute("tests"));
    assertEquals("0",testResultsElement.getAttribute("failures"));
    assertEquals("0",testResultsElement.getAttribute("disabled"));
    assertEquals("0",testResultsElement.getAttribute("errors"));

    NodeList resultList = testResultsElement.getElementsByTagName("testcase");
    assertEquals(2, resultList.getLength());
    Element testResult;

    for (int elementIndex = 0; elementIndex < 2; elementIndex++) {
      testResult = (Element) resultList.item(elementIndex);
      String pageName = testResult.getAttribute("name");
      assertSubString(pageName + "?pageHistory&resultDate=", XmlUtil.getTextValue(testResult,"system-out"));
      assertEquals("1",testResult.getAttribute("assertions"));
    }
  }

  @Test
  public void normalSuiteRunWithThreePassingTestsProducesSuiteResultFile() throws Exception {
    File xmlResultsFile = expectedXmlResultsFile();

    if (xmlResultsFile.exists())
      xmlResultsFile.delete();

    addTestToSuite("SlimTestOne", simpleSlimDecisionTable);
    addTestToSuite("SlimTestTwo", simpleSlimDecisionTable);
    runSuite();

    FileInputStream xmlResultsStream = new FileInputStream(xmlResultsFile);
    XmlUtil.newDocument(xmlResultsStream);
    xmlResultsStream.close();
    xmlResultsFile.delete();
  }
  @Test
  public void NoHistory_avoidsProducingSuiteResultFile() throws Exception {
    File xmlResultsFile = expectedXmlResultsFile();

    if (xmlResultsFile.exists())
      xmlResultsFile.delete();

    request.addInput("nohistory", "true");
    addTestToSuite("SlimTestOne", simpleSlimDecisionTable);
    addTestToSuite("SlimTestTwo", simpleSlimDecisionTable);
    runSuite();
    assertFalse(xmlResultsFile.exists());
  }

  @Test
  public void DisableHistory_avoidsProducingSuiteResultFile() throws Exception {
    File xmlResultsFile = expectedXmlResultsFile();

    if (xmlResultsFile.exists())
      xmlResultsFile.delete();
    
    PageData data = suite.getData();
    data.setAttribute(WikiPageProperty.DISABLE_TESTHISTORY);
    suite.commit(data);
    suite.getData();
    responder.page = suite;
    
    addTestToSuite("SlimTestOne", simpleSlimDecisionTable);
    addTestToSuite("SlimTestTwo", simpleSlimDecisionTable);
    runSuite();
    assertFalse(xmlResultsFile.exists());
  }

  @Test
  public void Includehtml_producesHTMLResultsInXMLSuite() throws Exception {
    request.addInput("format", "xml");
    request.addInput("includehtml", "true");
    addTestToSuite("SlimTestOne", simpleSlimDecisionTable);
    addTestToSuite("SlimTestTwo", simpleSlimDecisionTable);
    String results = runSuite();
    assertSubString("<content>", results);
  }

  @Test
  public void Default_producesNoHTMLResultsInXMLSuite() throws Exception {
    request.addInput("format", "xml");
    addTestToSuite("SlimTestOne", simpleSlimDecisionTable);
    addTestToSuite("SlimTestTwo", simpleSlimDecisionTable);
    String results = runSuite();
    assertNotSubString("<content>", results);
  }

  private File expectedXmlResultsFile() {
    TestSummary counts = new TestSummary(3, 0, 0, 0);
    String resultsFileName = String.format("%s/SuitePage/20081205141900_%d_%d_%d_%d.xml",
      context.getTestHistoryDirectory(), counts.getRight(), counts.getWrong(), counts.getIgnores(), counts.getExceptions());
    return new File(resultsFileName);
  }

  @Test
  public void normalSuiteRunProducesIndivualTestHistoryFile() throws Exception {
    TestSummary counts = new TestSummary(1, 0, 0, 0);
    String resultsFileName = String.format("%s/SuitePage.SlimTest/20081205141900_%d_%d_%d_%d.xml",
      context.getTestHistoryDirectory(), counts.getRight(), counts.getWrong(), counts.getIgnores(), counts.getExceptions());
    File xmlResultsFile = new File(resultsFileName);

    if (xmlResultsFile.exists())
      xmlResultsFile.delete();

    addTestToSuite("SlimTest", simpleSlimDecisionTable);
    runSuite();

    assertTrue(resultsFileName, xmlResultsFile.exists());
    FileInputStream xmlResultsStream = new FileInputStream(xmlResultsFile);
    XmlUtil.newDocument(xmlResultsStream);
    xmlResultsStream.close();
    xmlResultsFile.delete();
  }

  @Test
  public void exitCodeHeaderIsErrorCountForXml() throws Exception {
    request.addInput("format", "xml");
    addTestToSuite("TestFailingTest", fitFailFixture);
    String results = runSuite();
    assertSubString("Exit-Code: 1", results);
  }

  @Test
  public void showExecutionLogInXmlFormat() throws Exception {
    request.addInput("format", "xml");
    request.addInput("nochunk", "nochunk");
    addTestToSuite("SlimTest", simpleSlimDecisionTable);

    String results = runSuite();

    assertHasRegexp("<executionLog>", results);
    assertHasRegexp("<testSystem>fit:fit.FitServer</testSystem>", results);
    assertHasRegexp("<testSystem>slim:in-process</testSystem>", results);
    assertHasRegexp("<exitCode>0</exitCode>", results);
    assertHasRegexp("<stdOut>.*</stdOut>", results);
    assertHasRegexp("<stdErr>.*</stdErr>", results);
  }

  @Test
  public void loadsCustomFormatters() throws Exception {

    context.formatterFactory.registerFormatter(FooFormatter.class);
    FooFormatter.initialized = false;

    addTestToSuite("SlimTestOne", simpleSlimDecisionTable);
    runSuite();

    assertTrue(FooFormatter.initialized);
  }

  @Test
  public void testGetRerunPageName_withRerunPrefix() throws Exception {
    String rerunPageName = "RerunLastFailures_SuitePage";
    suite = WikiPageUtil.addPage(root, PathParser.parse(rerunPageName), "This is a rerun page\n");
    request.setResource(rerunPageName);
    responder.makeResponse(context, request);

    String result = responder.getRerunPageName();
    assertEquals(rerunPageName, result);
  }

  @Test
  public void testGetRerunPageName_ReplacesPeriod() throws Exception {
    addTestToSuite("TestTwo", "|!-fitnesse.testutil.FailFixture-!|\n\n|!-fitnesse.testutil.FailFixture-!|\n");
    request.setResource("SuitePage.TestTwo");
    responder.makeResponse(context, request);

    String result = responder.getRerunPageName();
    assertEquals("RerunLastFailures_SuitePage-TestTwo", result);
  }

  @Test
  public void testGetRerunPageName_withoutRerunPrefix() throws Exception {
    request.setResource("SuitePage");
    responder.makeResponse(context, request);

    String result = responder.getRerunPageName();
    assertEquals("RerunLastFailures_SuitePage", result);
  }

  private String runSuite() throws Exception {
    Response response = responder.makeResponse(context, request);

    int status = response.getStatus();
    assertEquals(200, status);
    return getResponseContent(response);
  }

  private String getResponseContent(Response response) throws IOException {
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    return sender.sentData();
  }

  public static class FooFormatter extends BaseFormatter {

    private static boolean initialized;

    public FooFormatter() {
      initialized = true;
    }
  }

}
