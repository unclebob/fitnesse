package fitnesse.responders.testHistory;

import static fitnesse.reporting.history.PageHistory.BarGraph;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static util.RegexTestCase.assertDoesntHaveRegexp;
import static util.RegexTestCase.assertHasRegexp;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import fitnesse.reporting.history.PageHistory;
import fitnesse.reporting.history.TestHistory;
import fitnesse.reporting.history.TestResultRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testsystems.TestSummary;
import fitnesse.testutil.FitNesseUtil;
import util.FileUtil;

public class TestHistoryResponderTest {
  private File resultsDirectory;
  private SimpleDateFormat dateFormat = new SimpleDateFormat(PageHistory.TEST_RESULT_FILE_DATE_PATTERN);
  private TestHistoryResponder responder;
  private SimpleResponse response;
  private FitNesseContext context;

  @Before
  public void setup() throws IOException {
    context = FitNesseUtil.makeTestContext();
    resultsDirectory = context.getTestHistoryDirectory();
    removeResultsDirectory();
    resultsDirectory.mkdirs();
    responder = new TestHistoryResponder();
  }

  private void makeResponse() throws Exception {
    response = (SimpleResponse) responder.makeResponse(context, new MockRequest());
  }

  private void removeResultsDirectory() throws IOException {
    if (resultsDirectory.exists())
      FileUtil.deleteFileSystemDirectory(resultsDirectory);
  }

  private void addPageDirectoryWithOneResult(String pageName, String testResultFileName) throws IOException {
    File pageDirectory = addPageDirectory(pageName);
    addTestResult(pageDirectory, testResultFileName);
  }

  private File addPageDirectory(String pageName) {
    File pageDirectory = new File(resultsDirectory, pageName);
    pageDirectory.mkdir();
    return pageDirectory;
  }

  @After
  public void teardown() throws IOException {
    removeResultsDirectory();
  }

  @Test
  public void emptyHistoryDirectoryShouldShowNoPages() throws Exception {
    TestHistory history = new TestHistory(resultsDirectory);
    assertEquals(0, history.getPageNames().size());
  }

  @Test
  public void historyDirectoryWithOnePageDirectoryShouldShowOnePage() throws Exception {
    addPageDirectoryWithOneResult("SomePage", "20090418123103_1_2_3_4");
    TestHistory history = new TestHistory(resultsDirectory);
    assertEquals(1, history.getPageNames().size());
    assertTrue(history.getPageNames().contains("SomePage"));
  }

  @Test
  public void historyDirectoryWithOneEmptyPageDirectoryShouldShowNoPages() throws Exception {
    addPageDirectory("SomePage");
    TestHistory history = new TestHistory(resultsDirectory);
    assertEquals(0, history.getPageNames().size());
    assertFalse(history.getPageNames().contains("SomePage"));
  }

  @Test
  public void historyDirectoryWithTwoPageDirectoriesShouldShowTwoPages() throws Exception {
    addPageDirectoryWithOneResult("PageOne", "20090418123103_1_2_3_4");
    addPageDirectoryWithOneResult("PageTwo", "20090418123103_1_2_3_4");
    TestHistory history = new TestHistory(resultsDirectory);
    assertEquals(2, history.getPageNames().size());
    assertTrue(history.getPageNames().contains("PageOne"));
    assertTrue(history.getPageNames().contains("PageTwo"));
  }

  @Test
  public void historyDirectoryWithTwoEmptyPageDirectoriesShouldShowNoPages() throws Exception {
    addPageDirectory("SomePage");
    addPageDirectory("SomeOtherPage");
    TestHistory history = new TestHistory(resultsDirectory);
    assertEquals(0, history.getPageNames().size());
    assertFalse(history.getPageNames().contains("SomePage"));
    assertFalse(history.getPageNames().contains("SomeOtherPage"));
  }

  @Test
  public void testHistoryWithPageSelectedShouldShowPagesBelowSelectedPage() throws Exception {
    addPageDirectoryWithOneResult("ParentOne.PageOne", "20090418123103_1_2_3_4");
    addPageDirectoryWithOneResult("ParentOne.PageTwo", "20090418123103_1_2_3_4");
    addPageDirectoryWithOneResult("ParentTwo.PageThree", "20090418123103_1_2_3_4");

    TestHistory history = new TestHistory(resultsDirectory, "ParentOne");
    Set<String> pageNames = history.getPageNames();
    assertEquals(2, pageNames.size());
    assertTrue(pageNames.contains("ParentOne.PageOne"));
    assertTrue(pageNames.contains("ParentOne.PageTwo"));
  }

  @Test
  public void pageDirectoryWithNoResultsShouldShowNoHistory() throws Exception {
    addPageDirectory("SomePage");
    TestHistory history = new TestHistory(resultsDirectory);
    PageHistory pageHistory = history.getPageHistory("SomePage");
    assertNull(pageHistory);
  }

