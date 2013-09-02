package fitnesse.reporting;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fitnesse.FitNesseContext;
import fitnesse.FitNesseVersion;
import fitnesse.reporting.history.PageHistory;
import fitnesse.reporting.SuiteExecutionReport.PageHistoryReference;
import fitnesse.reporting.history.TestHistory;
import fitnesse.testsystems.TestSummary;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.mem.InMemoryPage;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import util.Clock;
import util.DateAlteringClock;
import util.DateTimeUtil;
import util.TimeMeasurement;

public class CachingSuiteXmlFormatterTest {
  private CachingSuiteXmlFormatter formatter;
  private FitNesseContext context;
  private WikiPage root;
  private TestSummary testSummary;
  private WikiTestPage testPage;
  private Date testTime;
  private StringWriter writer;
  private DateAlteringClock clock;

  @Before
  public void setUp() throws Exception {
    testTime = DateTimeUtil.getDateFromString("10/8/1988 10:52:12");
    clock = new DateAlteringClock(testTime).freeze();

    root = InMemoryPage.makeRoot("RooT");
    context = FitNesseUtil.makeTestContext(root);
    testSummary = new TestSummary(1,2,3,4);
    testPage = new WikiTestPage(root.addChildPage("TestPage"));
    writer = new StringWriter();
    formatter = new CachingSuiteXmlFormatter(context,root, writer);
  }

  @After
  public void resetClock() {
    Clock.restoreDefaultClock();
  }

  @Test
  public void canCreateFormatter() throws Exception {
    Assert.assertTrue(formatter instanceof BaseFormatter);
    assertEquals(0, formatter.getPageHistoryReferences().size());
  }

  @Test
  public void shouldRememberThePageNameAndDateAndRunTime() throws Exception {
    formatter = newNonWritingCachingSuiteXmlFormatter();
    formatter.testStarted(testPage);
    clock.elapse(39);
    formatter.testComplete(testPage, testSummary);
    assertEquals(1, formatter.getPageHistoryReferences().size());
    PageHistoryReference pageHistoryReference = formatter.getPageHistoryReferences().get(0);
    assertEquals("TestPage", pageHistoryReference.getPageName());
    assertEquals(testTime.getTime(), pageHistoryReference.getTime());
    assertEquals(39, pageHistoryReference.getRunTimeInMillis());

    clock.elapse(10);
    formatter.close();
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
      protected void writeOutSuiteXML() {
      }
    };
  }

  @Test
  public void allTestsCompleteShouldReadTestHistoryAndInvokeVelocity() throws Exception {
    TestHistory testHistory = mock(TestHistory.class);
    formatter.setTestHistoryForTests(testHistory);
    VelocityContext velocityContext = mock(VelocityContext.class);
    VelocityEngine velocityEngine = mock(VelocityEngine.class);
    Writer writer = mock(Writer.class);
    formatter.setVelocityForTests(velocityContext, velocityEngine, writer);
    Template template = mock(Template.class);
    when(velocityEngine.getTemplate("suiteXML.vm")).thenReturn(template);
    formatter.close();
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
    CachingSuiteXmlFormatter formatter = new CachingSuiteXmlFormatter(context, testPage.getSourcePage(), null) {
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
    assertEquals(new FitNesseVersion().toString(), formatter.getFitNesseVersion().toString());
  }

  @Test
  public void formatterWithNoTestsShouldHaveZeroPageCounts() throws Exception {
    assertEquals(new TestSummary(0, 0, 0, 0), formatter.getPageCounts());  
  }

  @Test
  public void formatterShouldTallyPageCounts() throws Exception {
    formatter.testStarted(testPage);
    formatter.testComplete(testPage, new TestSummary(32, 0, 0, 0)); // 1 right.
    assertEquals(new TestSummary(1, 0, 0, 0), formatter.getPageCounts());
  }

  @Test
  public void shouldIncludeEscapedHtmlIfIncludeHtmlFlagIsSet() throws IOException, SAXException {
    // Note: HTML should be escaped, since FIT(-library) does not output XML compliant HTML
    final TimeMeasurement timeMeasurement = new TimeMeasurement();
    final TestHistory testHistory = mock(TestHistory.class);
    final TestResultRecord testResultRecord = mock(TestResultRecord.class);
    final TestExecutionReport expectedReport = mock(TestExecutionReport.class);
    final File file = mock(File.class);
    final List<TestExecutionReport.TestResult> testResults = new ArrayList<TestExecutionReport.TestResult>();
    TestExecutionReport.TestResult testResult = new TestExecutionReport.TestResult();
    testResults.add(testResult);
    testResult.content = "<html>blah\" <a class=unquoted>link</a>";
    CachingSuiteXmlFormatter formatter = new CachingSuiteXmlFormatter(context, testPage.getSourcePage(), writer) {
      @Override
      TestExecutionReport makeTestExecutionReport() {
        return expectedReport;
      }
    };
    PageHistory pageHistory = mock(PageHistory.class);
    when(testHistory.getPageHistory(anyString())).thenReturn(pageHistory);
    when(pageHistory.get(any(Date.class))).thenReturn(testResultRecord);
    when(testResultRecord.getFile()).thenReturn(file);
    when(expectedReport.read(file)).thenReturn(expectedReport);
    when(expectedReport.getResults()).thenReturn(testResults);

    formatter.setTestHistoryForTests(testHistory);
    formatter.includeHtml();
    formatter.testStarted(testPage);
    formatter.testOutputChunk("<html>blah\" <a class=unquoted");
    formatter.testComplete(testPage, new TestSummary(1, 0, 0, 0));

    formatter.close();
    String output = writer.toString();
    assertTrue(output, output.contains("&lt;html&gt;blah\" &lt;a class=unquoted"));
  }
}
