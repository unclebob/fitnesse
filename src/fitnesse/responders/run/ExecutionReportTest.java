package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.VelocityFactory;
import fitnesse.responders.run.TestExecutionReport.TestResult;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import util.DateTimeUtil;
import util.TimeMeasurement;

import java.io.StringWriter;
import java.util.Date;

public class ExecutionReportTest {
  private WikiPage root;
  private FitNesseContext context;

  @Before
  public void setup() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    context = FitNesseUtil.makeTestContext(root);
  }

  @Test
  public void canReadTestExecutionReport() throws Exception {
    TestExecutionReport original = new TestExecutionReport();
    original.version = "version";
    original.rootPath = "rootPath";
    original.setTotalRunTimeInMillis(totalTimeMeasurementWithElapsedMillis(42));

    StringWriter writer = new StringWriter();
    original.toXml(writer, VelocityFactory.getVelocityEngine());
    ExecutionReport report = ExecutionReport.makeReport(writer.toString());
    assertTrue(report instanceof TestExecutionReport);
    assertEquals(original, report);
    assertEquals(42, report.getTotalRunTimeInMillis());
  }

  private TimeMeasurement totalTimeMeasurementWithElapsedMillis(final long millis) {
    return new TimeMeasurement() {
      @Override
      public long elapsed() {
        return millis;
      }
    };
  }

  @Test
  public void canMakeSuiteExecutionReport() throws Exception {
    SuiteExecutionReport original = new SuiteExecutionReport();
    original.version = "version";
    original.rootPath = "rootPath";
    original.date = DateTimeUtil.getDateFromString("12/31/1969 18:00:00");
    original.finalCounts = new TestSummary(1, 2, 3, 4);
    original.setTotalRunTimeInMillis(totalTimeMeasurementWithElapsedMillis(41));
    long time = DateTimeUtil.getTimeFromString("12/31/1969 18:00:00");
    SuiteExecutionReport.PageHistoryReference reference = new SuiteExecutionReport.PageHistoryReference("dah", time, 3L);
    reference.getTestSummary().wrong = 99;
    original.addPageHistoryReference(reference);
    StringWriter writer = new StringWriter();
    original.toXml(writer, VelocityFactory.getVelocityEngine());
    ExecutionReport report = ExecutionReport.makeReport(writer.toString());
    assertTrue(report instanceof SuiteExecutionReport);
    assertEquals(original, report);
    assertEquals(41, report.getTotalRunTimeInMillis());
  }
  
  @Test
  public void shouldHandleMissingRunTimesGraceFully() throws Exception {
    TestExecutionReport report = new TestExecutionReport();
    Element element = mock(Element.class);
    NodeList emptyNodeList = mock(NodeList.class);
    when(element.getElementsByTagName("totalRunTimeInMillis")).thenReturn(emptyNodeList);
    when(emptyNodeList.getLength()).thenReturn(0);
    assertThat(report.getTotalRunTimeInMillisOrZeroIfNotPresent(element), is(0L));
    
    element = mock(Element.class);
    NodeList matchingNodeList = mock(NodeList.class);
    Node elementWithText = mock(Element.class);
    NodeList childNodeList = mock(NodeList.class);
    Text text = mock(Text.class);
    when(element.getElementsByTagName("totalRunTimeInMillis")).thenReturn(matchingNodeList);
    when(matchingNodeList.getLength()).thenReturn(1);
    when(matchingNodeList.item(0)).thenReturn(elementWithText);
    when(elementWithText.getChildNodes()).thenReturn(childNodeList);
    when(childNodeList.getLength()).thenReturn(1);
    when(childNodeList.item(0)).thenReturn(text);
    when(text.getNodeValue()).thenReturn("255");
    assertThat(report.getTotalRunTimeInMillisOrZeroIfNotPresent(element), is(255L));
  }

  @Test
  public void hasRunTimesShouldBeVersionAware() throws Exception {
    assertFalse(executionReportWithVersion("v20100303").hasRunTimes());
    assertTrue(executionReportWithVersion("v20100607").hasRunTimes());
    assertTrue(executionReportWithVersion("v20100608").hasRunTimes());
  }
  
  private ExecutionReport executionReportWithVersion(final String theVersion) {
    return new ExecutionReport() {
      @Override
      protected void unpackResults(Element testResults) throws Exception {
      }
      @Override
      public String getVersion() {
        return theVersion;
      }
    };
  }
}
