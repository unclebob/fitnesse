package fitnesse.reporting;

import fitnesse.FitNesseContext;
import fitnesse.http.ChunkedResponse;
import fitnesse.testsystems.TestSummary;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.mem.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;


public class TestFormatterTest {
    private WikiPage root = InMemoryPage.makeRoot("RooT");
    private FitNesseContext context = FitNesseUtil.makeTestContext(root);
    private ChunkedResponse response = mock(ChunkedResponse.class);
    private WikiPage dummyPage = root.addChildPage("testPage");
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
      }
    };

  private WikiTestPage page;
  private TestSummary right;
  private TestSummary wrong;
  private TestSummary exception;

  @Before
  public void setUp() throws Exception {
    page = new WikiTestPage(new WikiPageDummy("page", "content"));
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

    formatter.testStarted(page);
    formatter.testComplete(page, right);

    formatter.testStarted(page);
    formatter.testComplete(page, wrong);

    formatter.testStarted(page);
    formatter.testComplete(page, exception);

    formatter.close();

    assertEquals(3, formatter.testCount);
    assertEquals(2, formatter.failCount);
    if (!(formatter instanceof PageHistoryFormatter))
      assertEquals(2, BaseFormatter.finalErrorCount);
  }

}
