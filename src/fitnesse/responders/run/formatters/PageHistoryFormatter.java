package fitnesse.responders.run.formatters;

import fitnesse.responders.run.TestPage;
import util.TimeMeasurement;
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
  public void newTestStarted(TestPage testedPage, TimeMeasurement timeMeasurement) throws Exception {
    testResponse = new TestExecutionReport();
    writeHead(testedPage.getSourcePage());
    historyPage = testedPage.getSourcePage();
    super.newTestStarted(testedPage, timeMeasurement);
  }

  @Override
  public void testComplete(TestPage test, TestSummary testSummary, TimeMeasurement timeMeasurement) throws Exception {
    super.testComplete(test, testSummary, timeMeasurement);
    writeResults();
  }

  @Override
  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws Exception {
    setTotalRunTimeOnReport(totalTimeMeasurement);
  }

  @Override
  protected WikiPage getPageForHistory() {
    return historyPage;
  }
}
