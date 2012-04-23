package fitnesse.responders.run.formatters;

import java.io.IOException;
import java.io.Writer;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import util.TimeMeasurement;
import fitnesse.FitNesseContext;
import fitnesse.responders.run.TestPage;
import fitnesse.wiki.WikiPage;

public class SuiteHistoryFormatter extends SuiteExecutionReportFormatter {
  private Writer writer;
  private XmlFormatter.WriterFactory writerFactory;
  private long suiteTime = 0;

  public SuiteHistoryFormatter(FitNesseContext context, final WikiPage page, Writer writer) throws Exception {
    super(context, page);
    this.writer = writer;
  }

  @Override
  public void newTestStarted(TestPage test, TimeMeasurement timeMeasurement) {
    if (suiteTime == 0)
      suiteTime = timeMeasurement.startedAt();
    super.newTestStarted(test, timeMeasurement);
  }

  public SuiteHistoryFormatter(FitNesseContext context, WikiPage page, XmlFormatter.WriterFactory source) {
    super(context, page);
    writerFactory = source;
  }

  @Override
  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws IOException {
    super.allTestingComplete(totalTimeMeasurement);
    if (writerFactory != null)
      writer = writerFactory.getWriter(context, page, getPageCounts(), suiteTime);
    VelocityContext velocityContext = new VelocityContext();
    velocityContext.put("suiteExecutionReport", suiteExecutionReport);
    VelocityEngine velocityEngine = context.pageFactory.getVelocityEngine();
    Template template = velocityEngine.getTemplate("suiteHistoryXML.vm");
    template.merge(velocityContext, writer);
    writer.close();
  }
}
