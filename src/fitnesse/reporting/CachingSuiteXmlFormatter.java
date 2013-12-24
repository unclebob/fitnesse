package fitnesse.reporting;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;

import fitnesse.FitNesseVersion;
import fitnesse.reporting.history.PageHistory;
import fitnesse.reporting.history.TestHistory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import org.xml.sax.SAXException;
import util.TimeMeasurement;
import fitnesse.FitNesseContext;
import fitnesse.wiki.WikiPage;

@Deprecated // This should just read the page history and reformat it.
public class CachingSuiteXmlFormatter extends SuiteExecutionReportFormatter {
  private final TimeMeasurement totalTimeMeasurement;
  private TestHistory testHistory = new TestHistory();
  private VelocityContext velocityContext;
  private VelocityEngine velocityEngine;
  private Writer writer;
  private boolean includeHtml = false;

  public CachingSuiteXmlFormatter(FitNesseContext context, WikiPage page, Writer writer) {
    super(context, page);
    velocityContext = new VelocityContext();
    velocityEngine = context.pageFactory.getVelocityEngine();
    this.writer = writer;
    totalTimeMeasurement = new TimeMeasurement().start();
  }

  void setTestHistoryForTests(TestHistory testHistory) {
    this.testHistory = testHistory;
  }

  void setVelocityForTests(VelocityContext velocityContext, VelocityEngine engine, Writer writer) {
    this.velocityContext = velocityContext;
    this.velocityEngine = engine;
    this.writer = writer;
  }

  @Override
  public void close() throws IOException {
    totalTimeMeasurement.stop();
    super.close();
    writeOutSuiteXML();
  }

  protected void writeOutSuiteXML() throws IOException {
    testHistory.readHistoryDirectory(context.getTestHistoryDirectory());
    velocityContext.put("formatter", this);
    Template template = velocityEngine.getTemplate("suiteXML.vm");
    template.merge(velocityContext, writer);
    writer.close();
  }

  public TestExecutionReport getTestExecutionReport(SuiteExecutionReport.PageHistoryReference reference) throws IOException, SAXException {
    PageHistory pageHistory = testHistory.getPageHistory(reference.getPageName());
    Date date;
    date = new Date(reference.getTime());
    TestResultRecord record = pageHistory.get(date);
    return makeTestExecutionReport(record.getFile());
  }

  TestExecutionReport makeTestExecutionReport(File file) throws IOException, SAXException {
    return new TestExecutionReport(file);
  }


  public String getRootPageName() {
    return getSuiteExecutionReport().getRootPath();
  }

  public String getFitNesseVersion() {
    return new FitNesseVersion().toString();
  }

  public void includeHtml() {
    includeHtml = true;
  }

  public boolean shouldIncludeHtml() {
    return includeHtml;
  }

  public long getTotalRunTimeInMillis() {
    // for velocity macro only -- would be nicer to rewrite the macro
    // so that it reads from the report directly as per SuiteHistoryFormatter
    return getSuiteExecutionReport().getTotalRunTimeInMillis();
  }
}