  @Test
  public void pageDirectoryWithOneResultShouldShowOneHistoryRecord() throws Exception {
    addPageDirectoryWithOneResult("SomePage", "20090418123103_1_2_3_4");

    TestHistory history = new TestHistory(resultsDirectory);
    PageHistory pageHistory = history.getPageHistory("SomePage");
    assertEquals(1, pageHistory.getFailures());
    assertEquals(0, pageHistory.getPasses());

    Date date = dateFormat.parse("20090418123103");
    assertEquals(date, pageHistory.getMinDate());
    assertEquals(date, pageHistory.getMaxDate());
    assertEquals(1, pageHistory.size());
    TestResultRecord testResultRecord = pageHistory.get(date);
    assertEquals(date, testResultRecord.getDate());
    assertEquals(new TestSummary(1, 2, 3, 4), testResultRecord.toTestSummary());
  }

  private File addTestResult(File pageDirectory, String testResultFileName) throws IOException {
    File testResultFile = new File(pageDirectory, testResultFileName + ".xml");
    testResultFile.createNewFile();
    return testResultFile;
  }

  @Test
  public void pageDirectoryWithThreeResults() throws Exception {
    File pageDirectory = addPageDirectory("SomePage");
    addTestResult(pageDirectory, "20090418000000_1_0_0_0");
    addTestResult(pageDirectory, "20090419000000_1_1_0_0");
    addTestResult(pageDirectory, "20090417000000_1_0_0_1");

    TestHistory history = new TestHistory(resultsDirectory);
    PageHistory pageHistory = history.getPageHistory("SomePage");
    assertEquals(3, pageHistory.size());
    assertEquals(dateFormat.parse("20090417000000"), pageHistory.getMinDate());
    assertEquals(dateFormat.parse("20090419000000"), pageHistory.getMaxDate());
    assertEquals(1, pageHistory.getPasses());
    assertEquals(2, pageHistory.getFailures());
    assertEquals(new TestSummary(1, 0, 0, 0), pageHistory.get(dateFormat.parse("20090418000000")).toTestSummary());
    assertEquals(new TestSummary(1, 1, 0, 0), pageHistory.get(dateFormat.parse("20090419000000")).toTestSummary());
    assertEquals(new TestSummary(1, 0, 0, 1), pageHistory.get(dateFormat.parse("20090417000000")).toTestSummary());
  }

  @Test
  public void barGraphWithOnePassingResultShouldBeSingleTrueBoolean() throws Exception {
    BarGraph barGraph = makeBarGraph(new String[]{"20090418123103_1_0_0_0"});
    assertEquals(1, barGraph.size());
    assertTrue(barGraph.getPassFail(0).isPass());
  }

  private BarGraph makeBarGraph(String[] testResultFilenames) throws IOException {
    File pageDirectory = addPageDirectory("SomePage");
    for (String fileName : testResultFilenames)
      addTestResult(pageDirectory, fileName);

    TestHistory history = new TestHistory(resultsDirectory);
    PageHistory pageHistory = history.getPageHistory("SomePage");
    BarGraph barGraph = pageHistory.getBarGraph();
    return barGraph;
  }

  @Test
  public void barGraphWithOneFailingResultShouldBeSingleFalseBoolean() throws Exception {
    BarGraph barGraph = makeBarGraph(new String[]{"20090418123103_0_1_0_0"});
    assertEquals(1, barGraph.size());
    assertFalse(barGraph.getPassFail(0).isPass());
  }

  private BarGraph makeBarGraphWithManyResults() throws IOException {
    BarGraph barGraph = makeBarGraph(new String[]{
      "20090418123103_0_0_0_0", //18Apr FAIL
      "20090419123104_1_0_0_0", //19Apr PASS
      "20090420123105_0_1_0_0", //20Apr FAIL
      "20090421123106_0_0_1_0", //21Apr FAIL
      "20090422123107_0_0_0_1", //22Apr FAIL
      "20090423123108_1_1_0_0", //23Apr FAIL
      "20090424123109_1_0_1_0", //24Apr PASS
      "20090425123110_1_0_0_1"  //25Apr FAIL
    });
    return barGraph;
  }

  @Test
  public void barGraphWithManyResultsShouldHaveCorrespondingBooleans() throws Exception {
    BarGraph barGraph = makeBarGraphWithManyResults();
    assertEquals("-+----+-", barGraph.testString());
  }

  @Test
  public void barGraphWithOneResultShouldHaveSameStartingAndEndingDate() throws Exception {
    BarGraph barGraph = makeBarGraph(new String[]{"20090418123103_1_0_0_0"});
    assertEquals(dateFormat.parse("20090418123103"), barGraph.getStartingDate());
    assertEquals(dateFormat.parse("20090418123103"), barGraph.getEndingDate());
  }

