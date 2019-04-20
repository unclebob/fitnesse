package fitnesse.responders.testHistory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static util.RegexTestCase.assertSubString;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import fitnesse.wiki.*;
import org.apache.velocity.app.VelocityEngine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import util.FileUtil;
import fitnesse.FitNesseContext;
import fitnesse.reporting.history.TestExecutionReport;
import fitnesse.testutil.FitNesseUtil;

public class HistoryComparerTest {
  private HistoryComparer comparer;
  public FitNesseContext context;
  public String firstContent;
  public String secondContent;

  @Before
  public void setUp() throws Exception {
    comparer = new HistoryComparer() {
      @Override
      public String getFileContent(String filePath) {
        if (filePath.equals("TestFolder/FileOne"))
          return "this is file one";
        else if (filePath.equals("TestFolder/FileTwo"))
          return "this is file two";
        else
          return null;
      }
    };
    context = FitNesseUtil.makeTestContext();
    firstContent = getContentWith("pass");
    secondContent = getContentWith("fail");
  }

  @Test
  public void shouldBeAbleToHandleANonexistentFile() throws Exception {
    //todo this just tests the mock, not the actual comparer.
    String content = comparer.getFileContent("TestFolder/TestFile");
    assertNull(content);
  }

  @Test
  public void shouldBeAbleToGrabTwoFilesToBeCompared() throws Exception {
    FileUtil.createFile("TestFolder/FileOne", "this is file one");
    FileUtil.createFile("TestFolder/FileTwo", "this is file two");
    comparer.compare("TestFolder/FileOne", "TestFolder/FileTwo");
    assertEquals("this is file one", comparer.firstFileContent);
    assertEquals("this is file two", comparer.secondFileContent);
    FileUtil.deleteFileSystemDirectory("TestFolder");
  }

  @Test
  public void shouldKnowIfTheTwoFilesAreTheSameFile() throws Exception {
    FileUtil.createFile("TestFolder/FileOne", "this is file one");
    boolean compareWorked = comparer.compare("TestFolder/FileOne", "TestFolder/FileOne");
    assertFalse(compareWorked);
    FileUtil.deleteFileSystemDirectory("TestFolder");
  }


  @Test
  public void shouldCompareTwoSetsOfTables() throws Exception {
    comparer.firstFileContent = "<table><tr><td>x</td></tr></table><table><tr><td>y</td></tr></table>";
    comparer.secondFileContent = "<table><tr><td>x</td></tr></table><table><tr><td>y</td></tr></table>";
    assertTrue(comparer.grabAndCompareTablesFromHtml());
    assertEquals(2, comparer.getResultContent().size());
    assertEquals("pass", comparer.getResultContent().get(0));
    assertEquals("pass", comparer.getResultContent().get(1));
  }

  @Test
  public void shouldCompareUnevenAmountsOfTables() throws Exception {
    comparer.firstFileContent = "<table><tr><td>x</td></tr></table><table><tr><td>y</td></tr></table>";
    comparer.secondFileContent = "<table><tr><td>x</td></tr></table>";
    assertTrue(comparer.grabAndCompareTablesFromHtml());
    assertEquals(2, comparer.resultContent.size());
    assertEquals("pass", comparer.resultContent.get(0));
    assertEquals("fail", comparer.resultContent.get(1));
  }

  @Test
  public void findMatchScoreByFirstIndex() throws Exception {
    comparer.matchedTables.add(new HistoryComparer.MatchedPair(1, 2, 1.1));
    comparer.matchedTables.add(new HistoryComparer.MatchedPair(3, 4, 1.0));
    assertEquals(1.1,comparer.findScoreByFirstTableIndex(1),.0001);
    assertEquals(1.0,comparer.findScoreByFirstTableIndex(3),.0001);
  }

  @Test
  public void shouldBeAbleToFindMatchScoreByFirstIndexAndReturnAPercentString() throws Exception {
    comparer.matchedTables.add(new HistoryComparer.MatchedPair(1, 2, 1.1));
    comparer.matchedTables.add(new HistoryComparer.MatchedPair(3, 4, 1.0));
    assertSubString(String.format("%10.2f", 91.67), comparer.findScoreByFirstTableIndexAsStringAsPercent(1));
    assertSubString(String.format("%10.2f", 83.33), comparer.findScoreByFirstTableIndexAsStringAsPercent(3));
  }

