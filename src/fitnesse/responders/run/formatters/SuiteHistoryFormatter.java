package fitnesse.responders.run.formatters;

import fitnesse.FitNesseContext;
import fitnesse.VelocityFactory;
import fitnesse.wiki.WikiPage;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

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
  public void newTestStarted(WikiPage test, long time) throws Exception {
    if (suiteTime == 0)
      suiteTime = time;
    super.newTestStarted(test, time);
  }

  public SuiteHistoryFormatter(FitNesseContext context, WikiPage page, XmlFormatter.WriterFactory source) throws Exception {
    super(context, page);
    writerFactory = source;
  }

  @Override
  public void allTestingComplete() throws Exception {
    if (writerFactory != null)
      writer = writerFactory.getWriter(context, page, getPageCounts(), getSuiteTime());
    VelocityContext velocityContext = new VelocityContext();
    velocityContext.put("suiteExecutionReport", suiteExecutionReport);
    VelocityEngine velocityEngine = VelocityFactory.getVelocityEngine();
    Template template = velocityEngine.getTemplate("suiteHistoryXML.vm");
    template.merge(velocityContext, writer);
    writer.close();
  }

  private long getSuiteTime() {
    if (BaseFormatter.testTime != 0)
      return BaseFormatter.testTime;
    return suiteTime;
  }
}
