package fitnesse.responders.run;

import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlPageFactory;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.TagGroup;
import fitnesse.responders.WikiImportProperty;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public abstract class TestHtmlFormatter extends BaseFormatter {

  private final HtmlPageFactory pageFactory;
  private final TestSummary assertionCounts = new TestSummary();
  private CompositeExecutionLog log = null;
  private HtmlPage htmlPage = null;

  
  public TestHtmlFormatter(final WikiPage page, final HtmlPageFactory pageFactory) throws Exception {
    super(page);
    this.pageFactory = pageFactory;
  }
  
  protected abstract void writeData(String output) throws Exception;

  @Override
  public void writeHead(String pageType) throws Exception {
    htmlPage = buildHtml(pageType);
    htmlPage.main.use(HtmlPage.BreakPoint);
    htmlPage.divide();
    writeData(htmlPage.preDivision + makeSummaryPlaceHolder().html());
  }

  private HtmlTag makeSummaryPlaceHolder() {
    HtmlTag testSummaryDiv = new HtmlTag("div", "Running Tests ...");
    testSummaryDiv.addAttribute("id", "test-summary");

    return testSummaryDiv;
  }

  protected String testPageSummary() {
    return "";
  }

  @Override
  public void announceStartNewTest(WikiPage test) throws Exception {
    writeData(HtmlUtil.getHtmlOfInheritedPage("PageHeader", getPage()));
  }

  @Override
  public void announceStartTestSystem(String testSystemName, String testRunner)
      throws Exception {
  }

  @Override
  public void processTestOutput(String output) throws Exception {
    writeData(output);
  }

  @Override
  public void processTestResults(WikiPage test, TestSummary testSummary)
      throws Exception {
    getAssertionCounts().tally(testSummary);
  }

  @Override
  public void setExecutionLog(CompositeExecutionLog log) {
    this.log = log;
  }
  
  protected HtmlPage buildHtml(String pageType) throws Exception {
    PageCrawler pageCrawler = getPage().getPageCrawler();
    WikiPagePath fullPath = pageCrawler.getFullPath(getPage());
    String fullPathName = PathParser.render(fullPath);
    HtmlPage html = pageFactory.newPage();
    html.title.use(pageType + ": " + fullPathName);
    html.header.use(HtmlUtil.makeBreadCrumbsWithPageType(fullPathName, pageType));
    PageData data = getPage().getData();
    html.actions.use(HtmlUtil.makeActions(data));
    WikiImportProperty.handleImportProperties(html, getPage(), data);
    return html;
  }

  @Override
  public void allTestingComplete() throws Exception {
      publishAndAddLog();
      finishWritingOutput();
      close();
  }
  
  protected void close() throws Exception {
  }
  
  protected void finishWritingOutput() throws Exception {
    writeData(HtmlUtil.getHtmlOfInheritedPage("PageFooter", getPage()));
    writeData(htmlPage.postDivision);
  }

  protected void publishAndAddLog() throws Exception {
    writeData(testSummary(getAssertionCounts()));
    if (log != null) {
      log.publish();
      writeData(executionStatus(log));
    }
  }
  
  protected String cssClassFor(TestSummary testSummary) {
    if (testSummary.wrong > 0)
      return "fail";
    else if (testSummary.exceptions > 0 || testSummary.right + testSummary.ignores == 0)
      return "error";
    else if (testSummary.ignores > 0 && testSummary.right == 0)
      return "ignore";
    else
      return "pass";
  }
  
  public String executionStatus(CompositeExecutionLog logs) throws Exception {
    return logs.executionStatusHtml();
  }
  
  public String testSummary(TestSummary testSummary) throws Exception {
    String summaryContent = testPageSummary();
    summaryContent += "<strong>Assertions:</strong> " + testSummary.toString();
    HtmlTag script = new HtmlTag("script");
    script.add("document.getElementById(\"test-summary\").innerHTML = \"" + summaryContent + "\";");
    script.add("document.getElementById(\"test-summary\").className = \"" + cssClassFor(testSummary) + "\";");
    return script.html();
  }
  
  protected int exitCode() {
    return getAssertionCounts().wrong + getAssertionCounts().exceptions;
  }
  
  public void addMessageForBlankHtml() throws Exception {
    TagGroup html = new TagGroup();
    HtmlTag h2 = new HtmlTag("h2");
    h2.addAttribute("class", "centered");
    h2.add("Oops!  Did you forget to add to some content to this ?");
    html.add(h2.html());
    html.add(HtmlUtil.HR.html());
    writeData(html.html());
  }

  public TestSummary getAssertionCounts() {
    return assertionCounts;
  }

  public HtmlPage getHtmlPage() throws Exception {
    if (htmlPage != null) {
      return htmlPage;
    }
    return buildHtml("");
  }

}
