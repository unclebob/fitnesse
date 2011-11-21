package fitnesse.responders.run.formatters;

import fitnesse.FitNesseContext;
import fitnesse.FitNesseVersion;
import fitnesse.responders.run.TestPage;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.SuiteExecutionReport.PageHistoryReference;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import util.DateTimeUtil;
import util.TimeMeasurement;
import util.XmlUtil;

import java.io.StringWriter;
import java.util.Date;
import java.util.List;

public class SuiteHistoryFormatterTest {
  private SuiteHistoryFormatter formatter;
  private WikiPage root;
  private FitNesseContext context;
  private TestPage testPage;
  private StringWriter writer;
  private long testTime;
  private WikiPage suitePage;

  @Before
  public void setup() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    context = FitNesseUtil.makeTestContext(root);
    suitePage = root.addChildPage("SuitePage");
    testPage = new TestPage(suitePage.addChildPage("TestPage"));
    writer = new StringWriter();
    formatter = new SuiteHistoryFormatter(context, suitePage, writer);
    testTime = DateTimeUtil.getTimeFromString("12/5/1952 1:19:00");
  }

  @Test
  public void shouldRememberTestSummariesInReferences() throws Exception {
    TimeMeasurement totalTimeMeasurement = new TimeMeasurement().start();
    performTest(totalTimeMeasurement);
    List<PageHistoryReference> references = formatter.getPageHistoryReferences();
    assertEquals(1, references.size());
    PageHistoryReference pageHistoryReference = references.get(0);
    assertEquals(new TestSummary(1, 2, 3, 4), pageHistoryReference.getTestSummary());
    assertEquals(13, pageHistoryReference.getRunTimeInMillis());
  }

  private void performTest(TimeMeasurement totalTimeMeasurement) throws Exception {
    formatter.announceNumberTestsToRun(1);
    while (totalTimeMeasurement.elapsed() == 0) {
      Thread.sleep(50);
    }
    TimeMeasurement testTimeMeasurement = new TimeMeasurement() {
      @Override
      public long startedAt() {
        return testTime;
      }
      @Override
      public long elapsed() {
        return 13;
      }
    };
    formatter.newTestStarted(testPage, testTimeMeasurement);
    formatter.testComplete(testPage, new TestSummary(1, 2, 3, 4), testTimeMeasurement);
    formatter.allTestingComplete(totalTimeMeasurement.stop());
  }

  @Test
  public void allTestingCompleteShouldProduceLinksAndSetTotalRunTimeOnReport() throws Exception {
    TimeMeasurement totalTimeMeasurement = new TimeMeasurement().start();
    performTest(totalTimeMeasurement);
    assertEquals(totalTimeMeasurement.elapsed(), formatter.suiteExecutionReport.getTotalRunTimeInMillis());
    
    String output = writer.toString();
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
      assertEquals(DateTimeUtil.formatDate(new Date(testTime)), XmlUtil.getTextValue(pageHistoryReferenceElement, "date"));
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
    
    assertEquals(String.valueOf(totalTimeMeasurement.elapsed()), 
        XmlUtil.getTextValue(suiteResultsElement, "totalRunTimeInMillis"));
  }
}
