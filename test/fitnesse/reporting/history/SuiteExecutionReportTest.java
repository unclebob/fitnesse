package fitnesse.reporting.history;

import static fitnesse.reporting.history.SuiteExecutionReport.PageHistoryReference;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import fitnesse.FitNesseVersion;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fitnesse.testsystems.TestSummary;

import java.util.Date;

public class SuiteExecutionReportTest {
  private SuiteExecutionReport report1;
  private SuiteExecutionReport report2;

   @Before
  public void setUp() throws Exception {
    report1 = new SuiteExecutionReport(new FitNesseVersion("version"), "rootPath");
    report2 = new SuiteExecutionReport(new FitNesseVersion("version"), "rootPath");
  }

  @Test
  public void degeneratesShouldBeEqual() throws Exception {
    assertEquals(new SuiteExecutionReport(new FitNesseVersion("version"), "here"),
            new SuiteExecutionReport(new FitNesseVersion("version"), "here"));
  }
  @Test
  public void shouldNotBeEqualIfDifferentTypes() throws Exception {
    assertFalse(new SuiteExecutionReport(new FitNesseVersion("version"), "here").equals(new Integer(0)));
  }

  @Test
  public void shouldNotBeEqualWithDifferentRootPaths()throws Exception  {
    SuiteExecutionReport report1 = new SuiteExecutionReport(new FitNesseVersion("version"), "here");
    SuiteExecutionReport report2 = new SuiteExecutionReport(new FitNesseVersion("version"), "there");
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
    r1.setTestSummary(new TestSummary(3, 0, 0, 0));
    r2.setTestSummary(new TestSummary(3, 0, 0, 0));
    report1.addPageHistoryReference(r1);
    report2.addPageHistoryReference(r2);
    assertEquals(report1, report2);
  }

  @Test
  public void shouldNotBeEqualIfVersionIsDifferent() throws Exception {
    report1 = new SuiteExecutionReport(new FitNesseVersion("x"), "rootPath");
    report2 = new SuiteExecutionReport(new FitNesseVersion("y"), "rootPath");
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
    report1.getFinalCounts().add(new TestSummary(1,2,3,4));
    report2.getFinalCounts().add(new TestSummary(4,3,2,1));
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
    report1.date = report2.date = new Date(1);
    report1.getFinalCounts().add(new TestSummary(4,5,6,7));
    report2.getFinalCounts().add(new TestSummary(4,5,6,7));
    PageHistoryReference r1a = new PageHistoryReference("testPage", 1234, 5);
    PageHistoryReference r2a = new PageHistoryReference("testPage", 1234, 5);
    PageHistoryReference r1b = new PageHistoryReference("myPage", 7734, 6);
    PageHistoryReference r2b = new PageHistoryReference("myPage", 7734, 6);
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
    when(element.getElementsByTagName("runTimeInMillis")).thenReturn(matchingNodeList);
    when(matchingNodeList.getLength()).thenReturn(1);
    when(matchingNodeList.item(0)).thenReturn(elementWithText);
    when(elementWithText.getTextContent()).thenReturn("255");
    assertThat(report1.getRunTimeInMillisOrZeroIfNotPresent(element), is(255L));
  }
}