  @Test
  public void barGraphWithManyResultsShouldHaveStartingAndEndingDateCorrect() throws Exception {
    BarGraph barGraph = makeBarGraphWithManyResults();
    assertEquals(dateFormat.parse("20090418123103"), barGraph.getStartingDate());
    assertEquals(dateFormat.parse("20090425123110"), barGraph.getEndingDate());
  }

  @Test
  public void BarGraphResultsAreInReverseChronologicalOrder() throws Exception {
    String apr17Fail = "20090417123103_0_1_0_0";
    String apr18Pass = "20090418123103_1_0_0_0";
    BarGraph barGraph = makeBarGraph(new String[]{apr17Fail, apr18Pass});
    assertEquals("+-", barGraph.testString());
  }

  @Test
  public void barGraphLimitedToLast20Results() throws Exception {
    ArrayList<String> dates = new ArrayList<>();
    for (int day = 1; day < 32; day++) {
      int right = (day == 31) ? 1 : 0;
      dates.add(String.format("200905%02d010203_%1d_0_0_0", day, right));
    }
    BarGraph barGraph = makeBarGraph(dates.toArray(new String[dates.size()]));
    assertEquals(20, barGraph.size());
    assertEquals(dateFormat.parse("20090512010203"), barGraph.getStartingDate());
    assertEquals(dateFormat.parse("20090531010203"), barGraph.getEndingDate());
    assertEquals("+-------------------", barGraph.testString());

  }

  @Test
  public void responseWithNoHistoryShouldSayNoHistory() throws Exception {
    makeResponse();
    assertHasRegexp("No History", response.getContent());
  }

  @Test
  public void whenPageDirectoriesHaveNoResultsResponseShouldSayNoHistory() throws Exception {
    addPageDirectory("SomePage");
    makeResponse();
    assertHasRegexp("No History", response.getContent());
  }

  @Test
  public void testHistoryFormatMatchesRegularExpression() throws Exception {
    assertTrue(PageHistory.matchesPageHistoryFileFormat("20090513134559_01_02_03_04.xml"));
  }

  @Test
  public void whenPageDirectoryHasResultsRepsonseShouldShowSummary() throws Exception {
    File pageDirectory = addPageDirectory("SomePage");
    addTestResult(pageDirectory, "20090418123103_1_2_3_4");
    addTestResult(pageDirectory, "20090419123103_1_0_0_0");
    makeResponse();
    assertHasRegexp("SomePage", response.getContent());
    assertHasRegexp("<td class=\"pass\">1</td>", response.getContent());
    assertHasRegexp("<td class=\"fail\">1</td>", response.getContent());
    assertHasRegexp("<td>19 Apr 09, 12:31</td>", response.getContent());
    assertHasRegexp("<td class=\"pass\">.*\\+.*</td>", response.getContent());
    assertHasRegexp("<td class=\"fail\">.*-.*</td>", response.getContent());
    assertDoesntHaveRegexp("No History", response.getContent());

  }

  @Test
  public void shouldNotCountABadDirectoryNameAsAHistoryDirectory() throws Exception {
    addPageDirectoryWithOneResult("SomePage", "20090419123103_1_0_0_0");
    addPageDirectoryWithOneResult("bad+directory+name", "20090419123103_1_0_0_0");

    TestHistory history = new TestHistory(resultsDirectory);
    assertEquals(1, history.getPageNames().size());
    assertTrue(history.getPageNames().contains("SomePage"));
  }

  @Test
  public void shouldGenerateHistoryEvenWithBadFileNames() throws Exception {
    File pageDirectory = addPageDirectory("SomePage");
    addTestResult(pageDirectory, "20090602000000_1_0_0_0");     //good
    addTestResult(pageDirectory, "20090603000000_12_1_0_0");    //good
    addTestResult(pageDirectory, "20090604000000_1_0_125_0");   //good

    addTestResult(pageDirectory, "2009060200000012_1_0_0_0");   //bad
    addTestResult(pageDirectory, "20090602000000_1_0_0_0_0_0"); //bad
    addTestResult(pageDirectory, "bad_file_page_thing");        //bad

    makeResponse();
    TestHistory history = new TestHistory(resultsDirectory);
    PageHistory pageHistory = history.getPageHistory("SomePage");
    assertEquals(3, pageHistory.size());
  }

  @Test
  public void shouldBeAbleToAcceptFormatIsXMLforARequest() throws Exception {
    MockRequest request = new MockRequest();
    request.addInput("format", "xml");
    response = (SimpleResponse) responder.makeResponse(context, request);
    assertHasRegexp("text/xml", response.getContentType());
  }

  @Test
  public void shouldntBeCaseSensitiveForXMLRequest() throws Exception {
    MockRequest request = new MockRequest();
    request.addInput("format", "xML");
    response = (SimpleResponse) responder.makeResponse(context, request);
    assertHasRegexp("text/xml", response.getContentType());
  }
}
