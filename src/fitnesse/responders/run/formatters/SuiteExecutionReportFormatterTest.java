package fitnesse.responders.run.formatters;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fitnesse.responders.run.TestPage;
import org.junit.Test;

import util.TimeMeasurement;
import fitnesse.FitNesseContext;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.SuiteExecutionReport.PageHistoryReference;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;

public class SuiteExecutionReportFormatterTest {
  
  @Test
  public void testCompleteShouldSetRunTimeForCurrentReference() throws Exception {
    FitNesseContext context = mock(FitNesseContext.class);
    TestPage page = new TestPage(new WikiPageDummy("name", "content"));
    SuiteExecutionReportFormatter formatter = new SuiteExecutionReportFormatter(context, page.getSourcePage());
    
    TimeMeasurement timeMeasurement = mock(TimeMeasurement.class);
    when(timeMeasurement.startedAt()).thenReturn(65L);
    when(timeMeasurement.elapsed()).thenReturn(2L);
    formatter.newTestStarted(page, timeMeasurement);
    
    when(timeMeasurement.elapsed()).thenReturn(99L);
    TestSummary testSummary = new TestSummary(4,2,7,3);
    formatter.testComplete(page, testSummary, timeMeasurement);
    
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
    
    TimeMeasurement totalTimeMeasurement = new TimeMeasurement().start();
    formatter.announceNumberTestsToRun(0);
    while (totalTimeMeasurement.elapsed() == 0) {
      Thread.sleep(50);
    }

    formatter.allTestingComplete(totalTimeMeasurement);
    assertThat(formatter.suiteExecutionReport.getTotalRunTimeInMillis(), 
        is(totalTimeMeasurement.elapsed()));
  }
  
  @Test
  public void testCompleteShouldSetFailedCount() throws Exception {
    FitNesseContext context = mock(FitNesseContext.class);
    TestPage page = new TestPage(new WikiPageDummy("name", "content"));
    SuiteExecutionReportFormatter formatter = new SuiteExecutionReportFormatter(context, page.getSourcePage());
    
    TimeMeasurement timeMeasurement = mock(TimeMeasurement.class);
    when(timeMeasurement.startedAt()).thenReturn(65L);
    when(timeMeasurement.elapsed()).thenReturn(2L);
    formatter.newTestStarted(page, timeMeasurement);
    
    when(timeMeasurement.elapsed()).thenReturn(99L);
    TestSummary testSummary = new TestSummary(4,2,7,3);
    formatter.testComplete(page, testSummary, timeMeasurement);
	
	assertThat(formatter.failCount, is(5));
	
    formatter.allTestingComplete(timeMeasurement);
	
    assertThat(BaseFormatter.finalErrorCount, is(5));
    
  }

}
