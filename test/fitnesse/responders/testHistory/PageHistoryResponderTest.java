package fitnesse.responders.testHistory;

import fitnesse.FitNesseContext;
import fitnesse.FitNesseVersion;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.reporting.history.PageHistory;
import fitnesse.reporting.history.SuiteExecutionReport;
import fitnesse.reporting.history.TestExecutionReport;
import fitnesse.reporting.history.TestHistory;
import fitnesse.testsystems.TestSummary;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.util.DateTimeUtil;
import fitnesse.util.TimeMeasurement;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;
import util.RegexTestCase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.SortedSet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static util.RegexTestCase.assertHasRegexp;
import static util.RegexTestCase.assertSubString;

public class PageHistoryResponderTest {
  private File resultsDirectory;
  private SimpleDateFormat dateFormat = PageHistory.getDateFormat();
  private PageHistoryResponder responder;
  private SimpleResponse response;
  private MockRequest request;
  private FitNesseContext context;
  private FitNesseVersion fitNesseVersion = new FitNesseVersion();

  @Before
  public void setup() throws IOException {
    resultsDirectory = new File("testHistoryDirectory");
    removeResultsDirectory();
    resultsDirectory.mkdir();
    responder = new PageHistoryResponder();
    Properties properties = new Properties();
    properties.setProperty("test.history.path", resultsDirectory.getPath());
    context = FitNesseUtil.makeTestContext(properties);
  }

  @After
  public void teardown() throws IOException {
    removeResultsDirectory();
  }

  private void makeResponse() throws Exception {
    request = new MockRequest();
    request.setResource("TestPage");
    response = (SimpleResponse) responder.makeResponse(context, request);
  }

  private void removeResultsDirectory() throws IOException {
    if (resultsDirectory.exists())
      FileUtil.deleteFileSystemDirectory(resultsDirectory);
  }

  private File addPageDirectory(String pageName) {
    File pageDirectory = new File(resultsDirectory, pageName);
    pageDirectory.mkdir();
    return pageDirectory;
  }

  private File addTestResult(File pageDirectory, String testResultFileName) throws IOException {
    File testResultFile = new File(pageDirectory, testResultFileName + ".xml");
    testResultFile.createNewFile();
    return testResultFile;
  }

  @Test
  public void shouldReportNoHistoryIfNoPageDirectory() throws Exception {
    makeResponse();
    assertHasRegexp("No history for page: TestPage", response.getContent());
  }

  @Test
  public void shouldReportNoHistoryIfNoHistoryInPageDirectory() throws Exception {
    addPageDirectory("TestPage");
    makeResponse();
    assertHasRegexp("No history for page: TestPage", response.getContent());
  }

  @Test
  public void pageHistoryShouldHaveStatsForOneTestIfOnePageHistoryFileIsPresent() throws Exception {
    File pageDirectory = addPageDirectory("TestPage");
    addTestResult(pageDirectory, "20090418123103_1_2_3_4");

    TestHistory history = new TestHistory(resultsDirectory);
    PageHistory pageHistory = history.getPageHistory("TestPage");
    assertEquals(1, pageHistory.size());
    assertEquals(7, pageHistory.maxAssertions());
    SortedSet<Date> dates = pageHistory.datesInChronologicalOrder();
    assertEquals(1, dates.size());
    Date date = dateFormat.parse("20090418123103");
    assertEquals(date, dates.first());
    PageHistory.PassFailBar passFailBar = pageHistory.getPassFailBar(date, 50);
    assertEquals(1, passFailBar.getPass());
    assertEquals(6, passFailBar.getFail());
    assertEquals(7, passFailBar.getPassUnits());
    assertEquals(43, passFailBar.getFailUnits());
  }


  @Test
  public void pageHistoryShouldHaveStatsForTwoTestsIfTwoPageHistoryFilesArePresent() throws Exception {
    File pageDirectory = addPageDirectory("TestPage");
    addTestResult(pageDirectory, "20090503110451_6_5_3_1");
    addTestResult(pageDirectory, "20090418123103_1_2_3_4");

    TestHistory history = new TestHistory(resultsDirectory);
    PageHistory pageHistory = history.getPageHistory("TestPage");
    assertEquals(2, pageHistory.size());
    assertEquals(12, pageHistory.maxAssertions());
    SortedSet<Date> dates = pageHistory.datesInChronologicalOrder();
    assertEquals(2, dates.size());
    Date date1 = dateFormat.parse("20090418123103");
    Date date2 = dateFormat.parse("20090503110451");
    Date[] dateArray = dates.toArray(new Date[dates.size()]);
    assertEquals(date1, dateArray[1]);
    assertEquals(date2, dateArray[0]);
    PageHistory.PassFailBar passFailBar = pageHistory.getPassFailBar(date1, 50);
    assertEquals(1, passFailBar.getPass());
    assertEquals(6, passFailBar.getFail());
    assertEquals(4, passFailBar.getPassUnits());
    assertEquals(25, passFailBar.getFailUnits());
    passFailBar = pageHistory.getPassFailBar(date2, 50);
    assertEquals(6, passFailBar.getPass());
    assertEquals(6, passFailBar.getFail());
    assertEquals(25, passFailBar.getPassUnits());
    assertEquals(25, passFailBar.getFailUnits());
  }

