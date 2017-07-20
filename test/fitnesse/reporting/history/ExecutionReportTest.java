package fitnesse.reporting.history;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringWriter;

import fitnesse.FitNesseVersion;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fitnesse.util.DateTimeUtil;
import fitnesse.util.TimeMeasurement;
import fitnesse.FitNesseContext;
import fitnesse.testsystems.TestSummary;
import fitnesse.testutil.FitNesseUtil;

public class ExecutionReportTest {
  private FitNesseContext context;

  @Before
  public void setup() throws Exception {
    context = FitNesseUtil.makeTestContext();
  }

  @Test
  public void canReadTestExecutionReport() throws Exception {
    TestExecutionReport original = new TestExecutionReport(new FitNesseVersion("version"), "rootPath");
    original.setTotalRunTimeInMillis(totalTimeMeasurementWithElapsedMillis(42));

    StringWriter writer = new StringWriter();
    original.toXml(writer, context.pageFactory.getVelocityEngine());
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
    SuiteExecutionReport original = new SuiteExecutionReport(new FitNesseVersion("version"), "rootPath");
    original.date = DateTimeUtil.getDateFromString("12/31/1969 18:00:00");
    original.getFinalCounts().add(new TestSummary(1, 2, 3, 4));
    original.setTotalRunTimeInMillis(totalTimeMeasurementWithElapsedMillis(41));
    long time = DateTimeUtil.getTimeFromString("12/31/1969 18:00:00");
    SuiteExecutionReport.PageHistoryReference reference = new SuiteExecutionReport.PageHistoryReference("dah", time, 3L);
    reference.setTestSummary(new TestSummary(0, 99, 0, 0));
    original.addPageHistoryReference(reference);
    StringWriter writer = new StringWriter();
    original.toXml(writer, context.pageFactory.getVelocityEngine());
    ExecutionReport report = ExecutionReport.makeReport(writer.toString());
    assertTrue(report instanceof SuiteExecutionReport);
    assertEquals(original, report);
    assertEquals(41, report.getTotalRunTimeInMillis());
  }

  @Test
  public void shouldHandleMissingRunTimesGraceFully() throws Exception {
    TestExecutionReport report = new TestExecutionReport(new FitNesseVersion("version"), "rootPath");
    Element element = mock(Element.class);
    NodeList emptyNodeList = mock(NodeList.class);
    when(element.getElementsByTagName("totalRunTimeInMillis")).thenReturn(emptyNodeList);
    when(emptyNodeList.getLength()).thenReturn(0);
    assertThat(report.getTotalRunTimeInMillisOrMinusOneIfNotPresent(element), is(-1L));

    element = mock(Element.class);
    NodeList matchingNodeList = mock(NodeList.class);
    Node elementWithText = mock(Element.class);
    when(element.getElementsByTagName("totalRunTimeInMillis")).thenReturn(matchingNodeList);
    when(matchingNodeList.getLength()).thenReturn(1);
    when(matchingNodeList.item(0)).thenReturn(elementWithText);
    when(elementWithText.getTextContent()).thenReturn("255");
    assertThat(report.getTotalRunTimeInMillisOrMinusOneIfNotPresent(element), is(255L));
  }

  @Test
  public void readsExecutionLog() throws Exception {
    TestExecutionReport original = new TestExecutionReport(new FitNesseVersion("version"), "rootPath");
    original.setTotalRunTimeInMillis(totalTimeMeasurementWithElapsedMillis(42));
    original.addExecutionContext("command line", "test system");
    original.addStdOut("std out");
    original.addStdErr("std err");
    original.exitCode(1);
    original.exceptionOccurred(new Exception("Fancy exception"));
    StringWriter writer = new StringWriter();
    original.toXml(writer, context.pageFactory.getVelocityEngine());
    ExecutionReport report = ExecutionReport.makeReport(writer.toString());
    assertTrue(report instanceof TestExecutionReport);
    assertEquals(1, report.getExecutionLogs().size());
    ExecutionReport.ExecutionLogReport log = report.getExecutionLogs().get(0);
    assertEquals("command line", log.getCommand());
    assertEquals("test system", log.getTestSystemName());
    assertEquals("std out\n", log.getStdOut());
    assertEquals("std err\n", log.getStdErr());
    assertEquals("Fancy exception", log.getExceptions().get(0).getMessage());
  }

  @Test
  public void testHashCode() {
    TestExecutionReport original = new TestExecutionReport(new FitNesseVersion("version"), "rootPath");

    assertEquals(-836274316, original.hashCode());
  }
}
