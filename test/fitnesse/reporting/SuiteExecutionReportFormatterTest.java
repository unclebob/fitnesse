package fitnesse.reporting;

import java.util.Date;

import fitnesse.FitNesseContext;
import fitnesse.reporting.SuiteExecutionReport.PageHistoryReference;
import fitnesse.testsystems.TestSummary;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.Clock;
import util.DateAlteringClock;
import util.TimeMeasurement;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SuiteExecutionReportFormatterTest {

  private DateAlteringClock clock;

  @Before
  public void fixTime() {
    clock = new DateAlteringClock(new Date()).freeze();
  }

  @After
  public void restoreDefaultClock() {
    Clock.restoreDefaultClock();
  }

  @Test
  public void testCompleteShouldSetRunTimeForCurrentReference() throws Exception {
    FitNesseContext context = mock(FitNesseContext.class);
    WikiTestPage page = new WikiTestPage(new WikiPageDummy("name", "content"));
    SuiteExecutionReportFormatter formatter = new SuiteExecutionReportFormatter(context, page.getSourcePage());

    formatter.testStarted(page);

    clock.elapse(99L);
    TestSummary testSummary = new TestSummary(4, 2, 7, 3);
    formatter.testComplete(page, testSummary);

    assertThat(formatter.suiteExecutionReport.getPageHistoryReferences().size(), is(1));
    PageHistoryReference reference = formatter.suiteExecutionReport.getPageHistoryReferences().get(0);
    assertThat(reference.getTestSummary(), equalTo(testSummary));
    assertThat(reference.getRunTimeInMillis(), is(99L));
  }

  @Test
  public void allTestingCompleteShouldSetTotalRunTimeOnReport() throws Exception {
    FitNesseContext context = mock(FitNesseContext.class);
    WikiPage page = new WikiPageDummy("name", "content");
    SuiteExecutionReportFormatter formatter = new SuiteExecutionReportFormatter(context, page);

    clock.elapse(50);
    formatter.close();
    assertThat(formatter.suiteExecutionReport.getTotalRunTimeInMillis(),
      is(50L));
  }

  @Test
  public void testCompleteShouldSetFailedCount() throws Exception {
    FitNesseContext context = mock(FitNesseContext.class);
    WikiTestPage page = new WikiTestPage(new WikiPageDummy("name", "content"));
    SuiteExecutionReportFormatter formatter = new SuiteExecutionReportFormatter(context, page.getSourcePage());

    TimeMeasurement timeMeasurement = mock(TimeMeasurement.class);
    when(timeMeasurement.startedAt()).thenReturn(65L);
    when(timeMeasurement.elapsed()).thenReturn(2L);
    formatter.testStarted(page);

    when(timeMeasurement.elapsed()).thenReturn(99L);
    TestSummary testSummary = new TestSummary(4, 2, 7, 3);
    formatter.testComplete(page, testSummary);

    assertThat(formatter.failCount, is(2));

    formatter.close();

    assertThat(BaseFormatter.finalErrorCount, is(2));

  }

}