  @Test
  public void evenOneFailureShouldCountInTheFailUnit() throws Exception {
    PageHistory.PassFailBar passFailBar = computePassFailBarFor(1000, 1, 0, 0);
    assertEquals(49, passFailBar.getPassUnits());
    assertEquals(1, passFailBar.getFailUnits());
  }

  @Test
  public void exactMultiplesShouldWork() throws Exception {
    PageHistory.PassFailBar passFailBar = computePassFailBarFor(48, 2, 0, 0);
    assertEquals(48, passFailBar.getPassUnits());
    assertEquals(2, passFailBar.getFailUnits());
  }

  @Test
  public void AllRedIfFractionOfOneUnitPasses() throws Exception {
    PageHistory.PassFailBar passFailBar = computePassFailBarFor(1, 1000, 0, 0);
    assertEquals(0, passFailBar.getPassUnits());
    assertEquals(50, passFailBar.getFailUnits());
  }

  private PageHistory.PassFailBar computePassFailBarFor(int right, int wrong, int ignores, int exceptions) throws IOException, ParseException {
    File pageDirectory = addPageDirectory("TestPage");
    addTestResult(pageDirectory, String.format("20090503110451_%d_%d_%d_%d", right, wrong, ignores, exceptions));
    TestHistory history = new TestHistory(resultsDirectory);
    PageHistory pageHistory = history.getPageHistory("TestPage");
    Date date = dateFormat.parse("20090503110451");
    PageHistory.PassFailBar passFailBar = pageHistory.getPassFailBar(date, 50);
    return passFailBar;
  }


  @Test
  public void shouldNotReportNoHistoryIfHistoryIsPresent() throws Exception {
    addPageDirectory("TestPage");
    File pageDirectory = addPageDirectory("TestPage");
    addTestResult(pageDirectory, "20090503110455_6_5_3_1");

    makeResponse();
    RegexTestCase.assertDoesntHaveRegexp("No history for page: TestPage", response.getContent());
  }

  @Test
  public void singlePassingResultShouldHaveTableandHeaderRow() throws Exception {
    addPageDirectory("TestPage");
    File pageDirectory = addPageDirectory("TestPage");
    addTestResult(pageDirectory, "20090503110451_6_0_3_0");

    makeResponse();
    String content = response.getContent();
    content = content.replace("\n", " ");
    content = content.replace("\r", " ");
    assertHasRegexp("<table>.*<tr>.*<th>Time</th>.*<th>Pass</th>.*<th>Fail</th>.*<th colspan=\"50\">0..6</th>.*<tr", content);
    assertHasRegexp("</tr>.*</table>", content);

  }

  @Test
  public void singlePassingResultsShouldBeGreen() throws Exception {
    addPageDirectory("TestPage");
    File pageDirectory = addPageDirectory("TestPage");
    addTestResult(pageDirectory, "20090503110451_6_0_3_0");

    makeResponse();
    assertHasRegexp("<td class=\"date_field pass\">.*03 May, 09 11:04.*</td>", response.getContent());
  }

  @Test
  public void singleFailingResultsShouldBeRed() throws Exception {
    addPageDirectory("TestPage");
    File pageDirectory = addPageDirectory("TestPage");
    addTestResult(pageDirectory, "20090503110451_6_1_3_0");

    makeResponse();
    assertHasRegexp("<td class=\"date_field fail\">.*03 May, 09 11:04.*</td>", response.getContent());
  }

  @Test
  public void singleResultShouldShowPassFailStatistics() throws Exception {
    addPageDirectory("TestPage");
    File pageDirectory = addPageDirectory("TestPage");
    addTestResult(pageDirectory, "20090503110451_6_1_3_1");

    makeResponse();
    assertHasRegexp("<td class=\"fail_count fail\">2</td>", response.getContent());
    assertHasRegexp("<td class=\"pass_count pass\">6</td>", response.getContent());
  }

  @Test
  public void singleResultWithNoPassesOrFailuresShouldShowPassFailStatisticsInGrey() throws Exception {
    addPageDirectory("TestPage");
    File pageDirectory = addPageDirectory("TestPage");
    addTestResult(pageDirectory, "20090503110451_0_0_3_0");

    makeResponse();
    assertHasRegexp("<td class=\"pass_count ignore\">0</td>", response.getContent());
    assertHasRegexp("<td class=\"fail_count ignore\">0</td>", response.getContent());
  }

