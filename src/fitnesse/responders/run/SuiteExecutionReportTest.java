package fitnesse.responders.run;

import static fitnesse.responders.run.SuiteExecutionReport.PageHistoryReference;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.util.Date;

public class SuiteExecutionReportTest {
  private SuiteExecutionReport report1;
  private SuiteExecutionReport report2;

   @Before
  public void setUp() throws Exception {
    report1 = new SuiteExecutionReport();
    report2 = new SuiteExecutionReport();
  }

  @Test
  public void degeneratesShouldBeEqual() throws Exception {
    assertEquals(new SuiteExecutionReport(), new SuiteExecutionReport());
  }
  @Test
  public void shouldNotBeEqualIfDifferentTypes() throws Exception {
    assertFalse(new SuiteExecutionReport().equals(new Integer(0)));
  }

  @Test
  public void shouldNotBeEqualWithDifferentRootPaths()throws Exception  {
    report1.rootPath = "here";
    report2.rootPath = "there";
    assertFalse(report1.equals(report2));
  }

  @Test
  public void shouldNotBeEqualIfHaveDifferentReferences() throws Exception {
    report1.addPageHistoryReference(new PageHistoryReference("pageOne",1234, 9));
    report2.addPageHistoryReference(new PageHistoryReference("pageTwo",1234, 9));
    assertFalse(report1.equals(report2));
  }

  @Test
  public void shouldBeEqualIfReferencesAreTheSame() throws Exception {
    PageHistoryReference r1 = new PageHistoryReference("TestPage", 1111, 8);
    PageHistoryReference r2 = new PageHistoryReference("TestPage", 1111, 8);
    r1.getTestSummary().right = 3;
    r2.getTestSummary().right = 3;
    report1.addPageHistoryReference(r1);
    report2.addPageHistoryReference(r2);
    assertEquals(report1, report2);
  }

  @Test
  public void shouldNotBeEqualIfVersionIsDifferent() throws Exception {
    report1.version = "x";
    report2.version = "y";
    assertFalse(report1.equals(report2));
  }

  @Test
  public void shoudlNotBeEqualIfDateIsDifferent() throws Exception {
    report1.date = new Date(1);
    report2.date = new Date(2);
    assertFalse(report1.equals(report2));
  }

  @Test
  public void shouldNotBeEqualIfFinalCountsAreDifferent() throws Exception {
    report1.finalCounts = new TestSummary(1,2,3,4);
    report2.finalCounts = new TestSummary(4,3,2,1);
    assertFalse(report1.equals(report2));    
  }

  @Test
  public void shouldNotBeEqualIfRunTimesAreDifferent() throws Exception {
    report1.addPageHistoryReference(new PageHistoryReference("testPage", 1234, 5));
    report2.addPageHistoryReference(new PageHistoryReference("testPage", 1234, 6));
    assertFalse(report1.equals(report2));    
  }

  @Test
  public void shouldBeEqualWithAllFieldsEqual() throws Exception {
    report1.version = report2.version = "version";
    report1.date = report2.date = new Date(1);
    report1.finalCounts = report2.finalCounts = new TestSummary(4,5,6,7);
    report1.rootPath = report2.rootPath = "rootPath";
    PageHistoryReference r1a = new PageHistoryReference("testPage", 1234, 5);
    PageHistoryReference r2a = new PageHistoryReference("testPage", 1234, 5);
    PageHistoryReference r1b = new PageHistoryReference("myPage", 7734, 6);
    PageHistoryReference r2b = new PageHistoryReference("myPage", 7734, 6);
    r1a.getTestSummary().right=4;
    r2a.getTestSummary().right=4;
    report1.addPageHistoryReference(r1a);
    report1.addPageHistoryReference(r1b);
    report2.addPageHistoryReference(r2a);
    report2.addPageHistoryReference(r2b);
    assertEquals(report1, report2);
  }
  
  @Test
  public void shouldHandleMissingRunTimesGraceFully() throws Exception {
    Element element = mock(Element.class);
    NodeList emptyNodeList = mock(NodeList.class);
    when(element.getElementsByTagName("runTimeInMillis")).thenReturn(emptyNodeList);
    when(emptyNodeList.getLength()).thenReturn(0);
    assertThat(report1.getRunTimeInMillisOrZeroIfNotPresent(element), is(0L));
    
    element = mock(Element.class);
    NodeList matchingNodeList = mock(NodeList.class);
    Node elementWithText = mock(Element.class);
    NodeList childNodeList = mock(NodeList.class);
    Text text = mock(Text.class);
    when(element.getElementsByTagName("runTimeInMillis")).thenReturn(matchingNodeList);
    when(matchingNodeList.getLength()).thenReturn(1);
    when(matchingNodeList.item(0)).thenReturn(elementWithText);
    when(elementWithText.getChildNodes()).thenReturn(childNodeList);
    when(childNodeList.getLength()).thenReturn(1);
    when(childNodeList.item(0)).thenReturn(text);
    when(text.getNodeValue()).thenReturn("255");
    assertThat(report1.getRunTimeInMillisOrZeroIfNotPresent(element), is(255L));
  }
}
