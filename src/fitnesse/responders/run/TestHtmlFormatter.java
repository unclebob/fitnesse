// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.html.*;
import fitnesse.responders.WikiImportProperty;
import fitnesse.wiki.*;

public abstract class TestHtmlFormatter extends BaseFormatter {
  private HtmlPageFactory pageFactory;
  private TestSummary assertionCounts = new TestSummary();
  private CompositeExecutionLog log = null;
  private HtmlPage htmlPage = null;
  private boolean wasInterupted = false;

  private static String TESTING_INTERUPTED = "<strong>Testing was interupted and results are incomplete.</strong><br/>";

  public TestHtmlFormatter(FitNesseContext context, final WikiPage page,
                           final HtmlPageFactory pageFactory) throws Exception {
    super(context, page);
    this.pageFactory = pageFactory;
  }

  //special constructor for TestRunner.  Used only for formatting.
  //todo this is nasty coupling. 
  public TestHtmlFormatter(FitNesseContext context) {
    super(context, null);
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

  protected void updateSummaryDiv(String html) throws Exception {
    writeData(HtmlUtil.makeReplaceElementScript("test-summary", html).html());
  }

  protected String testPageSummary() {
    return "";
  }

  public void newTestStarted(WikiPage test) throws Exception {
    writeData(getPage().getData().getHeaderPageHtml());
  }

  public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner)
    throws Exception {
  }

  public void testOutputChunk(String output) throws Exception {
    writeData(output);
  }

  public void testComplete(WikiPage test, TestSummary testSummary)
    throws Exception {
    getAssertionCounts().add(testSummary);
  }

  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) throws Exception {
    this.log = log;
    addStopLink(stopResponderId);
  }

  private void addStopLink(String stopResponderId) throws Exception {
    String link = "?responder=stoptest&id=" + stopResponderId;

    HtmlTag status = new HtmlTag("div");
    status.addAttribute("id", "stop-test");
    HtmlTag image = new HtmlTag("img");
    image.addAttribute("src", "/files/images/stop.gif");

    status.add(HtmlUtil.makeSilentLink(link, image));
    status.add(HtmlUtil.BR);

    status.add(HtmlUtil.makeSilentLink(link, new RawHtml("Stop Test")));
    writeData(status.html());
  }

  private void removeStopTestLink() throws Exception {
    HtmlTag script = HtmlUtil.makeReplaceElementScript("stop-test", "");
    writeData(script.html());
  }

  protected HtmlPage buildHtml(String pageType) throws Exception {
    PageCrawler pageCrawler = getPage().getPageCrawler();
    WikiPagePath fullPath = pageCrawler.getFullPath(getPage());
    String fullPathName = PathParser.render(fullPath);
    HtmlPage html = pageFactory.newPage();
    html.title.use(pageType + ": " + fullPathName);
    html.header.use(HtmlUtil
      .makeBreadCrumbsWithPageType(fullPathName, pageType));
    PageData data = getPage().getData();
    html.actions.use(HtmlUtil.makeActions(getPage().getActions()));
    WikiImportProperty.handleImportProperties(html, getPage(), data);
    return html;
  }

  @Override
  public int allTestingComplete() throws Exception {
    removeStopTestLink();
    publishAndAddLog();
    finishWritingOutput();
    close();
    return exitCode();
  }

  protected void close() throws Exception {
  }

  protected void finishWritingOutput() throws Exception {
    writeData(getPage().getData().getFooterPageHtml());
    writeData(htmlPage.postDivision);
  }

  protected void publishAndAddLog() throws Exception {
    writeData(testSummary());
    if (log != null) {
      log.publish();
      writeData(executionStatus(log));
    }
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

  public String executionStatus(CompositeExecutionLog logs) throws Exception {
    return logs.executionStatusHtml();
  }

  protected String makeSummaryContent() {
    String summaryContent = testPageSummary();
    summaryContent += "<strong>Assertions:</strong> " + assertionCounts.toString();
    return summaryContent;
  }

  public String testSummary() throws Exception {
    String summaryContent = (wasInterupted) ? TESTING_INTERUPTED : "";
    summaryContent += makeSummaryContent();
    HtmlTag script = HtmlUtil.makeReplaceElementScript("test-summary", summaryContent);
    script.add("document.getElementById(\"test-summary\").className = \""
      + cssClassFor(assertionCounts) + "\";");
    return script.html();
  }

  protected int exitCode() {
    return getAssertionCounts().getWrong() + getAssertionCounts().getExceptions();
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

  @Override
  public void errorOccured() {
    wasInterupted = true;
    super.errorOccured();
  }
}
