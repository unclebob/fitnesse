package fitnesse.responders.testHistory;

import fitnesse.FitNesseContext;
import fitnesse.VelocityFactory;
import fitnesse.responders.run.TestExecutionReport;
import fitnesse.slimTables.HtmlTableScanner;
import fitnesse.slimTables.Table;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;
import org.apache.velocity.app.VelocityEngine;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

import java.io.StringWriter;
import java.io.Writer;

public class HistoryComparerTest {
  private HistoryComparer comparer;
  public FitNesseContext context;
  public WikiPage root;
  public String firstContent;
  public String secondContent;

  @Before
  public void setUp() throws Exception {
    comparer = new HistoryComparer() {
      public String getFileContent(String filePath) {
        if (filePath.equals("TestFolder/FileOne"))
          return "this is file one";
        else if (filePath.equals("TestFolder/FileTwo"))
          return "this is file two";
        else
          return null;
      }
    };
    context = FitNesseUtil.makeTestContext(root);
    root = InMemoryPage.makeRoot("RooT");
    firstContent = getContentWith("pass");
    secondContent = getContentWith("fail");
  }

  @Test
  public void shouldBeAbleToHandleANonexistantFile() throws Exception {
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
  public void shouldCompareTwoSimpleEqualTables() throws Exception {
    String table1text = "<table><tr><td>x</td></tr></table>";
    Table table1 = (new HtmlTableScanner(table1text)).getTable(0);
    String table2text = "<table><tr><td>x</td></tr></table>";
    Table table2 = (new HtmlTableScanner(table2text)).getTable(0);
    assertTrue(HistoryComparer.compareTables(table1, table2));
  }

  @Test
  public void shouldCompareTwoSimpleUnequalTables() throws Exception {
    String table1text = "<table><tr><td>x</td></tr></table>";
    Table table1 = (new HtmlTableScanner(table1text)).getTable(0);
    String table2text = "<table><tr><td>y</td></tr></table>";
    Table table2 = (new HtmlTableScanner(table2text)).getTable(0);
    assertFalse(HistoryComparer.compareTables(table1, table2));
  }

  @Test
  public void shouldCompareTwoDifferentlySizedTables() throws Exception {
    String table1text = "<table><tr><td>x</td></tr></table>";
    Table table1 = (new HtmlTableScanner(table1text)).getTable(0);
    String table2text = "<table><tr><td>x</td><td>y</td></tr></table>";
    Table table2 = (new HtmlTableScanner(table2text)).getTable(0);
    assertFalse(HistoryComparer.compareTables(table1, table2));
  }

  @Test
  public void compareShouldGetReportFileHtmlAndSetResultContentWithPassIfTheFilesWereTheSame() throws Exception {
    HistoryComparer comparer = new HistoryComparer();
    FileUtil.createFile("TestFolder/FirstFile", firstContent);
    FileUtil.createFile("TestFolder/SecondFile", firstContent);
    boolean worked = comparer.compare("TestFolder/FirstFile", "TestFolder/SecondFile");
    assertTrue(worked);
    String expectedResult = "pass";
    assertEquals(expectedResult, HistoryComparer.resultContent);
  }

  @Test
  public void compareShouldGetReportFileHtmlAndSetResultContentWithFailIfTheFilesDiffer() throws Exception {
    HistoryComparer comparer = new HistoryComparer();
    FileUtil.createFile("TestFolder/FirstFile", firstContent);
    FileUtil.createFile("TestFolder/SecondFile", secondContent);
    boolean worked = comparer.compare("TestFolder/FirstFile", "TestFolder/SecondFile");
    assertTrue(worked);
    String expectedResult = "fail";
    assertEquals(expectedResult, HistoryComparer.resultContent);
  }

  public String generateHtmlFromWiki(String passOrFail) throws Exception {
    PageCrawler crawler = root.getPageCrawler();
    String pageText =
      "|myTable|\n" +
        "La la\n" +
        "|NewTable|\n" +
        "|!style_"+passOrFail+"(a)|b|c|\n" +
        "La la la";
    WikiPage myPage = crawler.addPage(root, PathParser.parse("MyPage"), pageText);
    PageData myData = myPage.getData();
    String html = myData.getHtml();
    return html;
  }

  private String getContentWith(String passOrFail) throws Exception {
    TestExecutionReport report = new TestExecutionReport();
    TestExecutionReport.TestResult result = new TestExecutionReport.TestResult();
    result.right = "2";
    result.wrong = "0";
    result.ignores = "0";
    result.exceptions = "0";
    result.content = generateHtmlFromWiki(passOrFail);
    result.relativePageName = "testPageOne";
    report.results.add(result);
    Writer writer = new StringWriter();
    VelocityEngine engine = VelocityFactory.getVelocityEngine();
    report.toXml(writer, engine);
    return writer.toString();
  }
  @After
  public void tearDown(){
    FileUtil.deleteFileSystemDirectory("TestFolder");
  }
}