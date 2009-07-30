package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.VelocityFactory;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import util.DateTimeUtil;

import java.io.StringWriter;

public class ExecutionReportTest {
  private WikiPage root;
  private FitNesseContext context;

  @Before
  public void setup() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    context = FitNesseUtil.makeTestContext(root);
  }

  @Test
  public void canReadTestExecutionReport() throws Exception {
    TestExecutionReport original = new TestExecutionReport();
    original.version = "version";
    original.rootPath = "rootPath";

    StringWriter writer = new StringWriter();
    original.toXml(writer, VelocityFactory.getVelocityEngine());
    ExecutionReport report = ExecutionReport.makeReport(writer.toString());
    assertTrue(report instanceof TestExecutionReport);
  }

  @Test
  public void canMakeSuiteExecutionReport() throws Exception {
    SuiteExecutionReport original = new SuiteExecutionReport();
    original.version = "version";
    original.rootPath = "rootPath";
    original.date = DateTimeUtil.getDateFromString("12/31/1969 18:00:00");
    original.finalCounts = new TestSummary(1, 2, 3, 4);
    long time = DateTimeUtil.getTimeFromString("12/31/1969 18:00:00");
    SuiteExecutionReport.PageHistoryReference reference = new SuiteExecutionReport.PageHistoryReference("dah", time);
    reference.getTestSummary().wrong = 99;
    original.addPageHistoryReference(reference);
    StringWriter writer = new StringWriter();
    original.toXml(writer, VelocityFactory.getVelocityEngine());
    ExecutionReport report = ExecutionReport.makeReport(writer.toString());
    assertTrue(report instanceof SuiteExecutionReport);
    assertEquals(original, report);
  }
}
