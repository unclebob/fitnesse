// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.FitNesseVersion;
import fitnesse.wikitext.Utils;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureTestOperation;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.responders.SecureResponder;
import fitnesse.testutil.FitSocketReceiver;
import static fitnesse.testutil.RegexTestCase.*;
import fitnesse.util.XmlUtil;
import fitnesse.wiki.*;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestResponderTest {
  private WikiPage root;
  private MockRequest request;
  private TestResponder responder;
  private FitNesseContext context;
  private Response response;
  private MockResponseSender sender;
  private WikiPage testPage;
  private String results;
  private FitSocketReceiver receiver;
  private WikiPage errorLogsParentPage;
  private PageCrawler crawler;
  private String simpleRunPageName;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    errorLogsParentPage = crawler.addPage(root, PathParser.parse("ErrorLogs"));
    request = new MockRequest();
    responder = new TestResponder();
    responder.setFastTest(true);
    context = new FitNesseContext(root);

    receiver = new FitSocketReceiver(0, context.socketDealer);
    context.port = receiver.receiveSocket();
  }

  @After
  public void tearDown() throws Exception {
    receiver.close();
  }

  @Test
  public void testSimpleRun() throws Exception {
    doSimpleRun(passFixtureTable());

    assertSubString(testPage.getName(), results);
    assertSubString("Test Results", results);
    assertSubString("class", results);
    assertNotSubString("ClassNotFoundException", results);
  }

  private void doSimpleRun(String fixtureTable) throws Exception {
    simpleRunPageName = "TestPage";
    testPage = crawler.addPage(root, PathParser.parse(simpleRunPageName), classpathWidgets() + fixtureTable);
    request.setResource(testPage.getName());

    response = responder.makeResponse(context, request);
    sender = new MockResponseSender();
    sender.doSending(response);

    results = sender.sentData();
  }

  @Test
  public void testEmptyTestPage() throws Exception {
    PageData data = root.getData();
    data.setContent(classpathWidgets());
    root.commit(data);
    testPage = crawler.addPage(root, PathParser.parse("EmptyTestPage"));
    request.setResource(testPage.getName());

    response = responder.makeResponse(context, request);
    sender = new MockResponseSender();
    sender.doSending(response);
    sender.sentData();

    WikiPagePath errorLogPath = PathParser.parse("ErrorLogs.EmptyTestPage");
    WikiPage errorLogPage = crawler.getPage(root, errorLogPath);
    String errorLogContent = errorLogPage.getData().getContent();
    assertNotSubString("Exception", errorLogContent);
  }

  @Test
  public void testFitSocketGetsClosed() throws Exception {
    doSimpleRun(passFixtureTable());
    assertTrue(receiver.socket.isClosed());
  }

  @Test
  public void testStandardOutput() throws Exception {
    responder.setFastTest(false);
    String content = classpathWidgets()
      + outputWritingTable("output1")
      + outputWritingTable("output2")
      + outputWritingTable("output3");

    String errorLogContent = doRunAndGetErrorLog(content);

    assertHasRegexp("output1", errorLogContent);
    assertHasRegexp("output2", errorLogContent);
    assertHasRegexp("output3", errorLogContent);
  }

  @Test
  public void testErrorOutput() throws Exception {
    responder.setFastTest(false);
    String content = classpathWidgets()
      + errorWritingTable("error1")
      + errorWritingTable("error2")
      + errorWritingTable("error3");

    String errorLogContent = doRunAndGetErrorLog(content);

    assertHasRegexp("error1", errorLogContent);
    assertHasRegexp("error2", errorLogContent);
    assertHasRegexp("error3", errorLogContent);
  }

  private String doRunAndGetErrorLog(String content) throws Exception {
    WikiPage testPage = crawler.addPage(root, PathParser.parse("TestPage"), content);
    request.setResource(testPage.getName());

    Response response = responder.makeResponse(context, request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    String results = sender.sentData();

    assertHasRegexp("ErrorLog", results);

    WikiPage errorLog = errorLogsParentPage.getChildPage(testPage.getName());
    return errorLog.getData().getContent();
  }

  @Test
  public void testHasExitValueHeader() throws Exception {
    WikiPage testPage = crawler.addPage(root, PathParser.parse("TestPage"), classpathWidgets() + passFixtureTable());
    request.setResource(testPage.getName());

    Response response = responder.makeResponse(context, request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    String results = sender.sentData();

    assertSubString("Exit-Code: 0", results);
  }

  @Test
  public void testFixtureThatCrashes() throws Exception {
    responder.setFastTest(false);
    WikiPage testPage = crawler.addPage(root, PathParser.parse("TestPage"), classpathWidgets() + crashFixtureTable());
    request.setResource(testPage.getName());

    Response response = responder.makeResponse(context, request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);

    String results = sender.sentData();
    assertSubString("ErrorLog", results);
  }

  @Test
  public void testResultsIncludeActions() throws Exception {
    doSimpleRun(passFixtureTable());
    assertSubString("<div class=\"actions\">", results);
  }

  @Test
  public void testResultsHaveHeaderAndFooter() throws Exception {
    crawler.addPage(root, PathParser.parse("PageHeader"), "HEADER");
    crawler.addPage(root, PathParser.parse("PageFooter"), "FOOTER");
    doSimpleRun(passFixtureTable());
    assertSubString("HEADER", results);
    assertSubString("FOOTER", results);
  }

  @Test
  public void testExecutionStatusAppears() throws Exception {
    doSimpleRun(passFixtureTable());
    assertHasRegexp("<div id=\"execution-status\">.*?</div>", results);
  }

  @Test
  public void xmlFormat() throws Exception {
    request.addInput("format", "xml");
    doSimpleRun(passFixtureTable());
    assertEquals("text/xml", response.getContentType());

    Document testResultsDocument = getXmlDocumentFromResults(results);
    Element testResultsElement = testResultsDocument.getDocumentElement();
    assertEquals("testResults", testResultsElement.getNodeName());
    String version = XmlUtil.getTextValue(testResultsElement, "FitNesseVersion");
    assertEquals(new FitNesseVersion().toString(), version);

    Element result = XmlUtil.getElementByTagName(testResultsElement, "result");
    Element counts = XmlUtil.getElementByTagName(result, "counts");
    assertCounts(counts, "1", "0", "0", "0");
    String content = XmlUtil.getTextValue(result, "content");
    assertSubString("PassFixture", content);
    String relativePageName = XmlUtil.getTextValue(result, "relativePageName");
    assertEquals("TestPage", relativePageName);
  }

  static Document getXmlDocumentFromResults(String results) throws Exception {
    String endOfXml = "</testResults>";
    String startOfXml = "<?xml";
    int xmlStartIndex = results.indexOf(startOfXml);
    int xmlEndIndex = results.indexOf(endOfXml) + endOfXml.length();
    String xmlString = results.substring(xmlStartIndex, xmlEndIndex);
    Document testResultsDocument = XmlUtil.newDocument(xmlString);
    return testResultsDocument;
  }

  static void assertCounts(Element counts, String right, String wrong, String ignores, String exceptions)
    throws Exception {
    assertEquals(right, XmlUtil.getTextValue(counts, "right"));
    assertEquals(wrong, XmlUtil.getTextValue(counts, "wrong"));
    assertEquals(ignores, XmlUtil.getTextValue(counts, "ignores"));
    assertEquals(exceptions, XmlUtil.getTextValue(counts, "exceptions"));
  }

  private String getExecutionStatusMessage() throws Exception {
    Pattern pattern = Pattern.compile("<div id=\"execution-status\">.*?<a href=\"ErrorLogs\\.[^\"]*\">([^<>]*?)</a>.*?</div>", Pattern.DOTALL);
    Matcher matcher = pattern.matcher(results);
    matcher.find();
    return matcher.group(1);
  }

  private String getExecutionStatusIconFilename() {
    Pattern pattern = Pattern.compile("<div id=\"execution-status\">.*?<img.*?src=\"(?:[^/]*/)*([^/]*\\.gif)\".*?/>.*?</div>", Pattern.DOTALL);
    Matcher matcher = pattern.matcher(results);
    matcher.find();
    return matcher.group(1);
  }

  @Test
  public void testExecutionStatusOk() throws Exception {
    doSimpleRun(passFixtureTable());
    assertEquals("Tests Executed OK", getExecutionStatusMessage());
    assertEquals("ok.gif", getExecutionStatusIconFilename());
  }

  @Test
  public void debugTest() throws Exception {
    responder.setFastTest(false);
    request.addInput("debug", "");
    doSimpleRun(passFixtureTable());
    assertEquals("Tests Executed OK", getExecutionStatusMessage());
    assertEquals("ok.gif", getExecutionStatusIconFilename());
    assertTrue("should be fast test", responder.isFastTest());
  }

  @Test
  public void testExecutionStatusOutputCaptured() throws Exception {
    responder.setFastTest(false);
    doSimpleRun(outputWritingTable("blah"));
    assertEquals("Output Captured", getExecutionStatusMessage());
    assertEquals("output.gif", getExecutionStatusIconFilename());
  }

  @Test
  public void testExecutionStatusError() throws Exception {
    responder.setFastTest(false);
    doSimpleRun(crashFixtureTable());
    assertEquals("Errors Occurred", getExecutionStatusMessage());
    assertEquals("error.gif", getExecutionStatusIconFilename());
  }

  @Test
  public void testExecutionStatusErrorHasPriority() throws Exception {
    responder.setFastTest(false);
    doSimpleRun(errorWritingTable("blah") + crashFixtureTable());
    assertEquals("Errors Occurred", getExecutionStatusMessage());
  }

  @Test
  public void testTestSummaryAppears() throws Exception {
    doSimpleRun(passFixtureTable());
    assertHasRegexp(divWithIdAndContent("test-summary", ".*?"), results);
  }

  @Test
  public void testTestSummaryInformationAppears() throws Exception {
    doSimpleRun(passFixtureTable());
    assertHasRegexp("<script>.*?document\\.getElementById\\(\"test-summary\"\\)\\.innerHTML = \".*?Assertions:.*?\";.*?</script>", results);
    assertHasRegexp("<script>.*?document\\.getElementById\\(\"test-summary\"\\)\\.className = \".*?\";.*?</script>", results);
  }

  @Test
  public void testTestSummaryHasRightClass() throws Exception {
    doSimpleRun(passFixtureTable());
    assertHasRegexp("<script>.*?document\\.getElementById\\(\"test-summary\"\\)\\.className = \"pass\";.*?</script>", results);
  }

  @Test
  public void testAuthentication_RequiresTestPermission() throws Exception {
    assertTrue(responder instanceof SecureResponder);
    SecureOperation operation = ((SecureResponder) responder).getSecureOperation();
    assertEquals(SecureTestOperation.class, operation.getClass());
  }

  @Test
  public void testNotifyListeners() throws Exception {
    MockTestEventListener listener1 = new MockTestEventListener();
    MockTestEventListener listener2 = new MockTestEventListener();

    TestResponder.registerListener(listener1);
    TestResponder.registerListener(listener2);

    doSimpleRun(passFixtureTable());

    assertEquals(true, listener1.gotPreTestNotification);
    assertEquals(true, listener2.gotPreTestNotification);
  }

  @Test
  public void testSuiteSetUpAndTearDownIsCalledIfSingleTestIsRun() throws Exception {
    responder.setFastTest(false);
    WikiPage suitePage = crawler.addPage(root, PathParser.parse("TestSuite"), classpathWidgets());
    WikiPage testPage = crawler.addPage(suitePage, PathParser.parse("TestPage"), outputWritingTable("Output of TestPage"));
    crawler.addPage(suitePage, PathParser.parse(SuiteContentsFinder.SUITE_SETUP_NAME), outputWritingTable("Output of SuiteSetUp"));
    crawler.addPage(suitePage, PathParser.parse(SuiteContentsFinder.SUITE_TEARDOWN_NAME), outputWritingTable("Output of SuiteTearDown"));

    WikiPagePath testPagePath = crawler.getFullPath(testPage);
    String resource = PathParser.render(testPagePath);
    request.setResource(resource);

    Response response = responder.makeResponse(context, request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    results = sender.sentData();

    assertEquals("Output Captured", getExecutionStatusMessage());
    assertHasRegexp("ErrorLog", results);

    WikiPage errorLog = crawler.getPage(errorLogsParentPage, testPagePath);
    String errorLogContent = errorLog.getData().getContent();
    assertHasRegexp("Output of SuiteSetUp", errorLogContent);
    assertHasRegexp("Output of TestPage", errorLogContent);
    assertHasRegexp("Output of SuiteTearDown", errorLogContent);
  }

  @Test
  public void testDoSimpleSlimTable() throws Exception {
    doSimpleRun(simpleSlimDecisionTable());
    assertHasRegexp("<td><span class=\"pass\">wow</span></td>", Utils.unescapeHTML(results));
  }

  private String errorWritingTable(String message) {
    return "\n|!-fitnesse.testutil.ErrorWritingFixture-!|\n" +
      "|" + message + "|\n\n";

  }

  private String outputWritingTable(String message) {
    return "\n|!-fitnesse.testutil.OutputWritingFixture-!|\n" +
      "|" + message + "|\n\n";
  }

  private String classpathWidgets() {
    return "!path classes\n";
  }

  private String crashFixtureTable() {
    return "|!-fitnesse.testutil.CrashFixture-!|\n";
  }

  private String passFixtureTable() {
    return "|!-fitnesse.testutil.PassFixture-!|\n";
  }

  private String simpleSlimDecisionTable() {
    return "!define TEST_SYSTEM {slim}\n" +
      "|!-DT:fitnesse.slim.test.TestSlim-!|\n" +
      "|string|get string arg?|\n" +
      "|wow|wow|\n";
  }

}
