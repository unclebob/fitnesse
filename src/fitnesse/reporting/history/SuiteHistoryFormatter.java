package fitnesse.reporting.history;

import java.io.IOException;
import java.io.Writer;

import fitnesse.reporting.SuiteExecutionReportFormatter;
import fitnesse.reporting.XmlFormatter;
import fitnesse.testsystems.TestSystem;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import util.TimeMeasurement;
import fitnesse.FitNesseContext;
import fitnesse.wiki.WikiPage;

public class SuiteHistoryFormatter extends SuiteExecutionReportFormatter {
  private Writer writer;
  private XmlFormatter.WriterFactory writerFactory;
  private TimeMeasurement suiteTime;

  public SuiteHistoryFormatter(FitNesseContext context, final WikiPage page, Writer writer) throws Exception {
    super(context, page);
    this.writer = writer;
  }

  @Override
  public void testSystemStarted(TestSystem testSystem) {
    if (suiteTime == null)
      suiteTime = new TimeMeasurement().start();
    super.testSystemStarted(testSystem);
  }

  public SuiteHistoryFormatter(FitNesseContext context, WikiPage page, XmlFormatter.WriterFactory source) {
    super(context, page);
    writerFactory = source;
  }

  @Override
  public void close() throws IOException {
    if (suiteTime == null) return;
    suiteTime.stop();
    super.close();
    if (writerFactory != null)
      writer = writerFactory.getWriter(context, page, getPageCounts(), suiteTime.startedAt());
    VelocityContext velocityContext = new VelocityContext();
    velocityContext.put("suiteExecutionReport", suiteExecutionReport);
    VelocityEngine velocityEngine = context.pageFactory.getVelocityEngine();
    Template template = velocityEngine.getTemplate("suiteHistoryXML.vm");
    template.merge(velocityContext, writer);
    writer.close();
  }
}
