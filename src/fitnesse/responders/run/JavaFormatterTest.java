package fitnesse.responders.run;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.Before;
import org.junit.Test;
import util.TimeMeasurement;
import fitnesse.wiki.WikiPageDummy;

public class JavaFormatterTest {

  private final String nestedPageName = "ParentTest.ChildTest";
  private final String suiteName="MySuite";
  JavaFormatter jf;
  JavaFormatter.ResultsRepository mockResultsRepository;
  ResultsListener listener;
  
  @Before
  public void prepare(){
    jf=new JavaFormatter(suiteName);
    mockResultsRepository=mock(JavaFormatter.ResultsRepository.class);
    jf.setResultsRepository(mockResultsRepository);
    listener=mock(ResultsListener.class);
  }
  @Test
  public void getFullPath_WalksUpWikiPageParentsAndBuildsFullPathToPage() throws Exception{
    TestPage wp = buildNestedTestPage();
    assertEquals(nestedPageName, jf.getFullPath(wp.getSourcePage()));
  }
  private TestPage buildNestedTestPage() throws Exception {
    WikiPageDummy wp=new WikiPageDummy("ChildTest",null);
    WikiPageDummy parent=new WikiPageDummy("ParentTest",null);
    wp.setParent(parent);
    parent.setParent(new WikiPageDummy("root"));
    return new TestPage(wp);
  }
  @Test
  public void newTestStarted_SwitchesResultRepositoryToCurrentTest() throws Exception{
    TestPage wp=buildNestedTestPage();
    TimeMeasurement timeMeasurement = new TimeMeasurement();
    jf.newTestStarted(wp, timeMeasurement.start());
    verify(mockResultsRepository).open(nestedPageName);
  }
  @Test
  public void testComplete_closesResultRepositoryAndAddsToTotalTestSummary() throws Exception{
    jf.setTotalSummary(new TestSummary(1,2,3,4));
    TimeMeasurement timeMeasurement = new TimeMeasurement().start();
    jf.testComplete(buildNestedTestPage(), new TestSummary(5,6,7,8), timeMeasurement.stop());
    assertEquals(new TestSummary(6,8,10,12),jf.getTotalSummary());
    verify(mockResultsRepository).close();
  }
  @Test
  public void writeSummary_WritesSummaryOfTestExecutions() throws Exception{
    TimeMeasurement timeMeasurement = new TimeMeasurement().start();
    jf.testComplete(buildNestedTestPage(), new TestSummary(5,6,7,8), timeMeasurement.stop());
    WikiPageDummy secondPage=new WikiPageDummy("SecondPage", null);
    secondPage.setParent(new WikiPageDummy("root", null));
    jf.testComplete(new TestPage(secondPage), new TestSummary(11,12,13,14), timeMeasurement.stop());
    jf.writeSummary("SummaryPageName");
    verify(mockResultsRepository).open("SummaryPageName");
    verify(mockResultsRepository, times(1)).write(JavaFormatter.SUMMARY_HEADER);
    verify(mockResultsRepository, times(1)).write(jf.summaryRow(nestedPageName, new TestSummary(5,6,7,8)));
    verify(mockResultsRepository, times(1)).write(jf.summaryRow("SecondPage", new TestSummary(11,12,13,14)));
    verify(mockResultsRepository, times(1)).write(JavaFormatter.SUMMARY_FOOTER);
  }
  @Test
  public void testComplete_clones_TestSummary_Objects() throws Exception{
    WikiPageDummy secondPage=new WikiPageDummy("SecondPage", null);
    secondPage.setParent(new WikiPageDummy("root", null));

    TestSummary ts=new TestSummary(5,6,7,8);
    TimeMeasurement timeMeasurement = new TimeMeasurement().start();
    jf.testComplete(buildNestedTestPage(), ts, timeMeasurement.stop());
    ts.right=11; ts.wrong=12; ts.ignores=13; ts.exceptions=14;
    jf.testComplete(new TestPage(secondPage), ts, timeMeasurement.stop());
    assertEquals(new TestSummary(5,6,7,8), jf.getTestSummary("ParentTest.ChildTest"));
  }
  @Test
  public void summaryRowFormatsTestOutputRows(){
    assertEquals("pass, no errors or exceptions", 
        "<tr class=\"pass\"><td><a href=\"TestName.html\">TestName</a></td><td>5</td><td>0</td><td>0</td></tr>",
        jf.summaryRow("TestName", new TestSummary(5,0,0,0)));
    assertEquals("red, 1 error ", 
        "<tr class=\"fail\"><td><a href=\"TestName.html\">TestName</a></td><td>5</td><td>1</td><td>0</td></tr>",
        jf.summaryRow("TestName", new TestSummary(5,1,0,0)));
    assertEquals("error,exceptions", 
        "<tr class=\"error\"><td><a href=\"TestName.html\">TestName</a></td><td>5</td><td>6</td><td>7</td></tr>",
        jf.summaryRow("TestName", new TestSummary(5,6,0,7)));
  }
  @Test
  public void testOutputChunk_forwardsWriteToResultRepository() throws Exception{
    jf.testOutputChunk("Hey there!");
    verify(mockResultsRepository).write("Hey there!");
  }
  @Test
  public void getInstance_ReturnsTheSameObjectForTheSameTest(){
    assertSame(JavaFormatter.getInstance("TestOne"),JavaFormatter.getInstance("TestOne"));
    assertNotSame(JavaFormatter.getInstance("TestOne"),JavaFormatter.getInstance("TestTwo"));    
  }
  @Test
  public void allTestingComplete_writesSummaryIfMainPageWasntExecuted() throws Exception{
    TimeMeasurement timeMeasurement = new TimeMeasurement().start();
    jf.testComplete(buildNestedTestPage(), new TestSummary(5,6,7,8), timeMeasurement.stop());
    jf.allTestingComplete(timeMeasurement);
    verify(mockResultsRepository).open(suiteName);     
  }
  @Test
  public void allTestingComplete_doesntWriteSummaryIfMainPageWasExecuted() throws Exception{
    jf=JavaFormatter.getInstance(nestedPageName);
    jf.setResultsRepository(mockResultsRepository);
    TimeMeasurement timeMeasurement = new TimeMeasurement().start();
    jf.testComplete(buildNestedTestPage(), new TestSummary(5,6,7,8), timeMeasurement.stop());
    jf.allTestingComplete(timeMeasurement);
    verify(mockResultsRepository,times(0)).open(nestedPageName);     
  }
  @Test
  public void ifListenerIsSet_newTestStartedFiresTestStarted() throws Exception{
    jf.setListener(listener);
    TestPage page=buildNestedTestPage();
    TimeMeasurement timeMeasurement = new TimeMeasurement();
    jf.newTestStarted(page, timeMeasurement.start());
    verify(listener).newTestStarted(page, timeMeasurement);
  }
  @Test
  public void ifListenerIsSet_TestCompleteFiresTestComplete() throws Exception{
    jf.setListener(listener);
    TestPage page=buildNestedTestPage();
    TimeMeasurement timeMeasurement = new TimeMeasurement().start();
    jf.testComplete(page, new TestSummary(1,2,3,4), timeMeasurement.stop());
    verify(listener).testComplete(page, new TestSummary(1,2,3,4), timeMeasurement);
  }
  @Test
  public void ifListenerIsSet_AllTestingCompleteFiresAllTestingComplete() throws Exception{
    jf.setListener(listener);
    TimeMeasurement totalTimeMeasurement = new TimeMeasurement().start().stop();
    jf.allTestingComplete(totalTimeMeasurement);
    verify(listener).allTestingComplete(same(totalTimeMeasurement));
  }
  @Test
  public void dropInstance_drops_test_results(){
    JavaFormatter first=JavaFormatter.getInstance("TestName");
    JavaFormatter.dropInstance("TestName");
    JavaFormatter second=JavaFormatter.getInstance("TestName");
    assertNotSame(first, second);
  }
}
