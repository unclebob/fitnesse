package fitnesse.responders.run.formatters;

import fitnesse.FitNesseContext;
import fitnesse.VelocityFactory;
import fitnesse.responders.run.TestExecutionReport;
import fitnesse.responders.run.SuiteExecutionReport;
import fitnesse.responders.testHistory.PageHistory;
import fitnesse.responders.testHistory.TestHistory;
import fitnesse.responders.testHistory.TestResultRecord;
import fitnesse.wiki.WikiPage;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import util.TimeMeasurement;

import java.io.Writer;
import java.util.Date;

public class CachingSuiteXmlFormatter extends SuiteExecutionReportFormatter {
  private TestHistory testHistory = new TestHistory();
  private VelocityContext velocityContext;
  private VelocityEngine velocityEngine;
  private Writer writer;
  private boolean includeHtml = false;

  public CachingSuiteXmlFormatter(FitNesseContext context, WikiPage page, Writer writer) throws Exception {
    super(context, page);
    velocityContext = new VelocityContext();
    velocityEngine = VelocityFactory.getVelocityEngine();
    this.writer = writer;
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
  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws Exception {
    super.allTestingComplete(totalTimeMeasurement);
    writeOutSuiteXML();
  }

  protected void writeOutSuiteXML() throws Exception {
    testHistory.readHistoryDirectory(context.getTestHistoryDirectory());
    velocityContext.put("formatter", this);
    Template template = velocityEngine.getTemplate("suiteXML.vm");
    template.merge(velocityContext, writer);
    writer.close();
  }

  public TestExecutionReport getTestExecutionReport(SuiteExecutionReport.PageHistoryReference reference) throws Exception {
    PageHistory pageHistory = testHistory.getPageHistory(reference.getPageName());
    Date date;
    date = new Date(reference.getTime());
    TestResultRecord record = pageHistory.get(date);
    return makeTestExecutionReport().read(record.getFile());
  }

  TestExecutionReport makeTestExecutionReport() {
    return new TestExecutionReport();
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
    return suiteExecutionReport.getTotalRunTimeInMillis();
  }
}
