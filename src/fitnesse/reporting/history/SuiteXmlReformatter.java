package fitnesse.reporting.history;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import fitnesse.FitNesseContext;
import fitnesse.reporting.BaseFormatter;
import fitnesse.wiki.WikiPage;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.xml.sax.SAXException;

/**
 * Format test results as Xml. This responder returns an alternate
 * format of the test history.
 */
public class SuiteXmlReformatter extends BaseFormatter implements Closeable {
  private static final Logger LOG = Logger.getLogger(SuiteXmlReformatter.class.getName());
  private final FitNesseContext context;
  private final Writer writer;
  private final SuiteHistoryFormatter historyFormatter;
  private boolean includeHtml;
  private boolean includeInstructions;
  private TestHistory testHistory;

  public SuiteXmlReformatter(FitNesseContext context, WikiPage page, Writer writer, SuiteHistoryFormatter historyFormatter) {
    super(page);
    this.context = context;
    this.writer = writer;
    this.historyFormatter = historyFormatter;
  }

  @Override
  public void close() throws IOException {
    historyFormatter.close();
    testHistory = new TestHistory(context.getTestHistoryDirectory());

    // read file based on historyFormatter timestamp
    VelocityContext velocityContext = new VelocityContext();
    velocityContext.put("formatter", this);
    velocityContext.put("suiteExecutionReport", historyFormatter.getSuiteExecutionReport());
    velocityContext.put("includeHtml", includeHtml);
    velocityContext.put("includeInstructions", includeInstructions);
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
  public TestExecutionReport.TestResult getTestResult(SuiteExecutionReport.PageHistoryReference reference) throws IOException, SAXException, InvalidReportException {
    PageHistory pageHistory = testHistory.getPageHistory(reference.getPageName());
    if(pageHistory == null) {
      LOG.log(Level.WARNING, "Unable to get page history");
      return null;
    }
    Date date = new Date(reference.getTime());
    TestResultRecord record = pageHistory.get(date);
    return makeTestExecutionReport(record.getFile()).getResults().get(0);
  }

  TestExecutionReport makeTestExecutionReport(File file) throws IOException, SAXException, InvalidReportException {
    return new TestExecutionReport(file);
  }

  public void includeHtml() {
    this.includeHtml = true;
  }

  public void includeInstructions() {
    this.includeInstructions = true;
  }

}
