package fitnesse.responders.run.formatters;

import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.InMemoryPage;
import fitnesse.FitNesseContext;
import fitnesse.FitNesseVersion;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestExecutionReport;
import fitnesse.responders.run.SuiteExecutionReport;
import fitnesse.responders.run.SuiteExecutionReport.PageHistoryReference;
import fitnesse.responders.testHistory.TestHistory;
import fitnesse.responders.testHistory.PageHistory;
import fitnesse.responders.testHistory.TestResultRecord;
import fitnesse.testutil.FitNesseUtil;
import util.DateTimeUtil;
import util.TimeMeasurement;

import java.io.StringWriter;
import java.io.Writer;
import java.io.File;
import java.util.Date;

public class CachingSuiteXmlFormatterTest {
  private CachingSuiteXmlFormatter formatter;
  private FitNesseContext context;
  private WikiPage root;
  private TestSummary testSummary;
  private WikiPage testPage;
  private long testTime;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    context = FitNesseUtil.makeTestContext(root);
    testSummary = new TestSummary(1,2,3,4);
    testPage = root.addChildPage("TestPage");
    formatter = new CachingSuiteXmlFormatter(context,root, null);
    testTime = DateTimeUtil.getTimeFromString("10/8/1988 10:52:12");
  }
  
  @Test
  public void canCreateFormatter() throws Exception {
    Assert.assertTrue(formatter instanceof BaseFormatter);
    assertEquals(0, formatter.getPageHistoryReferences().size());
  }

  @Test
  public void shouldRememberThePageNameAndDateAndRunTime() throws Exception {
    formatter = newNonWritingCachingSuiteXmlFormatter();
    formatter.announceNumberTestsToRun(1);
    TimeMeasurement timeMeasurement = constantStartTimeAndElapsedTimeMeasurement(testTime, 39);
    formatter.newTestStarted(testPage, timeMeasurement);
    
    formatter.testComplete(testPage, testSummary, timeMeasurement);
    assertEquals(1, formatter.getPageHistoryReferences().size());
    PageHistoryReference pageHistoryReference = formatter.getPageHistoryReferences().get(0);
    assertEquals("TestPage", pageHistoryReference.getPageName());
    assertEquals(testTime, pageHistoryReference.getTime());
    assertEquals(39, pageHistoryReference.getRunTimeInMillis());
    
    formatter.allTestingComplete(constantStartTimeAndElapsedTimeMeasurement(testTime, 49));
    assertEquals(49, formatter.suiteExecutionReport.getTotalRunTimeInMillis());
  }
  
  @Test
  public void shouldDelegateToReportForTotalRunTime() throws Exception {
    formatter.suiteExecutionReport = mock(SuiteExecutionReport.class);
    formatter.getTotalRunTimeInMillis();
    verify(formatter.suiteExecutionReport).getTotalRunTimeInMillis(); 
  }

  private CachingSuiteXmlFormatter newNonWritingCachingSuiteXmlFormatter() throws Exception {
    return new CachingSuiteXmlFormatter(context,root, null) {
      @Override
      protected void writeOutSuiteXML() throws Exception {
      }
    };
  }

  private TimeMeasurement constantStartTimeAndElapsedTimeMeasurement(final long startTime, final long elapsed) {
    return new TimeMeasurement() {
      @Override
      public long startedAt() {
        return startTime;
      }
      @Override
      public long elapsed() {
        return elapsed;
      }
    };
  }

  @Test
  public void allTestsCompleteShouldReadTestHistoryAndInvokeVelocity() throws Exception {
    formatter.announceNumberTestsToRun(0);
    TestHistory testHistory = mock(TestHistory.class);
    formatter.setTestHistoryForTests(testHistory);
    VelocityContext velocityContext = mock(VelocityContext.class);
    VelocityEngine velocityEngine = mock(VelocityEngine.class);
    Writer writer = mock(Writer.class);
    formatter.setVelocityForTests(velocityContext, velocityEngine, writer);
    Template template = mock(Template.class);
    when(velocityEngine.getTemplate("suiteXML.vm")).thenReturn(template);
    formatter.allTestingComplete(new TimeMeasurement().start().stop());
    verify(testHistory).readHistoryDirectory(context.getTestHistoryDirectory());
    verify(velocityContext).put("formatter", formatter);
    verify(velocityEngine).getTemplate("suiteXML.vm");
    verify(template).merge(velocityContext, writer);
    verify(writer).close();
  }

  @Test
  public void formatterShouldReturnTestResultsGivenAPageHistoryReference() throws Exception {
    TestHistory testHistory = mock(TestHistory.class);
    PageHistory pageHistory = mock(PageHistory.class);
    TestResultRecord expectedRecord = mock(TestResultRecord.class);
    File file = mock(File.class);
    final TestExecutionReport expectedReport = mock(TestExecutionReport.class);
    CachingSuiteXmlFormatter formatter = new CachingSuiteXmlFormatter(context, testPage, null) {
      @Override
      TestExecutionReport makeTestExecutionReport() {
        return expectedReport;
      }
    };
    Date referenceDate = DateTimeUtil.getDateFromString("12/5/1952 1:19:00");
    
    formatter.setTestHistoryForTests(testHistory);
    when(testHistory.getPageHistory("TestPage")).thenReturn(pageHistory);
    when(expectedRecord.getFile()).thenReturn(file);
    when(pageHistory.get(referenceDate)).thenReturn(expectedRecord);
    when(expectedReport.read(file)).thenReturn(expectedReport);
    SuiteExecutionReport.PageHistoryReference reference;
    reference = new SuiteExecutionReport.PageHistoryReference("TestPage", referenceDate.getTime(), 27);
    TestExecutionReport actualReport = formatter.getTestExecutionReport(reference);
    verify(testHistory).getPageHistory("TestPage");
    verify(pageHistory).get(referenceDate);
    assertSame(expectedReport, actualReport);
  }

  @Test
  public void formatterShouldKnowVersionAndRootPage() throws Exception {
    assertEquals("RooT", formatter.page.getName());
    assertEquals(new FitNesseVersion().toString(), new FitNesseVersion().toString());
  }

  @Test
  public void formatterWithNoTestsShouldHaveZeroPageCounts() throws Exception {
    assertEquals(new TestSummary(0, 0, 0, 0), formatter.getPageCounts());  
  }

  @Test
  public void formatterShouldTallyPageCounts() throws Exception {
    TimeMeasurement timeMeasurement = new TimeMeasurement();
    formatter.newTestStarted(testPage, timeMeasurement.start());
    formatter.testComplete(testPage, new TestSummary(32, 0, 0, 0), timeMeasurement.stop()); // 1 right.
    assertEquals(new TestSummary(1, 0, 0, 0), formatter.getPageCounts());
  }
}
