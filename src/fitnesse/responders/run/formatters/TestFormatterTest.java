package fitnesse.responders.run.formatters;

import fitnesse.FitNesseContext;
import fitnesse.http.ChunkedResponse;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSummary;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.TimeMeasurement;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class TestFormatterTest {
    private WikiPage root = InMemoryPage.makeRoot("RooT");
    private FitNesseContext context = FitNesseUtil.makeTestContext(root);
    private ChunkedResponse response = mock(ChunkedResponse.class);
    private WikiPageDummy dummyPage = new WikiPageDummy("testPage", "testContent");
    private XmlFormatter.WriterFactory writerFactory = mock(XmlFormatter.WriterFactory.class);

    private TestTextFormatter testTextFormatter = new TestTextFormatter(response);
    private XmlFormatter xmlFormatter = new XmlFormatter(context, dummyPage, writerFactory) {
      @Override
      protected void writeResults() {
      }
    };
    private InteractiveFormatter testHtmlFormatter = new TestHtmlFormatter(context, dummyPage) {
      @Override
      protected void writeData(String output) {
      }
    };
    private PageHistoryFormatter pageHistoryFormatter = new PageHistoryFormatter(context, dummyPage, writerFactory) {
      protected void writeResults() {
      };
    };

  private TestPage page;
  private TestSummary right;
  private TestSummary wrong;
  private TestSummary exception;

  @Before
  public void setUp() throws Exception {
    page = new TestPage(new WikiPageDummy("page", "content"));
    right = new TestSummary(1, 0, 0, 0);
    wrong = new TestSummary(0, 1, 0, 0);
    exception = new TestSummary(0, 0, 0, 1);
  }
  
  @After
  public void clearStaticFields() {
    BaseFormatter.finalErrorCount = 0;
  }
  
  @Test
  public void testComplete_shouldCountTestResultsForTestTextFormatter() throws Exception {
    countTestResultsForFormatter(testTextFormatter);
  }

  @Test
  public void testComplete_shouldCountTestResultsForXmlFormatter() throws Exception {
    countTestResultsForFormatter(xmlFormatter);
  }

  @Test
  public void testComplete_shouldCountTestResultsForHtmlFormatter() throws Exception {
    countTestResultsForFormatter(testHtmlFormatter);
  }

  @Test
  public void testComplete_shouldCountTestResultsForTestHistoryFormatter() throws Exception {
    countTestResultsForFormatter(pageHistoryFormatter);
  }

  private void countTestResultsForFormatter(BaseFormatter formatter) throws Exception {
    TimeMeasurement timeMeasurement = mock(TimeMeasurement.class);
    when(timeMeasurement.startedAtDate()).thenReturn(new Date(0));
    when(timeMeasurement.elapsedSeconds()).thenReturn(0d);

    formatter.announceNumberTestsToRun(3);
    formatter.testComplete(page, right, timeMeasurement);
    formatter.testComplete(page, wrong, timeMeasurement);
    formatter.testComplete(page, exception, timeMeasurement);
    formatter.allTestingComplete(new TimeMeasurement().start().stop());

    assertEquals(3, formatter.testCount);
    assertEquals(2, formatter.failCount);
    if (!(formatter instanceof PageHistoryFormatter))
      assertEquals(2, BaseFormatter.finalErrorCount);
  }

}