  @Test
  public void shouldBeAbleToTellIfTableListsWereACompleteMatch() throws Exception {
    assertFalse(comparer.allTablesMatch());
    comparer.firstTableResults.add("A");
    comparer.firstTableResults.add("B");
    comparer.secondTableResults.add("A");
    comparer.secondTableResults.add("B");
    comparer.matchedTables.add(new HistoryComparer.MatchedPair(0, 0, HistoryComparer.MAX_MATCH_SCORE));
    comparer.matchedTables.add(new HistoryComparer.MatchedPair(1, 1, 1.0));
    assertFalse(comparer.allTablesMatch());
    comparer.matchedTables.remove(new HistoryComparer.MatchedPair(1, 1, 1.0));
    comparer.matchedTables.add(new HistoryComparer.MatchedPair(1, 1, HistoryComparer.MAX_MATCH_SCORE));
    assertTrue(comparer.allTablesMatch());
    comparer.firstTableResults.add("C");
    assertFalse(comparer.allTablesMatch());

  }


  @Test
  public void shouldBeAbleToLineUpMisMatchedTables() throws Exception {
    comparer.firstTableResults.add("A");
    comparer.firstTableResults.add("B");
    comparer.firstTableResults.add("C");
    comparer.firstTableResults.add("D");
    comparer.secondTableResults.add("X");
    comparer.secondTableResults.add("Y");
    comparer.secondTableResults.add("B");
    comparer.secondTableResults.add("Z");
    comparer.secondTableResults.add("D");
    comparer.matchedTables.add(new HistoryComparer.MatchedPair(1, 2, 1.0));
    comparer.matchedTables.add(new HistoryComparer.MatchedPair(3, 4, 1.0));
    comparer.lineUpTheTables();
    assertEquals("A", comparer.firstTableResults.get(0));
    assertEquals("<table><tr><td></td></tr></table>", comparer.firstTableResults.get(1));
    assertEquals("B", comparer.firstTableResults.get(2));
    assertEquals("D", comparer.firstTableResults.get(4));

    assertEquals("X", comparer.secondTableResults.get(0));
    assertEquals("D", comparer.secondTableResults.get(4));
  }

  @Test
  public void shouldBeAbleToLineUpMoreMisMatchedTables() throws Exception {
    comparer.firstTableResults.add("A");
    comparer.firstTableResults.add("B");
    comparer.firstTableResults.add("C");
    comparer.firstTableResults.add("D");
    comparer.secondTableResults.add("B");
    comparer.secondTableResults.add("X");
    comparer.secondTableResults.add("Y");
    comparer.secondTableResults.add("Z");
    comparer.secondTableResults.add("D");
    comparer.secondTableResults.add("shouldMatchWithBlank");
    comparer.matchedTables.add(new HistoryComparer.MatchedPair(1, 0, 1.0));
    comparer.matchedTables.add(new HistoryComparer.MatchedPair(3, 4, 1.0));
    comparer.lineUpTheTables();
    assertEquals("A", comparer.firstTableResults.get(0));
    assertEquals("B", comparer.firstTableResults.get(1));
    assertEquals("<table><tr><td></td></tr></table>", comparer.firstTableResults.get(3));
    assertEquals("<table><tr><td></td></tr></table>", comparer.firstTableResults.get(4));
    assertEquals("D", comparer.firstTableResults.get(5));
    assertEquals("<table><tr><td></td></tr></table>", comparer.firstTableResults.get(6));

    assertEquals("<table><tr><td></td></tr></table>", comparer.secondTableResults.get(0));
    assertEquals("B", comparer.secondTableResults.get(1));
    assertEquals("Y", comparer.secondTableResults.get(3));
    assertEquals("Z", comparer.secondTableResults.get(4));
    assertEquals("D", comparer.secondTableResults.get(5));
    assertEquals("shouldMatchWithBlank", comparer.secondTableResults.get(6));
  }

  @Test
  public void shouldGuarenteeThatBothResultFilesAreTheSameLength() throws Exception {
    comparer.firstTableResults.add("A");
    comparer.firstTableResults.add("B");
    comparer.firstTableResults.add("C");
    comparer.firstTableResults.add("D");
    comparer.secondTableResults.add("X");
    comparer.secondTableResults.add("Y");
    comparer.lineUpTheTables();
    assertEquals(comparer.firstTableResults.size(), comparer.secondTableResults.size());
    assertEquals("<table><tr><td></td></tr></table>", comparer.secondTableResults.get(2));
    assertEquals("<table><tr><td></td></tr></table>", comparer.secondTableResults.get(3));
  }

