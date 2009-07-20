package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.wiki.WikiPage;

public class PageHistoryFormatter extends XmlFormatter {
  private WikiPage historyPage;

  public PageHistoryFormatter(FitNesseContext context, final WikiPage page, WriterFactory writerSource) throws Exception {
    super(context, page, writerSource);
  }

  @Override
  public void newTestStarted(WikiPage testedPage) throws Exception {
    testResponse = new TestExecutionReport();
    historyPage = testedPage;
    super.newTestStarted(testedPage);
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
