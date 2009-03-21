package fitnesse.responders.run;

import org.w3c.dom.Element;

import util.XmlUtil;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.WikiPage;

public abstract class SuiteXmlFormatter extends XmlFormatter {

  private TestSummary xmlPageCounts = new TestSummary();

  public SuiteXmlFormatter(WikiPage page) throws Exception {
    super(page);
  }

  private void addFinalCounts() throws Exception {
    Element finalCounts = getDocument().createElement("finalCounts");
    getTestResultsElement().appendChild(finalCounts);
    XmlUtil.addTextNode(getDocument(), finalCounts, "right", Integer.toString(xmlPageCounts.right));
    XmlUtil.addTextNode(getDocument(), finalCounts, "wrong", Integer.toString(xmlPageCounts.wrong));
    XmlUtil.addTextNode(getDocument(), finalCounts, "ignores", Integer.toString(xmlPageCounts.ignores));
    XmlUtil.addTextNode(getDocument(), finalCounts, "exceptions", Integer.toString(xmlPageCounts.exceptions));
  }
  
  @Override
  public void allTestingComplete() throws Exception {
    addFinalCounts();
    super.allTestingComplete();
  }
  
  @Override
  public void processTestResults(WikiPage testPage, TestSummary testSummary)
      throws Exception {
    PageCrawler pageCrawler = getPage().getPageCrawler();
    String relativeName = pageCrawler.getRelativeName(getPage(), testPage);
    if ("".equals(relativeName))
      relativeName = String.format("(%s)", testPage.getName());
    processTestResults(relativeName, testSummary);

    xmlPageCounts.tallyPageCounts(testSummary);
  }
  


}
