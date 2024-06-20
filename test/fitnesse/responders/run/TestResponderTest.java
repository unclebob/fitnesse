// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.FitNesseVersion;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.authentication.SecureTestOperation;
import fitnesse.html.HtmlUtil;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.testHistory.ExecutionLogResponder;
import fitnesse.testsystems.TestSummary;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.util.Clock;
import fitnesse.util.DateAlteringClock;
import fitnesse.util.DateTimeUtil;
import fitnesse.util.XmlUtil;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
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
import java.util.Properties;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fitnesse.responders.run.TestResponderTest.XmlTestUtilities.assertCounts;
import static fitnesse.responders.run.TestResponderTest.XmlTestUtilities.getXmlDocumentFromResults;
import static fitnesse.util.XmlUtil.getElementByTagName;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static util.RegexTestCase.assertHasRegexp;
import static util.RegexTestCase.assertNotSubString;
import static util.RegexTestCase.assertSubString;
import static util.RegexTestCase.divWithIdAndContent;

public class TestResponderTest {
  private static final String TEST_TIME = "2008-12-05T13:19:00Z";
  private WikiPage root;
  private MockRequest request;
  private SuiteResponder responder;
  private FitNesseContext context;
  private Response response;
  private MockResponseSender sender;
  private WikiPage testPage;
  private String results;
  private File xmlResultsFile;
  private XmlChecker xmlChecker = new XmlChecker();
  private boolean debug;

  @Before
  public void setUp() throws Exception {
    debug = true;
    File testDir = new File("TestDir");
    testDir.mkdir();
    Properties properties = new Properties();
    context = FitNesseUtil.makeTestContext();
    root = context.getRootPage();
    request = new MockRequest();
    responder = new TestResponder();
    properties.setProperty("FITNESSE_PORT", String.valueOf(context.port));
    new DateAlteringClock(DateTimeUtil.getDateFromString(TEST_TIME), TimeZone.getTimeZone("CET")).advanceMillisOnEachQuery();
  }

  @After
  public void tearDown() throws Exception {
    FitNesseUtil.destroyTestContext();
    Clock.restoreDefaultClock();
  }

  @Test
  public void testIsValidHtml() throws Exception {
    doSimpleRun(passFixtureTable());

    assertSubString("<!DOCTYPE html>", results);
    assertSubString("</html>", results);

    //assertSubString("<base href=\"http://somehost.com:8080/\"", results);
    assertSubString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>", results);
    //assertSubString("Command Line Test Results", html);
  }

  @Test
  public void testHead() throws Exception {
    doSimpleRun(passFixtureTable());
    assertSubString("<div id=\"test-summary\"><div id=\"progressBar\">Preparing Tests ...</div></div>", results);
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
    doSimpleRunWithTags(fixtureTable, null);
  }

  private void doSimpleRunWithTags(String fixtureTable, String tags) throws Exception {
    if (debug) request.addInput("debug", "true");
    String simpleRunPageName = "TestPage";
    testPage = WikiPageUtil.addPage(root, PathParser.parse(simpleRunPageName), classpathWidgets() + fixtureTable);
    if (tags != null) {
      PageData pageData = testPage.getData();
      pageData.setAttribute(PageData.PropertySUITES, tags);
      testPage.commit(pageData);
    }
    request.addInput("requestParam", "2");
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
    testPage = WikiPageUtil.addPage(root, PathParser.parse("EmptyTestPage"), "");
    request.setResource(testPage.getName());

    response = responder.makeResponse(context, request);
    sender = new MockResponseSender();
    sender.doSending(response);
    sender.sentData();

    String errorLogContent = getExecutionLog();
    assertNotSubString("Exception", errorLogContent);
    assertSubString("No execution log available.", errorLogContent);
  }

