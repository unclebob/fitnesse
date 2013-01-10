package fitnesse.responders.run.formatters;

import java.io.IOException;

import fitnesse.responders.run.TestPage;
import util.TimeMeasurement;
import fitnesse.FitNesseContext;
import fitnesse.responders.run.TestExecutionReport;
import fitnesse.testsystems.TestSummary;
import fitnesse.wiki.WikiPage;

public class PageHistoryFormatter extends XmlFormatter {

  public PageHistoryFormatter(FitNesseContext context, final WikiPage page, WriterFactory writerFactory) {
    super(context, page, writerFactory);
  }

  @Override
  public void newTestStarted(TestPage testedPage, TimeMeasurement timeMeasurement) {
    testResponse = new TestExecutionReport();
    setPage(testedPage.getSourcePage());
    super.newTestStarted(testedPage, timeMeasurement);
  }

  @Override
  public void testComplete(TestPage test, TestSummary testSummary, TimeMeasurement timeMeasurement) throws IOException {
    super.testComplete(test, testSummary, timeMeasurement);
    writeResults();
  }

  @Override
  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) {
    setTotalRunTimeOnReport(totalTimeMeasurement);
  }

}
