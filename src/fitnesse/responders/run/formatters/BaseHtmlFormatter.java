package fitnesse.responders.run.formatters;

import java.io.IOException;

import fitnesse.FitNesseContext;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.RawHtml;
import fitnesse.responders.run.CompositeExecutionLog;
import fitnesse.responders.run.TestPage;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.templateUtilities.HtmlPage;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiImportProperty;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageActions;
import fitnesse.wiki.WikiPagePath;

public abstract class BaseHtmlFormatter extends BaseFormatter {

  private static String TESTING_INTERUPTED = "<strong>Testing was interupted and results are incomplete.</strong>";

  private boolean wasInterupted = false;
  private TestSummary assertionCounts = new TestSummary();

  private HtmlPage htmlPage;

  protected BaseHtmlFormatter() {
    super();
  }

  protected BaseHtmlFormatter(FitNesseContext context, WikiPage page) {
    super(context, page);
  }

  protected abstract void writeData(String output);

  protected void updateSummaryDiv(String html) {
    writeData(HtmlUtil.makeReplaceElementScript("test-summary", html).html());
  }

  protected String getRelativeName(TestPage testPage) {
    PageCrawler pageCrawler = getPage().getPageCrawler();
    String relativeName = pageCrawler.getRelativeName(getPage(), testPage.getSourcePage());
    if ("".equals(relativeName)) {
      relativeName = String.format("(%s)", testPage.getName());
    }
    return relativeName;
  }

  protected void addStopLink(String stopResponderId) {
    String link = "?responder=stoptest&id=" + stopResponderId;

    HtmlTag status = HtmlUtil.makeSilentLink(link, new RawHtml("Stop Test"));
    status.addAttribute("class", "stop");
    
    writeData(HtmlUtil.makeReplaceElementScript("test-action", status.html()).html());
  }

  protected void removeStopTestLink() {
    HtmlTag script = HtmlUtil.makeReplaceElementScript("test-action", "");
    writeData(script.html());
  }


  protected String cssClassFor(TestSummary testSummary) {
    if (testSummary.getWrong() > 0 || wasInterupted)
      return "fail";
    else if (testSummary.getExceptions() > 0
      || testSummary.getRight() + testSummary.getIgnores() == 0)
      return "error";
    else if (testSummary.getIgnores() > 0 && testSummary.getRight() == 0)
      return "ignore";
    else
      return "pass";
  }

  @Override
  public void writeHead(String pageType) throws IOException {
    htmlPage = buildHtml(pageType);
    htmlPage.setMainTemplate("testPage");
    htmlPage.divide();
    writeData(htmlPage.preDivision);
  }

  protected HtmlPage buildHtml(String pageType) {
    PageCrawler pageCrawler = getPage().getPageCrawler();
    WikiPagePath fullPath = pageCrawler.getFullPath(getPage());
    String fullPathName = PathParser.render(fullPath);
    HtmlPage html = context.pageFactory.newPage();
    html.setTitle(pageType + ": " + fullPathName);
    html.setPageTitle(new PageTitle(pageType, fullPath));
    html.setNavTemplate("wikiNav.vm");
    html.put("actions", new WikiPageActions(getPage()).withPageHistory());
    html.setFooterTemplate("wikiFooter.vm");
    html.put("footerContent", new WikiPageFooterRenderer());

    WikiImportProperty.handleImportProperties(html, getPage(), getPage().getData());
    return html;
  }

  public class WikiPageFooterRenderer {
    public String render() {
        return getPage().getData().getFooterPageHtml();
    }
  }

  public TestSummary getAssertionCounts() {
    return assertionCounts;
  }

  public boolean wasInterupted() {
    return wasInterupted;
  }
  
  @Override
  public void errorOccured() {
    wasInterupted = true;
    super.errorOccured();
  }
  
  public String testSummary() {
    String summaryContent = (wasInterupted()) ? TESTING_INTERUPTED : "";
    summaryContent += makeSummaryContent();
    HtmlTag script = HtmlUtil.makeReplaceElementScript("test-summary", summaryContent);
    script.add("document.getElementById(\"test-summary\").className = \""
      + cssClassFor(getAssertionCounts()) + "\";");
    return script.html();
  }

  protected abstract String makeSummaryContent();

  protected void finishWritingOutput() throws IOException {
    writeData(htmlPage.postDivision);
  }

  protected void close() {
  }
  
  public String executionStatus(CompositeExecutionLog logs) {
    return logs.executionStatusHtml();
  }
}
