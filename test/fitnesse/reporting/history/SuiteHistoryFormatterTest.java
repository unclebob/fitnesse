package fitnesse.reporting.history;

import fitnesse.FitNesseContext;
import fitnesse.FitNesseVersion;
import fitnesse.reporting.BaseFormatter;
import fitnesse.reporting.history.SuiteExecutionReport.PageHistoryReference;
import fitnesse.testsystems.TestSummary;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.mem.InMemoryPage;
import fitnesse.wiki.WikiPage;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import util.Clock;
import util.DateAlteringClock;
import util.DateTimeUtil;
import util.TimeMeasurement;
import util.XmlUtil;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class SuiteHistoryFormatterTest {
  private SuiteHistoryFormatter formatter;
  private WikiTestPage testPage;
  private Date testTime;
  private DateAlteringClock clock;
  private List<StringWriter> writers;

  @Before
  public void setup() throws Exception {
    testTime = DateTimeUtil.getDateFromString("12/5/1952 1:19:00");
    clock = new DateAlteringClock(testTime).freeze();

    WikiPage root = InMemoryPage.makeRoot("RooT");
    FitNesseContext context = FitNesseUtil.makeTestContext(root);
    WikiPage suitePage = root.addChildPage("SuitePage");
    testPage = new WikiTestPage(suitePage.addChildPage("TestPage"));
    writers = new LinkedList<StringWriter>();
    formatter = new SuiteHistoryFormatter(context, suitePage, new TestXmlFormatter.WriterFactory() {
      @Override
      public Writer getWriter(FitNesseContext context, WikiPage page, TestSummary counts, long time) throws IOException {
        StringWriter w = new StringWriter();
        writers.add(w);
        return w;
      }
    });
  }

  @After
  public void restoreDefaultClock() {
    Clock.restoreDefaultClock();
  }

  @Test
  public void testCompleteShouldSetFailedCount() throws Exception {
    FitNesseContext context = mock(FitNesseContext.class);

    TimeMeasurement timeMeasurement = mock(TimeMeasurement.class);
    when(timeMeasurement.startedAt()).thenReturn(65L);
    when(timeMeasurement.elapsed()).thenReturn(2L);
    formatter.testStarted(testPage);

    when(timeMeasurement.elapsed()).thenReturn(99L);
    TestSummary testSummary = new TestSummary(4, 2, 7, 3);
    formatter.testComplete(testPage, testSummary);

    assertThat(formatter.getErrorCount(), is(1));

    formatter.close();

    assertThat(BaseFormatter.finalErrorCount, is(2));

  }

  @Test
  public void shouldRememberTestSummariesInReferences() throws Exception {
    performTest(13);
    List<PageHistoryReference> references = formatter.getPageHistoryReferences();
    assertEquals(1, references.size());
    PageHistoryReference pageHistoryReference = references.get(0);
    assertEquals(new TestSummary(1, 2, 3, 4), pageHistoryReference.getTestSummary());
    assertEquals(13, pageHistoryReference.getRunTimeInMillis());
  }

  private void performTest(long elapsedTime) throws Exception {
    formatter.testSystemStarted(null);
    formatter.testStarted(testPage);
    clock.elapse(elapsedTime);
    formatter.testComplete(testPage, new TestSummary(1, 2, 3, 4));
    formatter.close();
  }

  @Test
  public void allTestingCompleteShouldProduceLinksAndSetTotalRunTimeOnReport() throws Exception {
    performTest(13);
    assertEquals(13L, formatter.getSuiteExecutionReport().getTotalRunTimeInMillis());
    
    String output = suiteOutputAsString();
    Document document = XmlUtil.newDocument(output);
    Element suiteResultsElement = document.getDocumentElement();
    assertEquals("suiteResults", suiteResultsElement.getNodeName());
    assertEquals(new FitNesseVersion().toString(), XmlUtil.getTextValue(suiteResultsElement, "FitNesseVersion"));
    assertEquals("SuitePage", XmlUtil.getTextValue(suiteResultsElement, "rootPath"));

    NodeList xmlPageReferences = suiteResultsElement.getElementsByTagName("pageHistoryReference");
    assertEquals(1, xmlPageReferences.getLength());
    for (int referenceIndex = 0; referenceIndex < xmlPageReferences.getLength(); referenceIndex++) {
      Element pageHistoryReferenceElement = (Element) xmlPageReferences.item(referenceIndex);
      assertEquals("SuitePage.TestPage", XmlUtil.getTextValue(pageHistoryReferenceElement, "name"));
      assertEquals(DateTimeUtil.formatDate(testTime), XmlUtil.getTextValue(pageHistoryReferenceElement, "date"));
      String link = "SuitePage.TestPage?pageHistory&resultDate=19521205011900";
      assertEquals(link, XmlUtil.getTextValue(pageHistoryReferenceElement, "pageHistoryLink"));
      Element countsElement = XmlUtil.getElementByTagName(pageHistoryReferenceElement, "counts");
      assertEquals("1", XmlUtil.getTextValue(countsElement, "right"));
      assertEquals("2", XmlUtil.getTextValue(countsElement, "wrong"));
      assertEquals("3", XmlUtil.getTextValue(countsElement, "ignores"));
      assertEquals("4", XmlUtil.getTextValue(countsElement, "exceptions"));
      assertEquals("13", XmlUtil.getTextValue(pageHistoryReferenceElement, "runTimeInMillis"));
    }

    Element finalCounts = XmlUtil.getElementByTagName(suiteResultsElement, "finalCounts");
    assertEquals("0", XmlUtil.getTextValue(finalCounts, "right"));
    assertEquals("1", XmlUtil.getTextValue(finalCounts, "wrong"));
    assertEquals("0", XmlUtil.getTextValue(finalCounts, "ignores"));
    assertEquals("0", XmlUtil.getTextValue(finalCounts, "exceptions"));
    
    assertEquals(String.valueOf(13L),
        XmlUtil.getTextValue(suiteResultsElement, "totalRunTimeInMillis"));
  }

  private String suiteOutputAsString() {
    return writers.get(1).toString();
  }
}
