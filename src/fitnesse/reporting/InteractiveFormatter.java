package fitnesse.reporting;

import java.io.IOException;
import java.io.Writer;

import fitnesse.testrunner.TestsRunnerListener;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.RawHtml;
import fitnesse.testrunner.WikiTestPageUtil;
import fitnesse.testsystems.*;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.WikiPage;

public abstract class InteractiveFormatter extends BaseFormatter implements TestsRunnerListener {

  private static final String TESTING_INTERRUPTED = "<strong>Testing was interrupted and results are incomplete.</strong>&nbsp;";
  private final Writer writer;

  private boolean wasInterrupted = false;
  private TestSummary assertionCounts = new TestSummary();

  private String relativeName;

  protected InteractiveFormatter(WikiPage page, Writer writer) {
    super(page);
    this.writer = writer;
  }

  protected void writeData(String output) {
    try {
      writer.write(output);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void updateSummaryDiv(String html) {
    writeData(JavascriptUtil.makeReplaceElementScript("test-summary", html).html());
  }

  protected String getRelativeName() {
	  return relativeName;
  }

  protected String getRelativeName(TestPage testPage) {
    PageCrawler pageCrawler = getPage().getPageCrawler();
    String relativeName = pageCrawler.getRelativeName(WikiTestPageUtil.getSourcePage(testPage));
    if ("".equals(relativeName)) {
      relativeName = String.format("(%s)", testPage.getName());
    }
    return relativeName;
  }

  protected void addStopLink(String stopResponderId) {
    String link = "?responder=stoptest&id=" + stopResponderId;

    HtmlTag status = JavascriptUtil.makeSilentLink(link, new RawHtml("Stop Test"));
    status.addAttribute("class", "stop");

    writeData(JavascriptUtil.makeReplaceElementScript("test-action", status.html()).html());
  }

  protected void removeStopTestLink() {
    HtmlTag script = JavascriptUtil.makeReplaceElementScript("test-action", "");
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

  private void errorOccurred(Throwable cause) {
    wasInterrupted = true;

    writeData(String.format("<span class=\"error\">Could not complete testing: %s</span>", cause.toString()));
  }

  @Override
  public void testSystemStopped(TestSystem testSystem, Throwable cause) {
    if (cause != null) {
      errorOccurred(cause);
    }
  }

  @Override
  public void testStarted(TestPage testPage) {
    relativeName = getRelativeName(testPage);
  }

  public String testSummary() {
    String summaryContent = wasInterrupted ? TESTING_INTERRUPTED : "";
    summaryContent += makeSummaryContent();
    HtmlTag script = JavascriptUtil.makeReplaceElementScript("test-summary", summaryContent);
    script.add("document.getElementById(\"test-summary\").className = \""
      + (wasInterrupted ? ExecutionResult.ERROR : ExecutionResult.getExecutionResult(relativeName, getAssertionCounts())) + "\";");
    return script.html();
  }

  protected abstract String makeSummaryContent();

  public void finishWritingOutput() {
    writeData(testSummary());
  }

  @Override
  public void announceNumberTestsToRun(int testsToRun) {
  }

  @Override
  public void unableToStartTestSystem(String testSystemName, Throwable cause) {
    writeData(String.format("<span class=\"error\">Unable to start test system '%s': %s</span>", testSystemName, cause.toString()));
  }

  public void setTrackingId(String stopResponderId) {
    addStopLink(stopResponderId);
  }

  protected void AddLogLink() {
    writeData(JavascriptUtil.makeReplaceElementScript("test-action", executionStatus()).html());
  }

  protected void maybeMakeErrorNavigatorVisible() {
    if(exceptionsOrErrorsExist()){
      writeData(initErroMetadata());
    }
  }

  private boolean exceptionsOrErrorsExist() {
	return (assertionCounts.getExceptions() + assertionCounts.getWrong()) > 0;
  }

  public String executionStatus() {
    if (wasInterrupted)
      return makeExecutionStatusLink(ExecutionStatus.ERROR);

    return makeExecutionStatusLink(ExecutionStatus.OK);
  }

  private String initErroMetadata() {
    HtmlTag init = JavascriptUtil.makeInitErrorMetadataScript();
    return init.html();
  }

  public static String makeExecutionStatusLink(ExecutionStatus executionStatus) {
    HtmlTag status = HtmlUtil.makeLink("?executionLog", "Execution Log");
    status.addAttribute("class", executionStatus.getStyle());
    return status.html();
  }

}
