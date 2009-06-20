package fitnesse.responders.testHistory;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.run.TestSummary;
import static fitnesse.responders.testHistory.PageHistory.BarGraph;
import org.junit.After;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;
import static util.RegexTestCase.assertHasRegexp;
import static util.RegexTestCase.assertDoesntHaveRegexp;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TestHistoryResponderTest {
  private File resultsDirectory;
  private TestHistory history;
  private SimpleDateFormat dateFormat = new SimpleDateFormat(TestHistory.TEST_RESULT_FILE_DATE_PATTERN);
  private TestHistoryResponder responder;
  private SimpleResponse response;
  private FitNesseContext context;

  @Before
  public void setup() throws Exception {
    resultsDirectory = new File("testHistoryDirectory");
    removeResultsDirectory();
    resultsDirectory.mkdir();
    history = new TestHistory();
    responder = new TestHistoryResponder();
    responder.setResultsDirectory(resultsDirectory);
    context = new FitNesseContext();
  }

  private void makeResponse() throws Exception {
    response = (SimpleResponse) responder.makeResponse(context, new MockRequest());
  }

  private void removeResultsDirectory() {
    if (resultsDirectory.exists())
      FileUtil.deleteFileSystemDirectory(resultsDirectory);
  }

  @After
  public void teardown() {
    removeResultsDirectory();
  }

  @Test
  public void emptyHistoryDirectoryShouldShowNoPages() throws Exception {
    history.readHistoryDirectory(resultsDirectory);
    assertEquals(0, history.getPageNames().size());
  }

  @Test
  public void historyDirectoryWithOnePageDirectoryShouldShowOnePage() throws Exception {
    addPageDirectory("SomePage");
    history.readHistoryDirectory(resultsDirectory);
    assertEquals(1, history.getPageNames().size());
    assertTrue(history.getPageNames().contains("SomePage"));
  }

  private File addPageDirectory(String pageName) {
    File pageDirectory = new File(resultsDirectory, pageName);
    pageDirectory.mkdir();
    return pageDirectory;
  }

  @Test
  public void historyDirectoryWithTwoPageDirectoriesShouldShowTwoPages() throws Exception {
    addPageDirectory("PageOne");
    addPageDirectory("PageTwo");
    history.readHistoryDirectory(resultsDirectory);
    assertEquals(2, history.getPageNames().size());
    assertTrue(history.getPageNames().contains("PageOne"));
    assertTrue(history.getPageNames().contains("PageTwo"));
  }

  @Test
  public void pageDirectoryWithNoResultsShouldShowNoHistory() throws Exception {
    addPageDirectory("SomePage");
    history.readHistoryDirectory(resultsDirectory);
    PageHistory pageHistory = history.getPageHistory("SomePage");
    assertNull(pageHistory);
  }

  @Test
  public void pageDirectoryWithOneResultShouldShowOneHistoryRecord() throws Exception {
    File pageDirectory = addPageDirectory("SomePage");
    addTestResult(pageDirectory, "20090418123103_1_2_3_4");

    history.readHistoryDirectory(resultsDirectory);
    PageHistory pageHistory = history.getPageHistory("SomePage");
    assertEquals(1, pageHistory.getFailures());
    assertEquals(0, pageHistory.getPasses());

    Date date = dateFormat.parse("20090418123103");
    assertEquals(date, pageHistory.getMinDate());
    assertEquals(date, pageHistory.getMaxDate());
    assertEquals(1, pageHistory.size());
    PageHistory.TestResultRecord testSummary = pageHistory.get(date);
    assertEquals(date, testSummary.getDate());
    assertEquals(new TestSummary(1, 2, 3, 4), testSummary);
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
    history.readHistoryDirectory(resultsDirectory);
    PageHistory pageHistory = history.getPageHistory("SomePage");
    assertEquals(3, pageHistory.size());
    assertEquals(dateFormat.parse("20090417000000"), pageHistory.getMinDate());
    assertEquals(dateFormat.parse("20090419000000"), pageHistory.getMaxDate());
    assertEquals(1, pageHistory.getPasses());
    assertEquals(2, pageHistory.getFailures());
    assertEquals(new TestSummary(1, 0, 0, 0), pageHistory.get(dateFormat.parse("20090418000000")));
    assertEquals(new TestSummary(1, 1, 0, 0), pageHistory.get(dateFormat.parse("20090419000000")));
    assertEquals(new TestSummary(1, 0, 0, 1), pageHistory.get(dateFormat.parse("20090417000000")));
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

    history.readHistoryDirectory(resultsDirectory);
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
    ArrayList<String> dates = new ArrayList<String>();
    for (int day = 1; day < 32; day++) {
      int right = (day == 31) ? 1 : 0;
      dates.add(String.format("200905%02d010203_%1d_0_0_0", day, right));
    }
    BarGraph barGraph = makeBarGraph(dates.toArray(new String[0]));
    assertEquals(20, barGraph.size());
    assertEquals(dateFormat.parse("20090512010203"), barGraph.getStartingDate());
    assertEquals(dateFormat.parse("20090531010203"), barGraph.getEndingDate());
    assertEquals("+-------------------", barGraph.testString());

  }

  @Test
  public void responderShouldDefaultToContextDirectory() throws Exception {
    responder.setResultsDirectory(null);
    responder.generateNullResponseForTest();
    makeResponse();
    assertEquals(context.getTestHistoryDirectory().getPath(), responder.getResultsDirectory().getPath());
  }

  @Test
  public void responseShouldBeOfTypeTextHtml() throws Exception {
    responder.generateNullResponseForTest();
    makeResponse();
    assertEquals("text/html; charset=utf-8", response.getContentType());
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
    assertHasRegexp("<td>19 Apr, 09 12:31</td>", response.getContent());
    assertHasRegexp("<td class=\"pass\">.*\\+.*</td>", response.getContent());
    assertHasRegexp("<td class=\"fail\">.*-.*</td>", response.getContent());
    assertDoesntHaveRegexp("No History", response.getContent());

  }

  @Test
  public void shouldNotCountABadDirectoryNameAsAHistoryDirectory() throws Exception {
    addPageDirectory("SomePage");
    addPageDirectory("bad-directory-name");
    history.readHistoryDirectory(resultsDirectory);
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
    history.readHistoryDirectory(resultsDirectory);
    PageHistory pageHistory = history.getPageHistory("SomePage");
    assertEquals(3, pageHistory.size());
  }
}
