package fitnesse.responders.run.formatters;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import fitnesse.responders.run.TestPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import util.Clock;
import util.DateAlteringClock;
import util.DateTimeUtil;
import util.TimeMeasurement;
import fitnesse.FitNesseContext;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestExecutionReport.TestResult;
import fitnesse.responders.run.formatters.XmlFormatter.WriterFactory;
import fitnesse.responders.testHistory.TestHistory;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;

public class XmlFormatterTest {
  private static final String TEST_TIME = "4/13/2009 15:21:43";
  private DateAlteringClock clock;
  
  @Before
  public void setUp() throws ParseException {
    clock = new DateAlteringClock(DateTimeUtil.getDateFromString(TEST_TIME)).freeze();
  }

  @After
  public void tearDown() {
    Clock.restoreDefaultClock();
  }

  @Test
  public void makeFileName() throws Exception {
    XmlFormatter formatter = new XmlFormatter(null, null, null);
    TestSummary summary = new TestSummary(1, 2, 3, 4);
    assertEquals(
      "20090413152143_1_2_3_4.xml", 
      TestHistory.makeResultFileName(summary, clock.currentClockTimeInMillis()));
  }
  
  @Test
  public void processTestResultsShouldBuildUpCurrentResultAndFinalSummary() throws Exception {
    FitNesseContext context = mock(FitNesseContext.class);
    TestPage page = new TestPage(new WikiPageDummy("name", "content"));
    page.getData().setAttribute(PageData.PropertySUITES, "tag1");
    WriterFactory writerFactory = mock(WriterFactory.class);
    final TestResult testResult = new TestResult();
    XmlFormatter formatter = new XmlFormatter(context , page.getSourcePage(), writerFactory) {
      @Override
      protected TestResult newTestResult() {
        return testResult;
      }
    };
    
    formatter.testOutputChunk("outputChunk");
    
    TimeMeasurement timeMeasurement = new TimeMeasurement() {
      public long elapsed() {
        return 27;
      }
    }.start();
    formatter.newTestStarted(page, timeMeasurement);
    
    TestSummary summary = new TestSummary(9,8,7,6);
    formatter.testComplete(page, summary, timeMeasurement);
    assertThat(formatter.finalSummary, equalTo(summary));
    assertThat(formatter.testResponse.results.size(), is(1));
    assertThat(formatter.testResponse.results.get(0), is(testResult));
    assertThat(testResult.startTime, is(timeMeasurement.startedAt()));
    assertThat(testResult.content, is("outputChunk"));
    assertThat(testResult.right, is("9"));
    assertThat(testResult.wrong, is("8"));
    assertThat(testResult.ignores, is("7"));
    assertThat(testResult.exceptions, is("6"));
    assertThat(testResult.runTimeInMillis, is("27"));
    assertThat(testResult.relativePageName, is(page.getName()));
    assertThat(testResult.tags, is("tag1"));
  }
  
  @Test
  public void allTestingCompleteShouldSetTotalRunTime() throws Exception {
    FitNesseContext context = mock(FitNesseContext.class);
    WikiPage page = new WikiPageDummy("name", "content");
    WriterFactory writerFactory = mock(WriterFactory.class);
    XmlFormatter formatter = new XmlFormatter(context , page, writerFactory) {
      @Override
      protected void writeResults() throws Exception {
      }
    };

    TimeMeasurement totalTimeMeasurement = mock(TimeMeasurement.class);
    when(totalTimeMeasurement.elapsed()).thenReturn(77L);
    formatter.allTestingComplete(totalTimeMeasurement);
    assertThat(formatter.testResponse.getTotalRunTimeInMillis(), is(77L));
  }
}
