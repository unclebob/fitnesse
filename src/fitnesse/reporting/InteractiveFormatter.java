package fitnesse.reporting;

import java.io.IOException;

import fitnesse.testrunner.WikiTestPage;
import fitnesse.FitNesseContext;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.RawHtml;
import fitnesse.testrunner.CompositeExecutionLog;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.WikiPage;

public abstract class InteractiveFormatter extends BaseFormatter {

  private static final String TESTING_INTERRUPTED = "<strong>Testing was interrupted and results are incomplete.</strong>&nbsp;";

  private boolean wasInterrupted = false;
  private TestSummary assertionCounts = new TestSummary();

  private CompositeExecutionLog log;

  private String relativeName;

  protected InteractiveFormatter(FitNesseContext context, WikiPage page) {
    super(context, page);
  }

  protected abstract void writeData(String output);

  protected void updateSummaryDiv(String html) {
    writeData(HtmlUtil.makeReplaceElementScript("test-summary", html).html());
  }

  protected String getRelativeName() {
	  return relativeName;
  }

  protected String getRelativeName(WikiTestPage testPage) {
    PageCrawler pageCrawler = getPage().getPageCrawler();
    String relativeName = pageCrawler.getRelativeName(testPage.getSourcePage());
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

  public TestSummary getAssertionCounts() {
    return assertionCounts;
  }

  @Override
  public int getErrorCount() {
    return getAssertionCounts().getWrong() + getAssertionCounts().getExceptions();
  }

  public boolean wasInterrupted() {
    return wasInterrupted;
  }

  @Override
  public void errorOccurred(Throwable cause) {
    wasInterrupted = true;
    super.errorOccurred(cause);
  }

  @Override
  public void testStarted(WikiTestPage testPage) {
    relativeName = getRelativeName(testPage);
  }

  public String testSummary() {
    String summaryContent = (wasInterrupted()) ? TESTING_INTERRUPTED : "";
    summaryContent += makeSummaryContent();
    HtmlTag script = HtmlUtil.makeReplaceElementScript("test-summary", summaryContent);
    script.add("document.getElementById(\"test-summary\").className = \""
      + ExecutionResult.getExecutionResult(relativeName, getAssertionCounts()) + "\";");
    return script.html();
  }

  protected abstract String makeSummaryContent();

  public void finishWritingOutput() throws IOException {
    writeData(testSummary());
  }

  @Override
  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) {
    this.log = log;
    addStopLink(stopResponderId);
  }

  protected void publishAndAddLog() throws IOException {
    if (log != null) {
      log.publish(context.pageFactory);
      writeData(HtmlUtil.makeReplaceElementScript("test-action", executionStatus(log)).html());
    }
  }

  protected void maybeMakeErrorNavigatorVisible(){
    if(exceptionsOrErrorsExist()){
      writeData(makeErrorNavigatorVisible());
      writeData(initErroMetadata());
    }
  }

  private boolean exceptionsOrErrorsExist() {
	return (assertionCounts.getExceptions() + assertionCounts.getWrong()) > 0;
  }

  public String executionStatus(CompositeExecutionLog log) {
    String errorLogPageName = log.getErrorLogPageName();
    if (log.exceptionCount() != 0)
      return makeExecutionStatusLink(errorLogPageName, ExecutionStatus.ERROR);

    if (log.hasCapturedOutput())
      return makeExecutionStatusLink(errorLogPageName, ExecutionStatus.OUTPUT);

    return makeExecutionStatusLink(errorLogPageName, ExecutionStatus.OK);
  }

  private String makeErrorNavigatorVisible() {
    HtmlTag toggler = HtmlUtil.makeToggleClassScript("error-nav", "error-nav-hidden");
    return toggler.html();
  }

  private String initErroMetadata() {
    HtmlTag init = HtmlUtil.makeInitErrorMetadataScript();
    return init.html();
  }

  public static String makeExecutionStatusLink(String linkHref, ExecutionStatus executionStatus) {
    HtmlTag status = HtmlUtil.makeLink(linkHref, executionStatus.getMessage());
    status.addAttribute("class", executionStatus.getStyle());
    return status.html();
  }

}
