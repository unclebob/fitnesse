package fitnesse.responders.run.formatters;

import fitnesse.FitNesseContext;
import fitnesse.VelocityFactory;
import fitnesse.responders.run.TestPage;
import fitnesse.wiki.WikiPage;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import util.TimeMeasurement;

import java.io.Writer;

public class SuiteHistoryFormatter extends SuiteExecutionReportFormatter {
  private Writer writer;
  private XmlFormatter.WriterFactory writerFactory;
  private long suiteTime = 0;

  public SuiteHistoryFormatter(FitNesseContext context, final WikiPage page, Writer writer) throws Exception {
    super(context, page);
    this.writer = writer;
  }

  @Override
  public void newTestStarted(TestPage test, TimeMeasurement timeMeasurement) throws Exception {
    if (suiteTime == 0)
      suiteTime = timeMeasurement.startedAt();
    super.newTestStarted(test, timeMeasurement);
  }

  public SuiteHistoryFormatter(FitNesseContext context, WikiPage page, XmlFormatter.WriterFactory source) throws Exception {
    super(context, page);
    writerFactory = source;
  }

  @Override
  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws Exception {
    super.allTestingComplete(totalTimeMeasurement);
    if (writerFactory != null)
      writer = writerFactory.getWriter(context, page, getPageCounts(), suiteTime);
    VelocityContext velocityContext = new VelocityContext();
    velocityContext.put("suiteExecutionReport", suiteExecutionReport);
    VelocityEngine velocityEngine = VelocityFactory.getVelocityEngine();
    Template template = velocityEngine.getTemplate("suiteHistoryXML.vm");
    template.merge(velocityContext, writer);
    writer.close();
  }
}
