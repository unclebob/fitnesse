package fitnesse.responders.run.formatters;

import fitnesse.FitNesseContext;
import fitnesse.FitNesseVersion;
import fitnesse.VelocityFactory;
import fitnesse.responders.run.CompositeExecutionLog;
import fitnesse.responders.run.TestExecutionReport;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestSystem;
import fitnesse.responders.testHistory.PageHistory;
import fitnesse.responders.testHistory.TestHistory;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.Writer;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import util.DateTimeUtils;

public class CachingSuiteXmlFormatter extends BaseFormatter {
  private List<PageHistoryReference> pageHistoryReferences = new ArrayList<PageHistoryReference>();
  private PageHistoryReference referenceToCurrentTest;
  private TestHistory testHistory = new TestHistory();
  private VelocityContext velocityContext;
  private VelocityEngine velocityEngine;
  private Writer writer;
  private TestSummary pageCount;

  public CachingSuiteXmlFormatter(FitNesseContext context, WikiPage page, Writer writer) {
    super(context, page);
    velocityContext = new VelocityContext();
    velocityEngine = VelocityFactory.getVelocityEngine();
    this.writer = writer;
    pageCount = new TestSummary(0, 0, 0, 0);
  }

  void setTestHistoryForTests(TestHistory testHistory) {
    this.testHistory = testHistory;
  }

  void setVelocityForTests(VelocityContext velocityContext, VelocityEngine engine, Writer writer) {
    this.velocityContext = velocityContext;
    this.velocityEngine = engine;
    this.writer = writer;
  }

  public void writeHead(String pageType) throws Exception {
  }

  @Override
  public void allTestingComplete() throws Exception {
    testHistory.readHistoryDirectory(context.getTestHistoryDirectory());
    velocityContext.put("formatter", this);
    Template template = velocityEngine.getTemplate("suiteXML.vm");
    template.merge(velocityContext, writer);
    writer.close();
  }

  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) throws Exception {
  }

  public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) throws Exception {
  }

  public void newTestStarted(WikiPage test, long time) throws Exception {
    String pageName = PathParser.render(test.getPageCrawler().getFullPath(test));
    referenceToCurrentTest = new PageHistoryReference(pageName, time);
  }

  public void testOutputChunk(String output) throws Exception {
  }

  public void testComplete(WikiPage test, TestSummary testSummary) throws Exception {
    getPageHistoryReferences().add(referenceToCurrentTest);
    pageCount.tallyPageCounts(testSummary);
  }

  public List<PageHistoryReference> getPageHistoryReferences() {
    return pageHistoryReferences;
  }

  public TestExecutionReport getTestExecutionReport(PageHistoryReference reference) throws Exception {
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

   @Override
  public int getErrorCount() {
    return pageCount.wrong + pageCount.exceptions;
  }

  public String getRootPageName() throws Exception {
    return page.getName();
  }

  public String getFitNesseVersion() {
    return new FitNesseVersion().toString();
  }

  public TestSummary getPageCounts() {
    return pageCount;
  }

  public static class PageHistoryReference {
    private String pageName;
    private long time;

    public PageHistoryReference(String pageName, long time) {
      this.pageName = pageName;
      this.time = time;
    }

    public String getPageName() {
      return pageName;
    }

    public long getTime() {
      return time;
    }
  }
}
