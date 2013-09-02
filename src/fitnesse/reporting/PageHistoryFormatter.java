package fitnesse.reporting;

import java.io.IOException;

import fitnesse.testrunner.WikiTestPage;
import fitnesse.FitNesseContext;
import fitnesse.testsystems.TestSummary;
import fitnesse.wiki.WikiPage;

public class PageHistoryFormatter extends XmlFormatter {

  public PageHistoryFormatter(FitNesseContext context, final WikiPage page, WriterFactory writerFactory) {
    super(context, page, writerFactory);
  }

  @Override
  public void testStarted(WikiTestPage testedPage) {
    testResponse = new TestExecutionReport();
    setPage(testedPage.getSourcePage());
    super.testStarted(testedPage);
  }

  @Override
  public void testComplete(WikiTestPage test, TestSummary testSummary) throws IOException {
    super.testComplete(test, testSummary);
    writeResults();
  }

  @Override
  public void close() {
    setTotalRunTimeOnReport(totalTimeMeasurement);
  }

}
