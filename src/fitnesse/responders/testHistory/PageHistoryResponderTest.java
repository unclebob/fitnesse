package fitnesse.responders.testHistory;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.run.TestExecutionReport;
import fitnesse.responders.run.TestSummary;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;
import util.RegexTestCase;
import static util.RegexTestCase.assertHasRegexp;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SortedSet;

public class PageHistoryResponderTest {
  private File resultsDirectory;
  private TestHistory history;
  private SimpleDateFormat dateFormat = new SimpleDateFormat(TestHistory.TEST_RESULT_FILE_DATE_PATTERN);
  private PageHistoryResponder responder;
  private SimpleResponse response;
  private MockRequest request;

  @Before
  public void setup() throws Exception {
    resultsDirectory = new File("testHistoryDirectory");
    removeResultsDirectory();
    resultsDirectory.mkdir();
    history = new TestHistory();
    responder = new PageHistoryResponder();
    responder.setResultsDirectory(resultsDirectory);
  }

  @After
  public void teardown() {
    removeResultsDirectory();
  }

  private void makeResponse() throws Exception {
    request = new MockRequest();
    request.setResource("TestPage");
    response = (SimpleResponse) responder.makeResponse(new FitNesseContext(), request);
  }

  private void removeResultsDirectory() {
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

    history.readHistoryDirectory(resultsDirectory);
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

    history.readHistoryDirectory(resultsDirectory);
    PageHistory pageHistory = history.getPageHistory("TestPage");
    assertEquals(2, pageHistory.size());
    assertEquals(12, pageHistory.maxAssertions());
    SortedSet<Date> dates = pageHistory.datesInChronologicalOrder();
    assertEquals(2, dates.size());
    Date date1 = dateFormat.parse("20090418123103");
    Date date2 = dateFormat.parse("20090503110451");
    Date[] dateArray = dates.toArray(new Date[dates.size()]);
    assertEquals(date1, dateArray[0]);
    assertEquals(date2, dateArray[1]);
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
    history.readHistoryDirectory(resultsDirectory);
    PageHistory pageHistory = history.getPageHistory("TestPage");
    Date date = dateFormat.parse("20090503110451");
    PageHistory.PassFailBar passFailBar = pageHistory.getPassFailBar(date, 50);
    return passFailBar;
  }


  @Test
  public void shouldNotReportNoHistoryIfHistoryIsPresent() throws Exception {
    addPageDirectory("TestPage");
    File pageDirectory = addPageDirectory("TestPage");
    addTestResult(pageDirectory, "20090503110451_6_5_3_1");

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
    assertHasRegexp("<td .* class=\"pass\">.*03 May, 09 11:04.*</td>", response.getContent());
  }

  @Test
  public void singleFailingResultsShouldBeRed() throws Exception {
    addPageDirectory("TestPage");
    File pageDirectory = addPageDirectory("TestPage");
    addTestResult(pageDirectory, "20090503110451_6_1_3_0");

    makeResponse();
    assertHasRegexp("<td .* class=\"fail\">.*03 May, 09 11:04.*</td>", response.getContent());
  }

  @Test
  public void singleResultShouldShowPassFailStatistics() throws Exception {
    addPageDirectory("TestPage");
    File pageDirectory = addPageDirectory("TestPage");
    addTestResult(pageDirectory, "20090503110451_6_1_3_1");

    makeResponse();
    assertHasRegexp("<td .* class=\"fail\">2</td>", response.getContent());
    assertHasRegexp("<td .* class=\"pass\">6</td>", response.getContent());
  }

  @Test
  public void singleResultWithNoPassesOrFailuresShouldShowPassFailStatisticsInGrey() throws Exception {
    addPageDirectory("TestPage");
    File pageDirectory = addPageDirectory("TestPage");
    addTestResult(pageDirectory, "20090503110451_0_0_3_0");

    makeResponse();
    assertHasRegexp("<td .* class=\"ignore\">0</td>", response.getContent());
    assertHasRegexp("<td .* class=\"ignore\">0</td>", response.getContent());
  }