  @Test
  public void testStandardOutput() throws Exception {
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
    WikiPage testPage = WikiPageUtil.addPage(root, PathParser.parse("TestPage"), content);
    request.setResource(testPage.getName());

    Response response = responder.makeResponse(context, request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    String results = sender.sentData();

    assertHasRegexp("\\?executionLog", results);

    return getExecutionLog();
  }

  private String getExecutionLog() throws Exception {
    return ((SimpleResponse) new ExecutionLogResponder().makeResponse(context, request)).getContent();
  }

  @Test
  public void testHasExitValueHeader() throws Exception {
    WikiPage testPage = WikiPageUtil.addPage(root, PathParser.parse("TestPage"), classpathWidgets() + passFixtureTable());
    request.setResource(testPage.getName());

    Response response = responder.makeResponse(context, request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    String results = sender.sentData();

    assertSubString("Exit-Code: 0", results);
  }

  @Test
  public void exitCodeIsCountOfErrors() throws Exception {
    doSimpleRun(failFixtureTable());
    assertSubString("Exit-Code: 1", results);
  }

  @Test
  public void pageHistoryLinkIsIncluded() throws Exception {
    responder.turnOffChunking();
    doSimpleRun(passFixtureTable());
    assertSubString("<a class=\"dropdown-item\" href=\"TestPage?pageHistory\"", results);
    assertSubString("Page History", results);
  }

  @Test
  public void testTestIdIsSentAsHeader() throws Exception {
    WikiPage testPage = WikiPageUtil.addPage(root, PathParser.parse("TestPage"), "");
    request.setResource(testPage.getName());

    Response response = responder.makeResponse(context, request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);

    String results = sender.sentData();
    assertSubString("X-FitNesse-Test-Id: ", results);
  }

  @Test
  public void testFixtureThatCrashes() throws Exception {
    WikiPage testPage = WikiPageUtil.addPage(root, PathParser.parse("TestPage"), classpathWidgets() + crashFixtureTable());
    request.setResource(testPage.getName());

    Response response = responder.makeResponse(context, request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);

    String results = sender.sentData();
    assertSubString("?executionLog", results);
  }

  @Test
  public void testResultsIncludeActions() throws Exception {
    doSimpleRun(passFixtureTable());
    assertSubString("<nav", results);
  }

  @Test
  public void testResultsDoHaveHeaderAndFooter() throws Exception {
    WikiPageUtil.addPage(root, PathParser.parse("PageHeader"), "HEADER");
    WikiPageUtil.addPage(root, PathParser.parse("PageFooter"), "FOOTER");
    doSimpleRun(passFixtureTable());
    assertSubString("HEADER", results);
    assertSubString("FOOTER", results);
  }

  @Test
  public void testExecutionLogLinkAppears() throws Exception {
    doSimpleRun(passFixtureTable());
    assertHasRegexp("class=\\\\\"ok\\\\\">Execution Log", results);
  }

  @Test
  public void simpleXmlFormat() throws Exception {
    responder.turnOffChunking();
    request.addInput("format", "xml");
    doSimpleRun(passFixtureTable());
    xmlChecker.assertFitPassFixtureXmlReportIsCorrect();
  }

  @Test
  public void noHistory_skipsHistoryFormatter() throws Exception{
    ensureXmlResultFileDoesNotExist(new TestSummary(2, 0, 0, 0));
    request.addInput("nohistory", "true");
    doSimpleRun(simpleSlimDecisionTable());
    assertFalse(xmlResultsFile.exists());
  }
  private String slimDecisionTable() {
    return "!define TEST_SYSTEM {slim}\n" +
      "|!-DT:fitnesse.slim.test.TestSlim-!|\n" +
      "|string|get string arg?|\n" +
      "|right|wrong|\n" +
      "|wow|wow|\n";
  }

  @Test
  public void slimXmlFormat() throws Exception {
    responder.turnOffChunking();
    request.addInput("format", "xml");
    ensureXmlResultFileDoesNotExist(new TestSummary(0, 1, 0, 0));
    doSimpleRunWithTags(slimDecisionTable(), "zoo");
    Document xmlFromFile = getXmlFromFileAndDeleteFile();
    xmlChecker.assertXmlReportOfSlimDecisionTableWithZooTagIsCorrect();
    xmlChecker.assertXmlHeaderIsCorrect(xmlFromFile);
    xmlChecker.assertXmlReportOfSlimDecisionTableWithZooTagIsCorrect();
  }

  @Test
  public void slimXmlFormatGivesErrorCountAsExitCode() throws Exception {
    request.addInput("format", "xml");
    ensureXmlResultFileDoesNotExist(new TestSummary(0, 1, 0, 0));
    doSimpleRunWithTags(slimDecisionTable(), "zoo");
    getXmlFromFileAndDeleteFile();
    assertSubString("Exit-Code: 1", results);
  }


  private void ensureXmlResultFileDoesNotExist(TestSummary counts) throws IOException {
    String resultsFileName = String.format("%s/TestPage/20081205141900_%d_%d_%d_%d.xml",
      context.getTestHistoryDirectory(), counts.getRight(), counts.getWrong(), counts.getIgnores(), counts.getExceptions());
    xmlResultsFile = new File(resultsFileName);

    if (xmlResultsFile.exists())
      FileUtil.deleteFile(xmlResultsFile);
  }

  private Document getXmlFromFileAndDeleteFile() throws Exception {
    assertTrue(xmlResultsFile.getAbsolutePath(), xmlResultsFile.exists());
    FileInputStream xmlResultsStream = new FileInputStream(xmlResultsFile);
    Document xmlDoc = XmlUtil.newDocument(xmlResultsStream);
    xmlResultsStream.close();
    FileUtil.deleteFile(xmlResultsFile);
    return xmlDoc;
  }

  @Test
  public void slimScenarioXmlFormat() throws Exception {
    responder.turnOffChunking();
    request.addInput("format", "xml");
    doSimpleRun(XmlChecker.SLIM_SCENARIO_TABLE);
    xmlChecker.assertXmlReportOfSlimScenarioTableIsCorrect();
  }

  @Test
  public void simpleTextFormatForPassingTest() throws Exception {
    request.addInput("format", "text");
    doSimpleRun(passFixtureTable());
    assertEquals("text/text", response.getContentType());
    assertTrue(results.contains("\n. "));
    assertTrue(results.contains("R:1    W:0    I:0    E:0    TestPage\t(TestPage)"));
    assertTrue(results.contains("1 Tests,\t0 Failures"));
  }

  @Test
  public void simpleTextFormatForFailingTest() throws Exception {
    request.addInput("format", "text");
    doSimpleRun(failFixtureTable());
    assertEquals("text/text", response.getContentType());
    assertTrue(results.contains("\nF "));
    assertTrue(results.contains("R:0    W:1    I:0    E:0    TestPage\t(TestPage)"));
    assertTrue(results.contains("1 Tests,\t1 Failures"));
  }

  @Test
  public void simpleTextFormatForErrorTest() throws Exception {
    request.addInput("format", "text");
    doSimpleRun(errorFixtureTable());
    assertEquals("text/text", response.getContentType());
    assertTrue(results.contains("\nX "));
    assertTrue(results.contains("R:0    W:0    I:0    E:1    TestPage\t(TestPage)"));
    assertTrue(results.contains("1 Tests,\t1 Failures"));
  }

  @Test
  public void testExecutionStatusOk() throws Exception {
    doSimpleRun(passFixtureTable());
    assertTrue(results.contains(">Execution Log<"));
    assertTrue(results.contains("\\\"ok\\\""));
  }

  @Test
  public void debugTest() throws Exception {
    request.addInput("debug", "");
    doSimpleRun(passFixtureTable());
    assertTrue(results.contains(">Execution Log<"));
    assertTrue(results.contains("\\\"ok\\\""));
    assertTrue("should be fast test", responder.isDebug());
  }

  @Test
  public void testExecutionStatusError() throws Exception {
    debug = false;
    doSimpleRun(crashFixtureTable());
    assertTrue(results.contains(">Execution Log<"));
    assertTrue(results.contains("\\\"error\\\""));
  }

  @Test
  public void testExecutionStatusErrorHasPriority() throws Exception {
    debug = false;
    doSimpleRun(errorWritingTable("blah") + crashFixtureTable());
    assertTrue(results.contains("class=\\\"error\\\">Execution Log<"));
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
  public void testTestHasStopped() throws Exception {

    String semaphoreName = "testTestHasStopped.semaphore";
    File semaphore = new File(semaphoreName);
    if (semaphore.exists())
      semaphore.delete();

    new Thread(new WaitForSemaphoreThenStopProcesses(semaphore)).start();

    doSimpleRun(createAndWaitFixture(semaphoreName));
    assertHasRegexp("Testing was interrupted", results);
    semaphore.delete();
  }

  private String createAndWaitFixture(String semaphoreName) {
    return "!define TEST_SYSTEM {slim}\n" +
      // Set a timeout, so the command can be processed async
      "!define slim.flags {-s 10 }\n" +
      "!|fitnesse.testutil.CreateFileAndWaitFixture|" + semaphoreName + "|\n";
  }

  private class WaitForSemaphoreThenStopProcesses implements Runnable {
    private File semaphore;

    public WaitForSemaphoreThenStopProcesses(File semaphore) {
      this.semaphore = semaphore;
    }

    @Override
    public void run() {
      waitForSemaphore();
      SuiteResponder.runningTestingTracker.stopAllProcesses();
    }

    private void waitForSemaphore() {
      try {
        int i = 1000;
        while (!semaphore.exists()) {
          if (--i <= 0)
            break;
          Thread.sleep(5);
        }
      } catch (InterruptedException e) {
      }
    }
  }


  @Test
  public void testAuthentication_RequiresTestPermission() throws Exception {
    assertTrue(responder instanceof SecureResponder);
    SecureOperation operation = responder.getSecureOperation();
    assertEquals(SecureTestOperation.class, operation.getClass());
  }

  @Test
  public void testSuiteSetUpAndTearDownIsCalledIfSingleTestIsRun() throws Exception {
    WikiPage suitePage = WikiPageUtil.addPage(root, PathParser.parse("TestSuite"), classpathWidgets());
    WikiPage testPage = WikiPageUtil.addPage(suitePage, PathParser.parse("TestPage"), outputWritingTable("Output of TestPage"));
    WikiPageUtil.addPage(suitePage, PathParser.parse(PageData.SUITE_SETUP_NAME), outputWritingTable("Output of SuiteSetUp"));
    WikiPageUtil.addPage(suitePage, PathParser.parse(PageData.SUITE_TEARDOWN_NAME), outputWritingTable("Output of SuiteTearDown"));

    PageData data = testPage.getData();
    WikiPageProperty properties = data.getProperties();
    properties.set(PageData.PropertySUITES, "Test Page tags");
    testPage.commit(data);

    WikiPagePath testPagePath = testPage.getFullPath();
    String resource = PathParser.render(testPagePath);
    request.setResource(resource);

    Response response = responder.makeResponse(context, request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    results = sender.sentData();

    assertTrue(results.contains(">Execution Log<"));
    assertHasRegexp("\\?executionLog", results);
    assertSubString("Test Page tags", results);

    String errorLogContent = getExecutionLog();
    assertHasRegexp("Output of SuiteSetUp", errorLogContent);
    assertHasRegexp("Output of TestPage", errorLogContent);
    assertHasRegexp("Output of SuiteTearDown", errorLogContent);
  }

  @Test
  public void testSuiteSetUpDoesNotIncludeSetUp() throws Exception {
    WikiPage suitePage = WikiPageUtil.addPage(root, PathParser.parse("TestSuite"), classpathWidgets());
    WikiPage testPage = WikiPageUtil.addPage(suitePage, PathParser.parse("TestPage"), outputWritingTable("Output of TestPage"));
    WikiPageUtil.addPage(suitePage, PathParser.parse(PageData.SUITE_SETUP_NAME), outputWritingTable("Output of SuiteSetUp"));
    WikiPageUtil.addPage(suitePage, PathParser.parse("SetUp"), outputWritingTable("Output of SetUp"));

    WikiPagePath testPagePath = testPage.getFullPath();
    String resource = PathParser.render(testPagePath);
    request.setResource(resource);

    Response response = responder.makeResponse(context, request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    results = sender.sentData();

    String errorLogContent = getExecutionLog();
    assertMessagesOccurInOrder(errorLogContent, "Output of SuiteSetUp", "Output of SetUp", "Output of TestPage");
    assertMessageHasJustOneOccurrenceOf(errorLogContent, "Output of SetUp");
  }

  @Test
  public void testSuiteTearDownDoesNotIncludeTearDown() throws Exception {
    WikiPage suitePage = WikiPageUtil.addPage(root, PathParser.parse("TestSuite"), classpathWidgets());
    WikiPage testPage = WikiPageUtil.addPage(suitePage, PathParser.parse("TestPage"), outputWritingTable("Output of TestPage"));
    WikiPageUtil.addPage(suitePage, PathParser.parse(PageData.SUITE_TEARDOWN_NAME), outputWritingTable("Output of SuiteTearDown"));
    WikiPageUtil.addPage(suitePage, PathParser.parse("TearDown"), outputWritingTable("Output of TearDown"));

    WikiPagePath testPagePath = testPage.getFullPath();
    String resource = PathParser.render(testPagePath);
    request.setResource(resource);

    Response response = responder.makeResponse(context, request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    results = sender.sentData();

    String errorLogContent = getExecutionLog();
    assertMessagesOccurInOrder(errorLogContent, "Output of TestPage", "Output of TearDown", "Output of SuiteTearDown");
    assertMessageHasJustOneOccurrenceOf(errorLogContent, "Output of TearDown");
  }

  @Test
  public void testSuiteSetUpAndSuiteTearDownWithSetUpAndTearDown() throws Exception {
    WikiPage suitePage = WikiPageUtil.addPage(root, PathParser.parse("TestSuite"), classpathWidgets());
    WikiPage testPage = WikiPageUtil.addPage(suitePage, PathParser.parse("TestPage"), outputWritingTable("Output of TestPage"));
    WikiPageUtil.addPage(suitePage, PathParser.parse(PageData.SUITE_SETUP_NAME), outputWritingTable("Output of SuiteSetUp"));
    WikiPageUtil.addPage(suitePage, PathParser.parse("SetUp"), outputWritingTable("Output of SetUp"));
    WikiPageUtil.addPage(suitePage, PathParser.parse(PageData.SUITE_TEARDOWN_NAME), outputWritingTable("Output of SuiteTearDown"));
    WikiPageUtil.addPage(suitePage, PathParser.parse("TearDown"), outputWritingTable("Output of TearDown"));

    WikiPagePath testPagePath = testPage.getFullPath();
    String resource = PathParser.render(testPagePath);
    request.setResource(resource);

    Response response = responder.makeResponse(context, request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    results = sender.sentData();

    String errorLogContent = getExecutionLog();
    assertMessagesOccurInOrder(errorLogContent, "Output of SuiteSetUp", "Output of SetUp", "Output of TestPage", "Output of TearDown", "Output of SuiteTearDown");
    assertMessageHasJustOneOccurrenceOf(errorLogContent, "Output of SetUp");
  }

  private void assertMessageHasJustOneOccurrenceOf(String output, String regexp) {
    Matcher match = Pattern.compile(regexp, Pattern.MULTILINE | Pattern.DOTALL).matcher(output);
    match.find();
    boolean found = match.find();
    if (found)
      fail("The regexp <" + regexp + "> was more than once in: " + output + ".");
  }

  private void assertMessagesOccurInOrder(String errorLogContent, String... messages) {
    int previousIndex = 0, currentIndex = 0;
    String previousMsg = "";
    for (String msg: messages) {
      currentIndex = errorLogContent.indexOf(msg);
      assertTrue(String.format("\"%s\" should occur not before \"%s\", but did in \"%s\"", msg, previousMsg, errorLogContent), currentIndex > previousIndex);
      previousIndex = currentIndex;
      previousMsg = msg;
    }
  }

  private String simpleSlimDecisionTable() {
    return "!define TEST_SYSTEM {slim}\n" +
      "|!-DT:fitnesse.slim.test.TestSlim-!|\n" +
      "|string|get string arg?|\n" +
      "|wow|wow|\n";
  }

  @Test
  public void checkHistoryForSimpleSlimTable() throws Exception {
    ensureXmlResultFileDoesNotExist(new TestSummary(1, 0, 0, 0));
    doSimpleRun(simpleSlimDecisionTable());
    Document xmlFromFile = getXmlFromFileAndDeleteFile();
    xmlChecker.assertXmlHeaderIsCorrect(xmlFromFile);
    assertHasRegexp("<td><span class=\"pass\">wow</span></td>", HtmlUtil.unescapeHTML(results));
  }

  @Test
  public void xmlFormatterShouldContainExecutionLog() throws Exception {
    WikiPage suitePage = WikiPageUtil.addPage(root, PathParser.parse("TestSuite"), classpathWidgets());
    WikiPage testPage = WikiPageUtil.addPage(suitePage, PathParser.parse("TestPage"), outputWritingTable("Output of TestPage"));

    WikiPagePath testPagePath = testPage.getFullPath();
    String resource = PathParser.render(testPagePath);
    request.setResource(resource);
    request.addInput("format", "xml");
    request.addInput("nochunk", "nochunk");

    Response response = responder.makeResponse(context, request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    results = sender.sentData();

    assertHasRegexp("<executionLog>", results);
    assertHasRegexp("<testSystem>fit:fit.FitServer</testSystem>", results);
    assertHasRegexp("<exitCode>0</exitCode>", results);
    assertHasRegexp("<stdOut>Output of TestPage.*</stdOut>", results);
    assertHasRegexp("<stdErr></stdErr>", results);
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

  private String failFixtureTable() {
    return "|!-fitnesse.testutil.FailFixture-!|\n";
  }

  private String errorFixtureTable() {
    return "|!-fitnesse.testutil.ErrorFixture-!|\n";
  }

  class XmlChecker {
    private Element testResultsElement;

    public void assertXmlHeaderIsCorrect(Document testResultsDocument) throws Exception {
      testResultsElement = testResultsDocument.getDocumentElement();
      assertEquals("testResults", testResultsElement.getNodeName());
      String version = XmlUtil.getTextValue(testResultsElement, "FitNesseVersion");
      assertEquals(new FitNesseVersion().toString(), version);
    }

    public void assertFitPassFixtureXmlReportIsCorrect() throws Exception {
      assertHeaderOfXmlDocumentsInResponseIsCorrect();

      Element result = getElementByTagName(testResultsElement, "result");
      Element counts = getElementByTagName(result, "counts");
      assertCounts(counts, "1", "0", "0", "0");

      String runTimeInMillis = XmlUtil.getTextValue(result, "runTimeInMillis");
      assertThat(Long.parseLong(runTimeInMillis), is(not(0L)));

      Element tags = getElementByTagName(result, "tags");
      assertNull(tags);

      String content = XmlUtil.getTextValue(result, "content");
      assertSubString("PassFixture", content);
      String relativePageName = XmlUtil.getTextValue(result, "relativePageName");
      assertEquals("TestPage", relativePageName);
    }

    public void assertXmlReportOfSlimDecisionTableWithZooTagIsCorrect() throws Exception {
      //String instructionContents[] = {"make", "table", "beginTable", "reset", "setString", "execute", "getStringArg", "reset", "setString", "execute", "getStringArg", "endTable"};
      //String instructionResults[] = {"OK", "EXCEPTION", "EXCEPTION", "EXCEPTION", "VOID", "VOID", "right", "EXCEPTION", "VOID", "VOID", "wow", "EXCEPTION"};
      String instructionContents[] = {"make", "setString", "getStringArg", "setString", "getStringArg"};
      String instructionResults[] = {"pass(DT:fitnesse.slim.test.TestSlim)", null, "fail(a=right;e=wrong)", null, "pass(wow)"};
      assertHeaderOfXmlDocumentsInResponseIsCorrect();

      Element result = getElementByTagName(testResultsElement, "result");
      Element counts = getElementByTagName(result, "counts");
      assertCounts(counts, "1", "1", "0", "0");

      String tags = XmlUtil.getTextValue(result, "tags");
      assertEquals("zoo", tags);

      Element instructions = getElementByTagName(result, "instructions");
      NodeList instructionList = instructions.getElementsByTagName("instructionResult");
      //assertEquals(instructionContents.length, instructionList.getLength());

      for (int i = 0; i < instructionContents.length; i++) {
        Element instructionElement = (Element) instructionList.item(i);
        assertInstructionHas(instructionElement, instructionContents[i]);
      }

      for (int i = 0; i < instructionResults.length; i++) {
        Element instructionElement = (Element) instructionList.item(i);
        assertResultHas(instructionElement, instructionResults[i]);
      }

      checkExpectation(instructionList, 0, "decisionTable_0_0", "0", "0", "pass", "ConstructionExpectation", null, null, "DT:fitnesse.slim.test.TestSlim");
      checkExpectation(instructionList, 4, "decisionTable_0_10", "1", "3", "pass", "ReturnedValueExpectation", null, null, "wow");
    }

    private static final String SLIM_SCENARIO_TABLE =
      "!define TEST_SYSTEM {slim}\n" +
        "\n" +
        "!|scenario|f|a|\n" +
        "|check|echo int|@a|@a|\n" +
        "\n" +
        "!|script|fitnesse.slim.test.TestSlim|\n" +
        "\n" +
        "!|f|\n" +
        "|a|\n" +
        "|1|\n" +
        "|${requestParam}|\n";

    public void assertXmlReportOfSlimScenarioTableIsCorrect() throws Exception {
      assertHeaderOfXmlDocumentsInResponseIsCorrect();

      Element result = getElementByTagName(testResultsElement, "result");
      Element counts = getElementByTagName(result, "counts");
      assertCounts(counts, "2", "0", "0", "0");

      String runTimeInMillis = XmlUtil.getTextValue(result, "runTimeInMillis");
      assertThat(Long.parseLong(runTimeInMillis), is(not(0L)));

      assertInstructionsOfSlimScenarioTableAreCorrect(result);
    }

    private void assertInstructionsOfSlimScenarioTableAreCorrect(Element result) throws Exception {

      Element instructions = getElementByTagName(result, "instructions");
      NodeList instructionList = instructions.getElementsByTagName("instructionResult");
      assertInstructionContentsOfSlimScenarioAreCorrect(instructionList);
      assertInstructionResultsOfSlimScenarioAreCorrect(instructionList);
      assertExpectationsOfSlimScenarioAreCorrect(instructionList);
    }

    private void assertExpectationsOfSlimScenarioAreCorrect(NodeList instructionList) throws Exception {
      checkExpectation(instructionList, 0, "scriptTable_1_0", "1", "0", "pass", "ConstructionExpectation", null, null, "fitnesse.slim.test.TestSlim");
      checkExpectation(instructionList, 1, "decisionTable_2_0/scriptTable_0_0", "3", "1", "pass", "ReturnedValueExpectation", null, null, "1");
      checkExpectation(instructionList, 2, "decisionTable_2_1/scriptTable_0_0", "3", "1", "pass", "ReturnedValueExpectation", null, null, "2");
    }

    private void assertInstructionResultsOfSlimScenarioAreCorrect(NodeList instructionList) throws Exception {
      String instructionResults[] = {"pass", "1", "2"};

      for (int i = 0; i < instructionResults.length; i++) {
        Element instructionElement = (Element) instructionList.item(i);
        assertResultHas(instructionElement, instructionResults[i]);
      }
    }

    private void assertInstructionContentsOfSlimScenarioAreCorrect(NodeList instructionList) throws Exception {
      String instructionContents[] = {"make", "call", "call"};
      assertEquals(instructionContents.length, instructionList.getLength());

      for (int i = 0; i < instructionContents.length; i++) {
        Element instructionElement = (Element) instructionList.item(i);
        assertInstructionHas(instructionElement, instructionContents[i]);
      }
    }

    private void checkExpectation(NodeList instructionList, int index, String id, String col, String row, String status, String type, String actual, String expected, String message) throws Exception {
      Element instructionElement = (Element) instructionList.item(index);
      Element expectation = getElementByTagName(instructionElement, "expectation");
      assertEquals(id, XmlUtil.getTextValue(expectation, "instructionId"));
      assertEquals(status, XmlUtil.getTextValue(expectation, "status"));
      assertEquals(type, XmlUtil.getTextValue(expectation, "type"));
      assertEquals(col, XmlUtil.getTextValue(expectation, "col"));
      assertEquals(row, XmlUtil.getTextValue(expectation, "row"));
      assertEquals(actual, XmlUtil.getTextValue(expectation, "actual"));
      assertEquals(expected, XmlUtil.getTextValue(expectation, "expected"));
      assertEquals(message, XmlUtil.getTextValue(expectation, "evaluationMessage"));
    }

    private void assertInstructionHas(Element instructionElement, String content) throws Exception {
      String instruction = XmlUtil.getTextValue(instructionElement, "instruction");
      assertTrue(String.format("instruction %s should contain: %s", instruction, content), instruction.contains(content));
    }

    private void assertResultHas(Element instructionElement, String content) throws Exception {
      String result = XmlUtil.getTextValue(instructionElement, "slimResult");
      assertTrue(String.format("result %s should contain: %s", result, content), (result == null && content == null) || result.contains(content));
    }

    private void assertHeaderOfXmlDocumentsInResponseIsCorrect() throws Exception {
      assertEquals("text/xml", response.getContentType());
      Document testResultsDocument = getXmlDocumentFromResults(results);
      xmlChecker.assertXmlHeaderIsCorrect(testResultsDocument);
    }
  }

  public static class XmlTestUtilities {
    public static Document getXmlDocumentFromResults(String results) throws Exception {
      String endOfXml = "</testResults>";
      String startOfXml = "<?xml";
      int xmlStartIndex = results.indexOf(startOfXml);
      int xmlEndIndex = results.indexOf(endOfXml) + endOfXml.length();
      String xmlString = results.substring(xmlStartIndex, xmlEndIndex);
      return XmlUtil.newDocument(xmlString);
    }

    public static void assertCounts(Element counts, String right, String wrong, String ignores, String exceptions)
      throws Exception {
      assertEquals(right, XmlUtil.getTextValue(counts, "right"));
      assertEquals(wrong, XmlUtil.getTextValue(counts, "wrong"));
      assertEquals(ignores, XmlUtil.getTextValue(counts, "ignores"));
      assertEquals(exceptions, XmlUtil.getTextValue(counts, "exceptions"));
    }
  }
  public static class JunitTestUtilities {
      public static Document getXmlDocumentFromResults(String results) throws Exception {
        String endOfXml = "</testsuite>";
        String startOfXml = "<?xml";
        int xmlStartIndex = results.indexOf(startOfXml);
        int xmlEndIndex = results.indexOf(endOfXml) + endOfXml.length();
        String xmlString = results.substring(xmlStartIndex, xmlEndIndex);
        return XmlUtil.newDocument(xmlString);
    }
  }
}