  @Test
  public void singleResultShouldShowPassFailBar() throws Exception {
    addPageDirectory("TestPage");
    File pageDirectory = addPageDirectory("TestPage");
    addTestResult(pageDirectory, "20090503110451_30_20_3_0");
    makeResponse();
    StringBuilder expected = new StringBuilder();
    for (int i = 0; i < 30; i++) {
      expected.append("<td class=\"element pass\">&nbsp;</td>");
    }
    expected.append(".*");
    for (int i = 0; i < 20; i++) {
      expected.append("<td class=\"element fail\">&nbsp;</td>");
    }
    assertHasRegexp(expected.toString(), response.getContent());
  }

  @Test
  public void shortResultShouldShowPassFailBarWithPadding() throws Exception {
    File pageDirectory = addPageDirectory("TestPage");
    addTestResult(pageDirectory, "20090503110451_30_20_3_0");
    addTestResult(pageDirectory, "20090503143157_10_5_3_0");

    makeResponse();
    StringBuilder expected = new StringBuilder();
    for (int i = 0; i < 10; i++) {
      expected.append("<td class=\"element pass\">&nbsp;</td>");
    }
    expected.append(".*");
    for (int i = 0; i < 5; i++) {
      expected.append("<td class=\"element fail\">&nbsp;</td>");
    }
    expected.append(".*");
    for (int i = 0; i < 35; i++) {
      expected.append("<td class=\"element ignore\">&nbsp;</td>");
    }
    assertHasRegexp(expected.toString(), response.getContent());
  }

  @Test
  public void canGetTestExecutionReport() throws Exception {
    File pageDirectory = addPageDirectory("TestPage");
    File resultFile = new File(pageDirectory, "20090503110451_30_20_3_0.xml");
    addDummyTestResult(resultFile);
    makeResultForDate("TestPage", "20090503110451");
    assertHasRegexp(fitNesseVersion.toString(), response.getContent());
    assertHasRegexp("relativePageName", response.getContent());
    assertHasRegexp("11 right", response.getContent());
    assertHasRegexp("22 wrong", response.getContent());
    assertHasRegexp("33 ignored", response.getContent());
    assertHasRegexp("44 exceptions", response.getContent());
    assertHasRegexp("99 ms", response.getContent());
    assertHasRegexp("wad of HTML content after control character", response.getContent());
  }

  @Test
  public void canGetSuiteExecutionReport() throws Exception {
    File pageDirectory = addPageDirectory("SuitePage");
    File resultFile = new File(pageDirectory, "19801205012000_30_20_3_0.xml");
    addDummySuiteResult(resultFile);
    makeResultForDate("SuitePage", "19801205012000");
    assertSubString(fitNesseVersion.toString(), response.getContent());
    assertSubString("SuitePage.TestPageOne?pageHistory&resultDate=19801205012000", response.getContent());
    assertHasRegexp("(12321 ms)", response.getContent());
  }

  @Test
  public void canGetLatestWhenOnlyOneTestResultExists() throws Exception {
    File pageDirectory = addPageDirectory("TestPage");
    addDummyTestResult(new File(pageDirectory, "19801205012000_30_20_3_0"));

    makeResultForDate("TestPage", "latest");
    assertHasRegexp("Fri Dec 05 01:20:00 [A-Z0-9:+]+ 1980", response.getContent());
  }

  @Test
  public void canGetLatestWhenManyTestResultsExists() throws Exception {
    File pageDirectory = addPageDirectory("TestPage");
    addDummyTestResult(new File(pageDirectory, "19801205012000_30_20_3_0"));
    addDummyTestResult(new File(pageDirectory, "19901205012000_30_20_3_0"));
    addDummyTestResult(new File(pageDirectory, "19951205012000_30_20_3_0"));
    addDummyTestResult(new File(pageDirectory, "19941205012000_30_20_3_0"));

    makeResultForDate("TestPage", "latest");
    assertHasRegexp("Tue Dec 05 01:20:00 [A-Z0-9:+]+ 1995", response.getContent());
  }