  @Test
  public void singleResultShouldShowPassFailBar() throws Exception {
    addPageDirectory("TestPage");
    File pageDirectory = addPageDirectory("TestPage");
    addTestResult(pageDirectory, "20090503110451_30_20_3_0");
    makeResponse();
    StringBuilder expected = new StringBuilder();
    for (int i = 0; i < 30; i++) {
      expected.append("<td id=\"element\" class=\"pass\">&nbsp</td>");
    }
    expected.append(".*");
    for (int i = 0; i < 20; i++) {
      expected.append("<td id=\"element\" class=\"fail\">&nbsp</td>");
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
      expected.append("<td id=\"element\" class=\"pass\">&nbsp</td>");
    }
    expected.append(".*");
    for (int i = 0; i < 5; i++) {
      expected.append("<td id=\"element\" class=\"fail\">&nbsp</td>");
    }
    expected.append(".*");
    for (int i = 0; i < 35; i++) {
      expected.append("<td id=\"element\" class=\"ignore\">&nbsp</td>");
    }
    assertHasRegexp(expected.toString(), response.getContent());
  }

  @Test
  public void canGetHistoricalTestResult() throws Exception {
    File pageDirectory = addPageDirectory("TestPage");
    File resultFile = new File(pageDirectory, "20090503110451_30_20_3_0");
    addDummyTestResult(resultFile);
    makeResultForDate("TestPage", "20090503110451");
    assertHasRegexp("v1", response.getContent());
    assertHasRegexp("1 right", response.getContent());
    assertHasRegexp("2 wrong", response.getContent());
    assertHasRegexp("3 ignored", response.getContent());
    assertHasRegexp("4 exceptions", response.getContent());
    assertHasRegexp("relativePageName", response.getContent());
    assertHasRegexp("11 Right", response.getContent());
    assertHasRegexp("22 Wrong", response.getContent());
    assertHasRegexp("33 Ignores", response.getContent());
    assertHasRegexp("44 Exceptions", response.getContent());
    assertHasRegexp("wad of HTML content", response.getContent());
  }

  private void makeResultForDate(String page, String resultDate) throws Exception {
    request = new MockRequest();
    request.setResource(page);
    request.addInput("resultDate", resultDate);
    response = (SimpleResponse) responder.makeResponse(new FitNesseContext(), request);
  }

  private void addDummyTestResult(File resultFile) throws Exception {
    TestExecutionReport testResponse = makeDummyTestResponse();
    generateTestResultFile(testResponse, resultFile);
  }

  private void generateTestResultFile(TestExecutionReport testResponse, File resultFile) throws Exception {
    VelocityContext velocityContext = new VelocityContext();
    velocityContext.put("response", testResponse);
    FitNesseContext context = new FitNesseContext();
    Template template = context.getVelocityEngine().getTemplate("testResults.vm");
    FileWriter fileWriter = new FileWriter(resultFile);
    template.merge(velocityContext, fileWriter);
    fileWriter.close();
  }

  private TestExecutionReport makeDummyTestResponse() {
    TestExecutionReport testResponse = new TestExecutionReport();
    testResponse.version = "v1";
    testResponse.rootPath = "rootPath";
    testResponse.finalCounts = new TestSummary(1, 2, 3, 4);
    TestExecutionReport.TestResult result = new TestExecutionReport.TestResult();
    testResponse.results.add(result);
    result.right = "11";
    result.wrong = "22";
    result.ignores = "33";
    result.exceptions = "44";
    result.relativePageName = "relativePageName";
    result.content = "wad of HTML content";
    return testResponse;
  }

  @Test
  public void shouldStillGenerateReponseAndBarWhenThereIsAnInvalidFileNameInList() throws Exception {
    File pageDirectory = addPageDirectory("TestPage");
    addTestResult(pageDirectory, "bad_File_name");
    addTestResult(pageDirectory, "20090418123103_1_2_3_4");

    history.readHistoryDirectory(resultsDirectory);
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

  private void addBadDummyTestResult(File resultFile) throws Exception {
    FileUtil.createFile(resultFile, "JUNK");
  }

  private TestExecutionReport makeBadDummyTestResponse() {
    TestExecutionReport testResponse = new TestExecutionReport();
    testResponse.version = "v1";
    testResponse.rootPath = "rootPath";
    testResponse.finalCounts = new TestSummary(1, 2, 3, 4);
    TestExecutionReport.TestResult result = new TestExecutionReport.TestResult();
    testResponse.results.add(result);
    result.right = "xx";
    result.wrong = "22";
    result.ignores = "33";
    result.exceptions = "44";
    result.relativePageName = "relativePageName";
    result.content = "wad of HTML content";
    return testResponse;
  }

}
