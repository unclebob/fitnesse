package fitnesse.responders.run;

import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.WikiPage;
import fitnesse.FitNesseContext;

import java.io.Writer;

public class SuiteXmlFormatter extends XmlFormatter {

  private TestSummary xmlPageCounts = new TestSummary();

  public SuiteXmlFormatter(FitNesseContext context, WikiPage page, WriterSource writerSource) throws Exception {
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
  public void testComplete(WikiPage testPage, TestSummary testSummary)
      throws Exception {
    PageCrawler pageCrawler = getPage().getPageCrawler();
    String relativeName = pageCrawler.getRelativeName(getPage(), testPage);
    if ("".equals(relativeName))
      relativeName = String.format("(%s)", testPage.getName());
    processTestResults(relativeName, testSummary);

    xmlPageCounts.tallyPageCounts(testSummary);
  }

  @Override
  public int allTestingComplete() throws Exception {
    addFinalCounts();
    return super.allTestingComplete();
  }


}