  @Test
  public void shouldAddBlankRowsForUnmatchedTables() throws Exception {
    comparer.firstTableResults.add("A");
    comparer.firstTableResults.add("B");
    comparer.firstTableResults.add("C");
    comparer.firstTableResults.add("D");
    comparer.secondTableResults.add("X");
    comparer.secondTableResults.add("B");
    comparer.secondTableResults.add("Y");
    comparer.matchedTables.add(new HistoryComparer.MatchedPair(1, 1, 1.0));
    comparer.lineUpTheTables();
    comparer.addBlanksToUnmatchingRows();
    assertEquals(comparer.firstTableResults.size(), comparer.secondTableResults.size());
    assertEquals("A", comparer.firstTableResults.get(0));
    assertEquals("<table><tr><td></td></tr></table>", comparer.firstTableResults.get(1));
    assertEquals("B", comparer.firstTableResults.get(2));
    assertEquals("C", comparer.firstTableResults.get(3));
    assertEquals("<table><tr><td></td></tr></table>", comparer.firstTableResults.get(4));
    assertEquals("D", comparer.firstTableResults.get(5));

    assertEquals("<table><tr><td></td></tr></table>", comparer.secondTableResults.get(0));
    assertEquals("X", comparer.secondTableResults.get(1));
    assertEquals("B", comparer.secondTableResults.get(2));
    assertEquals("<table><tr><td></td></tr></table>", comparer.secondTableResults.get(3));
    assertEquals("Y", comparer.secondTableResults.get(4));
    assertEquals("<table><tr><td></td></tr></table>", comparer.secondTableResults.get(5));
  }

  @Test
  public void shouldHaveCorrectPassFailResults() throws Exception {
    comparer.firstTableResults.add("A");
    comparer.firstTableResults.add("B");
    comparer.firstTableResults.add("C");
    comparer.firstTableResults.add("D");
    comparer.secondTableResults.add("X");
    comparer.secondTableResults.add("B");
    comparer.secondTableResults.add("Y");
    comparer.secondTableResults.add("D");
    comparer.matchedTables.add(new HistoryComparer.MatchedPair(1, 1, HistoryComparer.MAX_MATCH_SCORE));
    comparer.matchedTables.add(new HistoryComparer.MatchedPair(3, 3, HistoryComparer.MAX_MATCH_SCORE));
    comparer.lineUpTheTables();
    comparer.addBlanksToUnmatchingRows();
    comparer.makePassFailResultsFromMatches();
    assertEquals("fail", comparer.resultContent.get(0));
    assertEquals("fail", comparer.resultContent.get(1));
    assertEquals("pass", comparer.resultContent.get(2));
    assertEquals("fail", comparer.resultContent.get(3));
    assertEquals("fail", comparer.resultContent.get(4));
    assertEquals("pass", comparer.resultContent.get(5));

  }


  @Test
  public void compareShouldGetReportHtmlAndSetResultContentWithPassIfTheFilesWereTheSame() throws Exception {
    HistoryComparer comparer = new HistoryComparer();
    FileUtil.createFile("TestFolder/FirstFile", firstContent);
    FileUtil.createFile("TestFolder/SecondFile", firstContent);
    boolean worked = comparer.compare("TestFolder/FirstFile", "TestFolder/SecondFile");
    assertTrue(worked);
    String expectedResult = "pass";
    assertEquals(expectedResult, comparer.resultContent.get(0));
    assertEquals(expectedResult, comparer.resultContent.get(1));
  }

  @Test
  public void compareShouldGetReportFileHtmlAndSetResultContentWithFailIfTheFilesDiffer() throws Exception {
    HistoryComparer comparer = new HistoryComparer();
    FileUtil.createFile("TestFolder/FirstFile", firstContent);
    FileUtil.createFile("TestFolder/SecondFile", secondContent);
    boolean worked = comparer.compare("TestFolder/FirstFile", "TestFolder/SecondFile");
    assertTrue(worked);
    assertEquals("pass", comparer.resultContent.get(0));
    assertEquals("fail", comparer.resultContent.get(1));
  }

  public String generateHtmlFromWiki(String passOrFail) throws Exception {
    String pageText =
      "|myTable|\n" +
        "La la\n" +
        "|NewTable|\n" +
        "|!style_" + passOrFail + "(a)|b|c|\n" +
        "La la la";
    WikiPage myPage = WikiPageUtil.addPage(context.getRootPage(), PathParser.parse("MyPage"), pageText);
    String html = myPage.getHtml();
    return html;
  }

  private String getContentWith(String passOrFail) throws Exception {
    TestExecutionReport report = new TestExecutionReport(null, null);
    TestExecutionReport.TestResult result = new TestExecutionReport.TestResult();
    result.right = "2";
    result.wrong = "0";
    result.ignores = "0";
    result.exceptions = "0";
    result.content = generateHtmlFromWiki(passOrFail);
    result.relativePageName = "testPageOne";
    result.dateString = "2015-10-09T12:23:13-01:00";
    report.addResult(result);
    Writer writer = new StringWriter();
    VelocityEngine engine = context.pageFactory.getVelocityEngine();
    report.toXml(writer, engine);
    return writer.toString();
  }

  @After
  public void tearDown() throws IOException {
    FileUtil.deleteFileSystemDirectory("TestFolder");
  }
}
