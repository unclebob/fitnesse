package fitnesse.responders.run;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;

import fitnesse.wiki.WikiPage;
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
    WikiPageDummy wp = buildNestedTestPage();
    assertEquals(nestedPageName, jf.getFullPath(wp));
  }
  private WikiPageDummy buildNestedTestPage() throws Exception {
    WikiPageDummy wp=new WikiPageDummy("ChildTest",null);
    WikiPageDummy parent=new WikiPageDummy("ParentTest",null);
    wp.setParent(parent);
    parent.setParent(new WikiPageDummy("root"));
    return wp;
  }
  @Test
  public void newTestStarted_SwitchesResultRepositoryToCurrentTest() throws Exception{
    WikiPageDummy wp=buildNestedTestPage();
    jf.newTestStarted(wp, 0);
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
    WikiPageDummy secondPage=new WikiPageDummy("SecondPage", null);
    secondPage.setParent(new WikiPageDummy("root", null));
    jf.testComplete(secondPage, new TestSummary(11,12,13,14));
    jf.writeSummary("SummaryPageName");
    verify(mockResultsRepository).open("SummaryPageName");
    StringBuffer sb=new StringBuffer();
    verify(mockResultsRepository, times(1)).write(JavaFormatter.SUMMARY_HEADER);
    verify(mockResultsRepository, times(1)).write(jf.summaryRow(nestedPageName, new TestSummary(5,6,7,8)));
    verify(mockResultsRepository, times(1)).write(jf.summaryRow("SecondPage", new TestSummary(11,12,13,14)));
    verify(mockResultsRepository, times(1)).write(JavaFormatter.SUMMARY_FOOTER);
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
    jf.testComplete(buildNestedTestPage(), new TestSummary(5,6,7,8));
    jf.allTestingComplete();
    verify(mockResultsRepository).open(suiteName);     
  }
  @Test
  public void allTestingComplete_doesntWriteSummaryIfMainPageWasExecuted() throws Exception{
    jf=JavaFormatter.getInstance(nestedPageName);
    jf.setResultsRepository(mockResultsRepository);
    jf.testComplete(buildNestedTestPage(), new TestSummary(5,6,7,8));
    jf.allTestingComplete();
    verify(mockResultsRepository,times(0)).open(nestedPageName);     
  }
  @Test
  public void ifListenerIsSet_newTestStartedFiresTestStarted() throws Exception{
    jf.setListener(listener);
    WikiPage page=buildNestedTestPage();
    jf.newTestStarted(page, 12);
    verify(listener).newTestStarted(page, 12);
  }
  @Test
  public void ifListenerIsSet_TestCompleteFiresTestComplete() throws Exception{
    jf.setListener(listener);
    WikiPage page=buildNestedTestPage();
    jf.testComplete(page, new TestSummary(1,2,3,4));
    verify(listener).testComplete(page, new TestSummary(1,2,3,4));
  }
  @Test
  public void ifListenerIsSet_AllTestingCompleteFiresAllTestingComplete() throws Exception{
    jf.setListener(listener);
    WikiPage page=buildNestedTestPage();
    jf.allTestingComplete();
    verify(listener).allTestingComplete();
  }
  
}
