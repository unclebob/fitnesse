package fitnesse.responders.run.formatters;

import fitnesse.FitNesseContext;
import fitnesse.responders.run.TestExecutionReport;
import fitnesse.responders.run.TestSummary;
import fitnesse.wiki.WikiPage;

public class PageHistoryFormatter extends XmlFormatter {
  private WikiPage historyPage;

  public PageHistoryFormatter(FitNesseContext context, final WikiPage page, WriterFactory writerFactory) throws Exception {
    super(context, page, writerFactory);
  }

  @Override
  public void newTestStarted(WikiPage testedPage, long time) throws Exception {
    testResponse = new TestExecutionReport();
    writeHead(testedPage);
    historyPage = testedPage;
    super.newTestStarted(testedPage, time);
  }

  @Override
  public void testComplete(WikiPage test, TestSummary testSummary) throws Exception {
    super.testComplete(test, testSummary);
    writeResults();
  }

  @Override
   public void allTestingComplete() throws Exception {
  }

  @Override
  protected WikiPage getPageForHistory() {
    return historyPage;
  }
}
