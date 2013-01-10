package fitnesse.responders.run;

import java.io.IOException;

import util.TimeMeasurement;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.WikiPage;
import fitnesse.FitNesseContext;
import fitnesse.responders.run.formatters.XmlFormatter;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TestSummary;

public class SuiteXmlFormatter extends XmlFormatter {

  private TestSummary xmlPageCounts = new TestSummary();

  public SuiteXmlFormatter(FitNesseContext context, WikiPage page, WriterFactory writerSource) {
    super(context, page, writerSource);
  }

  private void addFinalCounts() {
    testResponse.finalCounts = new TestSummary();
    finalSummary.right = testResponse.finalCounts.right = xmlPageCounts.getRight();
    finalSummary.wrong = testResponse.finalCounts.wrong = xmlPageCounts.getWrong();
    finalSummary.ignores = testResponse.finalCounts.ignores = xmlPageCounts.getIgnores();
    finalSummary.exceptions = testResponse.finalCounts.exceptions = xmlPageCounts.getExceptions();
  }

  @Override
  public void testComplete(TestPage testPage, TestSummary testSummary, TimeMeasurement timeMeasurement) {
    PageCrawler pageCrawler = getPage().getPageCrawler();
    String relativeName = pageCrawler.getRelativeName(getPage(), testPage.getSourcePage());
    if ("".equals(relativeName))
      relativeName = String.format("(%s)", testPage.getName());
    processTestResults(relativeName, testSummary, timeMeasurement);

    xmlPageCounts.tallyPageCounts(ExecutionResult.getExecutionResult(relativeName, testSummary));
  }

  @Override
  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws IOException {
    addFinalCounts();
    super.allTestingComplete(totalTimeMeasurement);
  }
}
