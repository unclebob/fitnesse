package fitnesse.responders.run;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.*;


import org.junit.Before;
import org.junit.Test;

import fitnesse.wiki.WikiPageDummy;

public class JavaFormatterTest {

  private final String nestedPageName = "ParentTest.ChildTest";
  JavaFormatter jf;
  JavaFormatter.ResultsRepository mockResultsRepository;


  @Before
  public void prepare(){
    jf=new JavaFormatter();
    mockResultsRepository=mock(JavaFormatter.ResultsRepository.class);
    jf.setResultsRepository(mockResultsRepository);
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
}
