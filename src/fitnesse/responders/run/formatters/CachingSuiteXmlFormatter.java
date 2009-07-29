package fitnesse.responders.run.formatters;

import fitnesse.FitNesseContext;
import fitnesse.VelocityFactory;
import fitnesse.responders.run.TestExecutionReport;
import fitnesse.responders.run.SuiteExecutionReport;
import fitnesse.responders.testHistory.PageHistory;
import fitnesse.responders.testHistory.TestHistory;
import fitnesse.wiki.WikiPage;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.Writer;
import java.util.Date;

public class CachingSuiteXmlFormatter extends SuiteExecutionReportFormatter {
  private TestHistory testHistory = new TestHistory();
  private VelocityContext velocityContext;
  private VelocityEngine velocityEngine;
  private Writer writer;

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
  public void allTestingComplete() throws Exception {
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
    PageHistory.TestResultRecord record = pageHistory.get(date);
    if(record == null) { //todo get rid of this when we get rid of XMLFormatter.setTestTime().
      throw new RuntimeException("Did you forget to call XmlFormatter.clearTestTime?");
    }
    return makeTestExecutionReport().read(record.getFile());
  }

  TestExecutionReport makeTestExecutionReport() {
    return new TestExecutionReport();
  }

}
