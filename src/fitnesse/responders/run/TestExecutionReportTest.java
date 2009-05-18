package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import static fitnesse.responders.run.TestExecutionReport.*;

public class TestExecutionReportTest {
  private TestExecutionReport expected;
  private FitNesseContext context;
  private TestExecutionReport actual;

  @Before
  public void setup() throws Exception {
    expected = new TestExecutionReport();
    makeHeader();
    context = FitNesseUtil.makeTestContext(InMemoryPage.makeRoot("RooT"));
  }

  private void makeHeader() {
    expected.version = "version";
    expected.finalCounts = new TestSummary(1, 2, 3, 4);
    expected.rootPath = "rootPath";
  }

  private String reportToXml(TestExecutionReport report) throws Exception {
    StringWriter writer = new StringWriter();
    report.toXml(writer, context.getVelocityEngine());
    writer.close();
    return writer.toString();
  }

  private void serializeAndDeserialize() throws Exception {
    String xmlReport = reportToXml(expected);
    actual = new TestExecutionReport(new ByteArrayInputStream(xmlReport.getBytes()));
  }

  private void addDummyResult() {
    TestResult result = new TestResult();
    result.content = "content";
    result.right = "1";
    result.wrong = "2";
    result.ignores = "3";
    result.exceptions = "4";
    result.relativePageName = "relativePageName";
    result.tags = "tags";
    expected.results.add(result);
  }

  @Test
  public void headerCanBeSerializedAndDeserialized() throws Exception {
    serializeAndDeserialize();
    assertEquals(expected.getVersion(), actual.getVersion());
    assertEquals(expected.getFinalCounts(), actual.getFinalCounts());
    assertEquals(expected.getRootPath(), actual.getRootPath());
  }

  @Test
  public void simpleResultCanBeSerializedAndDeserialized() throws Exception {
    addDummyResult();
    serializeAndDeserialize();
    assertEquals(1, actual.getResults().size());
  }

  @Test
  public void getAssertions() throws Exception {
    addDummyResult();
    assertEquals(expected.getAssertionCounts(), new TestSummary(1, 2, 3, 4));
  }

  @Test
  public void summaryClass() throws Exception {
    assertEquals("pass", TestExecutionReport.summaryClass(new TestSummary(1, 0, 0, 0)));
    assertEquals("pass", TestExecutionReport.summaryClass(new TestSummary(1, 0, 1, 0)));
    assertEquals("fail", TestExecutionReport.summaryClass(new TestSummary(1, 1, 0, 0)));
    assertEquals("fail", TestExecutionReport.summaryClass(new TestSummary(0, 1, 0, 0)));
    assertEquals("fail", TestExecutionReport.summaryClass(new TestSummary(1, 1, 1, 0)));
    assertEquals("fail", TestExecutionReport.summaryClass(new TestSummary(1, 1, 1, 1)));
    assertEquals("error",TestExecutionReport.summaryClass(new TestSummary(0, 0, 0, 1)));
    assertEquals("error",TestExecutionReport.summaryClass(new TestSummary(0, 0, 1, 1)));
    assertEquals("ignore",TestExecutionReport.summaryClass(new TestSummary(0, 0, 0, 0))); 
    assertEquals("ignore",TestExecutionReport.summaryClass(new TestSummary(0, 0, 1, 0)));

  }
}
