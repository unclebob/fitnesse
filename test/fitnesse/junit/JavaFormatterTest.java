package fitnesse.junit;

import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSummary;
import fitnesse.util.TimeMeasurement;
import fitnesse.wiki.WikiPageDummy;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class JavaFormatterTest {

  private final String nestedPageName = "ParentTest.ChildTest";
  private final String suiteName="MySuite";
  private JavaFormatter jf;
  private JavaFormatter.ResultsRepository mockResultsRepository;

  @Before
  public void prepare(){
    jf=new JavaFormatter(suiteName);
    mockResultsRepository=mock(JavaFormatter.ResultsRepository.class);
    jf.setResultsRepository(mockResultsRepository);
  }

  @Test
  public void getFullPath_WalksUpWikiPageParentsAndBuildsFullPathToPage() throws Exception{
    WikiTestPage wp = buildNestedTestPage();
    assertEquals(nestedPageName, wp.getFullPath());
  }

  private WikiTestPage buildNestedTestPage() throws Exception {
    WikiPageDummy root = new WikiPageDummy("root", null, null);
    WikiPageDummy parent=new WikiPageDummy("ParentTest",null, root);
    WikiPageDummy wp=new WikiPageDummy("ChildTest",null, parent);
    return new WikiTestPage(wp);
  }

  @Test
  public void newTestStarted_SwitchesResultRepositoryToCurrentTest() throws Exception{
    WikiTestPage wp=buildNestedTestPage();
    jf.testStarted(wp);
    verify(mockResultsRepository).open(nestedPageName);
  }

  @Test
  public void testComplete_closesResultRepositoryAndAddsToTotalTestSummary() throws Exception{
    jf.setTotalSummary(new TestSummary(1,2,3,4));
    jf.testComplete(buildNestedTestPage(), new TestSummary(5,6,7,8));
    assertEquals(new TestSummary(6,8,10,12),jf.getTotalSummary());
    verify(mockResultsRepository).close();
  }

  @Test
  public void writeSummary_WritesSummaryOfTestExecutions() throws Exception{
    jf.testComplete(buildNestedTestPage(), new TestSummary(5,6,7,8));
    WikiPageDummy root = new WikiPageDummy("root", null, null);
    WikiPageDummy secondPage=new WikiPageDummy("SecondPage", null, root);
    jf.testComplete(new WikiTestPage(secondPage), new TestSummary(11,12,13,14));
    jf.writeSummary("SummaryPageName");
    String expectedOutput = new StringBuffer()
            .append(JavaFormatter.TestResultsSummaryTable.SUMMARY_HEADER)
            .append(new JavaFormatter.TestResultsSummaryTableRow(nestedPageName, new TestSummary(5,6,7,8)).toString())
            .append(new JavaFormatter.TestResultsSummaryTableRow("SecondPage", new TestSummary(11,12,13,14)).toString())
            .append(JavaFormatter.TestResultsSummaryTable.SUMMARY_FOOTER)
            .toString();
    verify(mockResultsRepository).open("SummaryPageName");
    verify(mockResultsRepository, times(1)).write(expectedOutput);
  }

  @Test
  public void testComplete_clones_TestSummary_Objects() throws Exception{
    WikiPageDummy root = new WikiPageDummy("root", null, null);
    WikiPageDummy secondPage=new WikiPageDummy("SecondPage", null, root);

    TestSummary ts=new TestSummary(5,6,7,8);
    jf.testComplete(buildNestedTestPage(), ts);
    ts.add(ExecutionResult.PASS);
    ts.add(ExecutionResult.FAIL);
    ts.add(ExecutionResult.IGNORE);
    ts.add(ExecutionResult.ERROR);
    jf.testComplete(new WikiTestPage(secondPage), ts);
    assertEquals(new TestSummary(5,6,7,8), jf.getTestSummary("ParentTest.ChildTest"));
  }

  @Test
  public void summaryRowFormatsTestOutputRows(){
    assertEquals("pass, no errors or exceptions",
            "<tr class=\"pass\"><td><a href=\"TestName.html\">TestName</a></td><td>5</td><td>0</td><td>0</td><td>0</td></tr>",
            new JavaFormatter.TestResultsSummaryTableRow("TestName", new TestSummary(5, 0, 0, 0)).toString());
    assertEquals("red, 1 error ",
        "<tr class=\"fail\"><td><a href=\"TestName.html\">TestName</a></td><td>5</td><td>1</td><td>0</td><td>0</td></tr>",
        new JavaFormatter.TestResultsSummaryTableRow("TestName", new TestSummary(5,1,0,0)).toString());
    assertEquals("error,exceptions",
            "<tr class=\"error\"><td><a href=\"TestName.html\">TestName</a></td><td>5</td><td>6</td><td>7</td><td>0</td></tr>",
            new JavaFormatter.TestResultsSummaryTableRow("TestName", new TestSummary(5, 6, 0, 7)).toString());

    TestSummary testSummary = new TestSummary(1,0,0,0);
    testSummary.setRunTimeInMillis(345);
    assertEquals("pass, with duration",
            "<tr class=\"pass\"><td><a href=\"TestName.html\">TestName</a></td><td>1</td><td>0</td><td>0</td><td>345</td></tr>",
            new JavaFormatter.TestResultsSummaryTableRow("TestName", testSummary).toString());
  }

  @Test
  public void testOutputChunk_forwardsWriteToResultRepository() throws Exception{
    TestPage testPage = new WikiTestPage(new WikiPageDummy("name", "content", null));
    jf.testOutputChunk(testPage, "Hey there!");
    verify(mockResultsRepository).write("Hey there!");
  }

  @Test
  public void allTestingComplete_writesSummaryIfMainPageWasntExecuted() throws Exception{
    TimeMeasurement timeMeasurement = new TimeMeasurement().start();
    jf.testComplete(buildNestedTestPage(), new TestSummary(5,6,7,8));
    jf.close();
    verify(mockResultsRepository).open(suiteName);
  }

  @Test
  public void allTestingComplete_doesntWriteSummaryIfMainPageWasExecuted() throws Exception{
    jf= new JavaFormatter(nestedPageName);
    jf.setResultsRepository(mockResultsRepository);
    jf.testComplete(buildNestedTestPage(), new TestSummary(5,6,7,8));
    jf.close();
    verify(mockResultsRepository,times(0)).open(nestedPageName);
  }
}