  private void addDummySuiteResult(File resultFile) throws Exception {
    SuiteExecutionReport report = new SuiteExecutionReport(fitNesseVersion, "SuitePage");
    report.date = DateTimeUtil.getDateFromString("12/5/1980 01:19:00");
    report.getFinalCounts().add(new TestSummary(4,5,6,7));
    TimeMeasurement timeMeasurement = mock(TimeMeasurement.class);
    when (timeMeasurement.elapsed()).thenReturn(12321L);
    report.setTotalRunTimeInMillis(timeMeasurement);
    long time = DateTimeUtil.getTimeFromString("12/5/1980 01:20:00");
    SuiteExecutionReport.PageHistoryReference r1 = new SuiteExecutionReport.PageHistoryReference("SuitePage.TestPageOne", time, 9);
    SuiteExecutionReport.PageHistoryReference r2 = new SuiteExecutionReport.PageHistoryReference("SuitePage.TestPageTwo", time, 11);

    report.addPageHistoryReference(r1);
    report.addPageHistoryReference(r2);

    generateSuiteResultFile(report, resultFile);
  }

  private void generateSuiteResultFile(SuiteExecutionReport report, File resultFile) throws Exception {
    VelocityContext velocityContext = new VelocityContext();
    velocityContext.put("suiteExecutionReport", report);
    Template template = context.pageFactory.getVelocityEngine().getTemplate("suiteHistoryXML.vm");
    FileWriter fileWriter = new FileWriter(resultFile);
    template.merge(velocityContext, fileWriter);
    fileWriter.close();
  }

  private void makeResultForDate(String page, String resultDate) throws Exception {
    request = new MockRequest();
    request.setResource(page);
    request.addInput("resultDate", resultDate);
    response = (SimpleResponse) responder.makeResponse(context, request);
  }

  private void addDummyTestResult(File resultFile) throws Exception {
    TestExecutionReport testResponse = makeDummyTestResponse();
    generateTestResultFile(testResponse, resultFile);
  }

  private void generateTestResultFile(TestExecutionReport testResponse, File resultFile) throws Exception {
    VelocityContext velocityContext = new VelocityContext();
    velocityContext.put("response", testResponse);
    Template template = context.pageFactory.getVelocityEngine().getTemplate("testResults.vm");
    FileWriter fileWriter = new FileWriter(resultFile);
    template.merge(velocityContext, fileWriter);
    fileWriter.close();
  }

  private TestExecutionReport makeDummyTestResponse() {
    TestExecutionReport testResponse = new TestExecutionReport(fitNesseVersion, "rootPath");
    testResponse.getFinalCounts().add(new TestSummary(1, 2, 3, 4));
    TestExecutionReport.TestResult result = new TestExecutionReport.TestResult();
    testResponse.addResult(result);
    result.right = "11";
    result.wrong = "22";
    result.ignores = "33";
    result.exceptions = "44";
    result.relativePageName = "relativePageName";
    result.content = "wad of HTML content\u001B after control character";
    result.dateString = "2015-07-03T12:56:57+00:00";
    result.runTimeInMillis = "99";
    return testResponse;
  }

  @Test
  public void shouldStillGenerateReponseAndBarWhenThereIsAnInvalidFileNameInList() throws Exception {
    File pageDirectory = addPageDirectory("TestPage");
    addTestResult(pageDirectory, "bad_File_name");
    addTestResult(pageDirectory, "20090418123103_1_2_3_4");

    TestHistory history = new TestHistory(resultsDirectory);
    PageHistory pageHistory = history.getPageHistory("TestPage");
    assertEquals(1, pageHistory.size());
  }

  @Test
  public void shouldStillMakeResponseWithCorruptTestResultFile() throws Exception {
    File pageDirectory = addPageDirectory("TestPage");
    File resultFile = new File(pageDirectory, "20090503110451_30_20_3_0");
    addBadDummyTestResult(resultFile);
    makeResultForDate("TestPage", "20090503110451");
    assertHasRegexp("Corrupt Test Result File", response.getContent());
  }

  @Test
  public void shouldBeAbleToAcceptFormatIsXMLforARequest() throws Exception {
    request = new MockRequest();
    request.setResource("TestPage");
    request.addInput("format", "xml");
    response = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals("text/xml", response.getContentType());
  }

  @Test
  public void shouldntBeCaseSensitiveForXMLRequest() throws Exception {
    request = new MockRequest();
    request.setResource("TestPage");
    request.addInput("format", "XMl");
    response = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals("text/xml", response.getContentType());
  }

  @Test
  public void shouldSendTestExecutionReportInXMLUponRequest() throws Exception {
    request = new MockRequest();
    request.setResource("TestPage");
    File pageDirectory = addPageDirectory("TestPage");
    File resultFile = new File(pageDirectory, "20090503110451_30_20_3_0");
    addDummyTestResult(resultFile);
    request.addInput("resultDate", "20090503110451");
    request.addInput("format", "xml");
    response = (SimpleResponse) responder.makeResponse(context, request);
    String content = response.getContent();
    assertHasRegexp("<FitNesseVersion>", content);
    assertEquals("text/xml", response.getContentType());
  }

  private void addBadDummyTestResult(File resultFile) throws Exception {
    FileUtil.createFile(resultFile, "JUNK");
  }

}
