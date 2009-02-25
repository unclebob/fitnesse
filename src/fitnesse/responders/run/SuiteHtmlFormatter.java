package fitnesse.responders.run;

import fitnesse.html.HtmlPageFactory;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.TagGroup;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public abstract class SuiteHtmlFormatter extends TestHtmlFormatter {

  private static final String cssSuffix1 = "1";
  private static final String cssSuffix2 = "2";

  private TestSummary pageCounts = new TestSummary();

  private String cssSuffix = cssSuffix1;
  private TagGroup testResultsGroup = new TagGroup();
  private HtmlTag currentOutputDiv;
  private int pageNumber = 0;

  public SuiteHtmlFormatter(WikiPage page, HtmlPageFactory pageFactory) throws Exception {
    super(page, pageFactory);
    
    HtmlTag outputTitle = new HtmlTag("h2", "Test Output");
    outputTitle.addAttribute("class", "centered");
    testResultsGroup.add(outputTitle);
  }
  
  public void announceTestSystem(String testSystemName) {
    HtmlTag outputTitle = new HtmlTag("h2", String.format("Test System: %s", testSystemName));
    outputTitle.addAttribute("class", "centered");
    testResultsGroup.add(outputTitle);
  }

  public String getTestSystemHeader(String testSystemName) {
    return String.format("<h3>%s</h3>\n", testSystemName);
  }
  
  public void announceStartNewTest(String relativeName, String fullPathName) {
    pageNumber++;
    HtmlTag pageNameBar = HtmlUtil.makeDivTag("test_output_name");
    HtmlTag anchor = HtmlUtil.makeLink(fullPathName, relativeName);
    anchor.addAttribute("id", relativeName + pageNumber);
    pageNameBar.add(anchor);
    testResultsGroup.add(pageNameBar);
    currentOutputDiv = HtmlUtil.makeDivTag("alternating_block_" + cssSuffix);
    testResultsGroup.add(currentOutputDiv);    
  }
  
  @Override
  public void announceStartNewTest(WikiPage newTest) throws Exception {
    PageCrawler pageCrawler = getPage().getPageCrawler();
    String relativeName = pageCrawler.getRelativeName(getPage(), newTest);
    WikiPagePath fullPath = pageCrawler.getFullPath(newTest);
    String fullPathName = PathParser.render(fullPath);
    
    announceStartNewTest(relativeName, fullPathName);
  }
  
  @Override
  public void processTestOutput(String output) throws Exception {
    currentOutputDiv.add(output);
  }
  
  public void processTestResults(String relativeName, TestSummary testSummary) throws Exception {
    getAssertionCounts().tally(testSummary);
    
    switchCssSuffix();
    HtmlTag mainDiv = HtmlUtil.makeDivTag("alternating_row_" + cssSuffix);

    mainDiv.add(HtmlUtil.makeSpanTag("test_summary_results " + cssClassFor(testSummary), testSummary.toString()));

    HtmlTag link = HtmlUtil.makeLink("#" + relativeName + pageNumber, relativeName);
    link.addAttribute("class", "test_summary_link");
    mainDiv.add(link);

    pageCounts.tallyPageCounts(testSummary);

    writeData(mainDiv.html(2));
  }
  
  @Override
  public void processTestResults(WikiPage testPage, TestSummary testSummary)
      throws Exception {
    PageCrawler pageCrawler = getPage().getPageCrawler();
    String relativeName = pageCrawler.getRelativeName(getPage(), testPage);
    if ("".equals(relativeName)) {
      relativeName = String.format("(%s)", testPage.getName());
    }
    
    processTestResults(relativeName, testSummary);
  }

  private void switchCssSuffix() {
    if (cssSuffix1.equals(cssSuffix))
      cssSuffix = cssSuffix2;
    else
      cssSuffix = cssSuffix1;
  }
  
  @Override
  public void announceStartTestSystem(String testSystemName, String testRunner)
      throws Exception {
    HtmlTag outputTitle = new HtmlTag("h2", String.format("Test System: %s", testSystemName));
    outputTitle.addAttribute("class", "centered");
    testResultsGroup.add(outputTitle);
    
    writeData(String.format("<h3>%s</h3>\n", testSystemName  + ":" + testRunner));
  }

  public void finishWritingOutput() throws Exception {
    String pageCountsSummary = "<strong>Test Pages:</strong> " + pageCounts.toString() + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
    writeData(pageCountsSummary);
    writeData(testSummary(getAssertionCounts()));
    writeData(testResultsGroup.html());
    writeData(getHtmlPage().postDivision);
  }
}



