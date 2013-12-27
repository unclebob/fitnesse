package fitnesse.reporting.history;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;

import fitnesse.FitNesseContext;
import fitnesse.reporting.BaseFormatter;
import fitnesse.reporting.history.PageHistory;
import fitnesse.reporting.history.SuiteExecutionReport;
import fitnesse.reporting.history.SuiteHistoryFormatter;
import fitnesse.reporting.history.TestExecutionReport;
import fitnesse.reporting.history.TestHistory;
import fitnesse.reporting.history.TestResultRecord;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.TestSystem;
import fitnesse.wiki.WikiPage;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.xml.sax.SAXException;

/**
 * Format test results as Xml. This responder returns an alternate
 * format of the test history.
 */
public class SuiteXmlReformatter extends BaseFormatter {

  private final Writer writer;
  private final SuiteHistoryFormatter historyFormatter;
  private boolean includeHtml;
  private TestHistory testHistory;

  public SuiteXmlReformatter(FitNesseContext context, WikiPage page, Writer writer, SuiteHistoryFormatter historyFormatter) {
    super(context, page);
    this.writer = writer;
    this.historyFormatter = historyFormatter;
  }

  @Override
  public void close() throws IOException {
    super.close();

    testHistory = new TestHistory();
    testHistory.readHistoryDirectory(context.getTestHistoryDirectory());

    // read file based on historyFormatter timestamp
    VelocityContext velocityContext = new VelocityContext();
    velocityContext.put("formatter", this);
    velocityContext.put("suiteExecutionReport", historyFormatter.getSuiteExecutionReport());
    velocityContext.put("includeHtml", includeHtml);
    VelocityEngine velocityEngine = context.pageFactory.getVelocityEngine();
    Template template = velocityEngine.getTemplate("suiteXML.vm");
    template.merge(velocityContext, writer);
    writer.close();
  }

  @Override
  public int getErrorCount() {
    return historyFormatter.getErrorCount();
  }

  // called from velocity template.
  public TestExecutionReport.TestResult getTestResult(SuiteExecutionReport.PageHistoryReference reference) throws IOException, SAXException {
    PageHistory pageHistory = testHistory.getPageHistory(reference.getPageName());
    Date date = new Date(reference.getTime());
    TestResultRecord record = pageHistory.get(date);
    return makeTestExecutionReport(record.getFile()).getResults().get(0);
  }

  TestExecutionReport makeTestExecutionReport(File file) throws IOException, SAXException {
    return new TestExecutionReport(file);
  }

  public void includeHtml() {
    this.includeHtml = true;
  }


}
