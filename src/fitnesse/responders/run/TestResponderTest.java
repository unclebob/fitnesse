// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.FitNesseVersion;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.authentication.SecureTestOperation;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import static fitnesse.responders.run.TestResponderTest.XmlTestUtilities.assertCounts;
import static fitnesse.responders.run.TestResponderTest.XmlTestUtilities.getXmlDocumentFromResults;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.testutil.FitSocketReceiver;
import fitnesse.wiki.*;
import fitnesse.wikitext.Utils;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import util.FileUtil;
import static util.RegexTestCase.*;
import util.XmlUtil;
import static util.XmlUtil.getElementByTagName;

import java.io.File;
import java.io.FileInputStream;
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
  private File xmlResultsFile;
  private XmlChecker xmlChecker = new XmlChecker();

  @Before
  public void setUp() throws Exception {
    File testDir = new File("TestDir");
    testDir.mkdir();
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    errorLogsParentPage = crawler.addPage(root, PathParser.parse("ErrorLogs"));
    request = new MockRequest();
    responder = new TestResponder();
    responder.setFastTest(true);
    context = FitNesseUtil.makeTestContext(root);
    receiver = new FitSocketReceiver(0, context.socketDealer);
    context.port = receiver.receiveSocket();
  }

  @After
  public void tearDown() throws Exception {
    receiver.close();
    FitNesseUtil.destroyTestContext();
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
    String simpleRunPageName = "TestPage";
    testPage = crawler.addPage(root, PathParser.parse(simpleRunPageName), classpathWidgets() + fixtureTable);
    if (tags != null) {
      PageData pageData = testPage.getData();
      pageData.setAttribute(PageData.PropertySUITES, tags);
      testPage.commit(pageData);
    }
    request.setResource(testPage.getName());

    responder.turnOffChunkingForTests();
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
  public void simpleXmlFormat() throws Exception {
    request.addInput("format", "xml");
    doSimpleRun(passFixtureTable());
    xmlChecker.assertFitPassFixtureXmlReportIsCorrect();
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
    request.addInput("format", "xml");
    ensureXmlResultFileDoesNotExist(new TestSummary(2, 1, 0, 0));
    doSimpleRunWithTags(slimDecisionTable(), "zoo");
    Document xmlFromFile = getXmlFromFileAndDeleteFile();
    xmlChecker.assertXmlReportOfSlimDecisionTableWithZooTagIsCorrect();
    xmlChecker.assertXmlHeaderIsCorrect(xmlFromFile);
    xmlChecker.assertXmlReportOfSlimDecisionTableWithZooTagIsCorrect();
  }


  private void ensureXmlResultFileDoesNotExist(TestSummary counts) {
    XmlFormatter.setTestTime("12/5/2008 01:19:00");
    String resultsFileName = String.format("%s/TestPage/20081205011900_%d_%d_%d_%d.xml",
      context.getTestHistoryDirectory(), counts.getRight(), counts.getWrong(), counts.getIgnores(), counts.getExceptions());
    xmlResultsFile = new File(resultsFileName);

    if (xmlResultsFile.exists())
      FileUtil.deleteFile(xmlResultsFile);
  }

  private Document getXmlFromFileAndDeleteFile() throws Exception {
    assertTrue(xmlResultsFile.exists());
    FileInputStream xmlResultsStream = new FileInputStream(xmlResultsFile);
    Document xmlDoc = XmlUtil.newDocument(xmlResultsStream);
    xmlResultsStream.close();
    FileUtil.deleteFile(xmlResultsFile);
    return xmlDoc;
  }

  @Test
  public void slimScenarioXmlFormat() throws Exception {
    request.addInput("format", "xml");
    doSimpleRun(XmlChecker.slimScenarioTable);
    xmlChecker.assertXmlReportOfSlimScenarioTableIsCorrect();
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
  public void testTestHasStopped() throws Exception {

    String semaphoreName = "testTestHasStopped.semaphore";
    File semaphore = new File(semaphoreName);
    if (semaphore.exists())
      semaphore.delete();

    new Thread(makeStopTestsRunnable(semaphore)).start();

    doSimpleRun(createAndWaitFixture(semaphoreName));
    assertHasRegexp("Testing was interupted", results);
    semaphore.delete();
  }

  private String createAndWaitFixture(String semaphoreName) {
    return "!define TEST_SYSTEM {slim}\n" +
      "!|fitnesse.testutil.CreateFileAndWaitFixture|" + semaphoreName + "|\n";
  }

  private Runnable makeStopTestsRunnable(File semaphore) {
    return new WaitForSemaphoreThenStopProcesses(semaphore);
  }

  private class WaitForSemaphoreThenStopProcesses implements Runnable {
    private File semaphore;

    public WaitForSemaphoreThenStopProcesses(File semaphore) {
      this.semaphore = semaphore;
    }

    public void run() {
      waitForSemaphore();
      context.runningTestingTracker.stopAllProcesses();
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


  private String simpleSlimDecisionTable() {
    return "!define TEST_SYSTEM {slim}\n" +
      "|!-DT:fitnesse.slim.test.TestSlim-!|\n" +
      "|string|get string arg?|\n" +
      "|wow|wow|\n";
  }

  @Test
  public void checkHistoryForSimpleSlimTable() throws Exception {
    ensureXmlResultFileDoesNotExist(new TestSummary(2, 0, 0, 0));
    doSimpleRun(simpleSlimDecisionTable());
    Document xmlFromFile = getXmlFromFileAndDeleteFile();
    xmlChecker.assertXmlHeaderIsCorrect(xmlFromFile);
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

      Element tags = getElementByTagName(result, "tags");
      assertNull(tags);

      String content = XmlUtil.getTextValue(result, "content");
      assertSubString("PassFixture", content);
      String relativePageName = XmlUtil.getTextValue(result, "relativePageName");
      assertEquals("TestPage", relativePageName);
    }

    public void assertXmlReportOfSlimDecisionTableWithZooTagIsCorrect() throws Exception {
      String instructionContents[] = {"make", "table", "reset", "setString", "execute", "getStringArg", "reset", "setString", "execute", "getStringArg"};
      String instructionResults[] = {"OK", "EXCEPTION", "EXCEPTION", "VOID", "VOID", "right", "EXCEPTION", "VOID", "VOID", "wow"};
      assertHeaderOfXmlDocumentsInResponseIsCorrect();

      Element result = getElementByTagName(testResultsElement, "result");
      Element counts = getElementByTagName(result, "counts");
      assertCounts(counts, "2", "1", "0", "0");

      String tags = XmlUtil.getTextValue(result, "tags");
      assertEquals("zoo", tags);

      Element tables = getElementByTagName(result, "tables");
      assertNotNull(tables);
      NodeList tableList = tables.getElementsByTagName("table");
      assertNotNull(tableList);
      assertEquals(1, tableList.getLength());
      Element tableElement = (Element) tableList.item(0);
      String tableName = XmlUtil.getTextValue(tableElement, "name");
      assertNotNull(tableName);
      assertEquals("decisionTable_0", tableName);
      assertEquals("[[pass(DT:fitnesse.slim.test.TestSlim)],[string,get string arg?],[right,[right] fail(expected [wrong])],[wow,pass(wow)]]", tableElementToString(tableElement));

      Element instructions = getElementByTagName(result, "instructions");
      NodeList instructionList = instructions.getElementsByTagName("instructionResult");
      assertEquals(instructionContents.length, instructionList.getLength());

      for (int i = 0; i < instructionContents.length; i++) {
        Element instructionElement = (Element) instructionList.item(i);
        assertInstructionHas(instructionElement, instructionContents[i]);
      }

      for (int i = 0; i < instructionResults.length; i++) {
        Element instructionElement = (Element) instructionList.item(i);
        assertResultHas(instructionElement, instructionResults[i]);
      }

      checkExpectation(instructionList, 0, "decisionTable_0_0", "0", "0", "right", "ConstructionExpectation", "OK", "DT:fitnesse.slim.test.TestSlim", "pass(DT:fitnesse.slim.test.TestSlim)");
      checkExpectation(instructionList, 3, "decisionTable_0_3", "0", "2", "ignored", "VoidReturnExpectation", "/__VOID__/", "right", "right");
      checkExpectation(instructionList, 5, "decisionTable_0_5", "1", "2", "wrong", "ReturnedValueExpectation", "right", "wrong", "[right] fail(expected [wrong])");
      checkExpectation(instructionList, 7, "decisionTable_0_7", "0", "3", "ignored", "VoidReturnExpectation", "/__VOID__/", "wow", "wow");
      checkExpectation(instructionList, 9, "decisionTable_0_9", "1", "3", "right", "ReturnedValueExpectation", "wow", "wow", "pass(wow)");
    }

    private String tableElementToString(Element tableElement) {
      StringBuilder result = new StringBuilder();
      result.append("[");
      rowsToString(tableElement, result);
      result.append("]");
      return result.toString();
    }

    private void rowsToString(Element tableElement, StringBuilder result) {
      NodeList rows = tableElement.getElementsByTagName("row");
      for (int row = 0; row < rows.getLength(); row++) {
        result.append("[");
        Element rowElement = (Element) rows.item(row);
        colsToString(result, rowElement);
        result.append("],");
      }
      result.deleteCharAt(result.length() - 1);
    }

    private void colsToString(StringBuilder result, Element rowElement) {
      NodeList cols = rowElement.getElementsByTagName("col");
      for (int col = 0; col < cols.getLength(); col++) {
        Element colElement = (Element) cols.item(col);
        result.append(colElement.getFirstChild().getNodeValue());
        result.append(",");
      }
      result.deleteCharAt(result.length() - 1);
    }

    public final static String slimScenarioTable =
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
        "|2|\n";

    public void assertXmlReportOfSlimScenarioTableIsCorrect() throws Exception {
      assertHeaderOfXmlDocumentsInResponseIsCorrect();

      Element result = getElementByTagName(testResultsElement, "result");
      Element counts = getElementByTagName(result, "counts");
      assertCounts(counts, "3", "0", "0", "0");

      assertTablesInSlimScenarioAreCorrect(result);
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
      checkExpectation(instructionList, 0, "scriptTable_1_0", "1", "0", "right", "ConstructionExpectation", "OK", "fitnesse.slim.test.TestSlim", "pass(fitnesse.slim.test.TestSlim)");
      checkExpectation(instructionList, 1, "decisionTable_2_0/scriptTable_0_0", "3", "1", "right", "ReturnedValueExpectation", "1", "1", "pass(1)");
      checkExpectation(instructionList, 2, "decisionTable_2_1/scriptTable_0_0", "3", "1", "right", "ReturnedValueExpectation", "2", "2", "pass(2)");
    }

    private void assertInstructionResultsOfSlimScenarioAreCorrect(NodeList instructionList) throws Exception {
      String instructionResults[] = {"OK", "1", "2"};

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

    private void assertTablesInSlimScenarioAreCorrect(Element result) throws Exception {
      Element tables = getElementByTagName(result, "tables");
      NodeList tableList = tables.getElementsByTagName("table");
      assertEquals(5, tableList.getLength());

      String tableNames[] = {"scenarioTable_0", "scriptTable_1", "decisionTable_2", "decisionTable_2_0/scriptTable_0", "decisionTable_2_1/scriptTable_0"};
      String tableValues[][][] = {
        {
          {"scenario", "f", "a"},
          {"check", "echo int", "@a", "@a"}
        },
        {
          {"script", "pass(fitnesse.slim.test.TestSlim)"}
        },
        {
          {"f"},
          {"a"},
          {"1", "pass(scenario:decisionTable_2_0/scriptTable_0)"},
          {"2", "pass(scenario:decisionTable_2_1/scriptTable_0)"}
        },
        {
          {"scenario", "f", "a"},
          {"check", "echo int", "1", "pass(1)"}
        },
        {
          {"scenario", "f", "a"},
          {"check", "echo int", "2", "pass(2)"}
        }
      };

      for (int tableIndex = 0; tableIndex < tableList.getLength(); tableIndex++) {
        assertEquals(tableNames[tableIndex], XmlUtil.getTextValue((Element) tableList.item(tableIndex), "name"));

        Element tableElement = (Element) tableList.item(tableIndex);
        NodeList rowList = tableElement.getElementsByTagName("row");
        for (int rowIndex = 0; rowIndex < rowList.getLength(); rowIndex++) {
          NodeList colList = ((Element) rowList.item(rowIndex)).getElementsByTagName("col");
          for (int colIndex = 0; colIndex < colList.getLength(); colIndex++)
            assertEquals(tableValues[tableIndex][rowIndex][colIndex], XmlUtil.getElementText((Element) colList.item(colIndex)));
        }
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
      assertTrue(String.format("instruction %s should contain: %s", instruction, content), instruction.indexOf(content) != -1);
    }

    private void assertResultHas(Element instructionElement, String content) throws Exception {
      String result = XmlUtil.getTextValue(instructionElement, "slimResult");
      assertTrue(String.format("result %s should contain: %s", result, content), result.indexOf(content) != -1);
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
}
