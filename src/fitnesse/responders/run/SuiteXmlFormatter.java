package fitnesse.responders.run;

import util.TimeMeasurement;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.WikiPage;
import fitnesse.FitNesseContext;
import fitnesse.responders.run.formatters.XmlFormatter;

public class SuiteXmlFormatter extends XmlFormatter {

  private TestSummary xmlPageCounts = new TestSummary();

  public SuiteXmlFormatter(FitNesseContext context, WikiPage page, WriterFactory writerSource) throws Exception {
    super(context, page, writerSource);
  }

  private void addFinalCounts() throws Exception {
    testResponse.finalCounts = new TestSummary();
    finalSummary.right = testResponse.finalCounts.right = xmlPageCounts.getRight();
    finalSummary.wrong = testResponse.finalCounts.wrong = xmlPageCounts.getWrong();
    finalSummary.ignores = testResponse.finalCounts.ignores = xmlPageCounts.getIgnores();
    finalSummary.exceptions = testResponse.finalCounts.exceptions = xmlPageCounts.getExceptions();
  }

  @Override
  public void testComplete(TestPage testPage, TestSummary testSummary, TimeMeasurement timeMeasurement)
      throws Exception {
    PageCrawler pageCrawler = getPage().getPageCrawler();
    String relativeName = pageCrawler.getRelativeName(getPage(), testPage.getSourcePage());
    if ("".equals(relativeName))
      relativeName = String.format("(%s)", testPage.getName());
    processTestResults(relativeName, testSummary, timeMeasurement);

    xmlPageCounts.tallyPageCounts(testSummary);
  }

  @Override
  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws Exception {
    addFinalCounts();
    super.allTestingComplete(totalTimeMeasurement);
  }
}
