// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.formatters;

import java.io.IOException;

import fitnesse.responders.PageFactory;
import fitnesse.responders.run.TestPage;
import util.TimeMeasurement;
import fitnesse.FitNesseContext;
import fitnesse.responders.run.CompositeExecutionLog;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestSystem;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public abstract class SuiteHtmlFormatter extends BaseHtmlFormatter {
  private TestSummary pageCounts = new TestSummary();
  private static final String TEST_SUMMARIES_ID = "test-summaries";

  private int currentTest = 0;
  private String testSystemFullName = null;
  private boolean printedTestOutput = false;
  private int totalTests = 1;
  private TimeMeasurement latestTestTime;
  private CompositeExecutionLog log;


  public SuiteHtmlFormatter(FitNesseContext context, WikiPage page) {
    super(context, page);
  }

  public SuiteHtmlFormatter(FitNesseContext context) {
    super(context, null);
  }

  
  public String getTestSystemHeader(String testSystemName) {
    String tag = String.format("<h3>%s</h3>\n", testSystemName);
    HtmlTag insertScript = HtmlUtil.makeAppendElementScript("test_summaries", tag);
    return insertScript.html();
  }

  @Override
  public void announceNumberTestsToRun(int testsToRun) {
    super.announceNumberTestsToRun(testsToRun);
    totalTests = (testsToRun != 0) ? testsToRun : 1;
  }

  public void announceStartNewTest(String relativeName, String fullPathName) {
    currentTest++;
    updateSummaryDiv(getProgressHtml());

    maybeWriteTestOutputDiv();
    maybeWriteTestSystem();
    writeTestOuputDiv(relativeName, fullPathName);
  }

  private void writeTestOuputDiv(String relativeName, String fullPathName) {
    HtmlTag pageNameBar = HtmlUtil.makeDivTag("test_output_name");
    HtmlTag anchor = HtmlUtil.makeLink(fullPathName, relativeName);
    anchor.addAttribute("id", relativeName + currentTest);
    anchor.addAttribute("class", "test_name");

    HtmlTag topLink = HtmlUtil.makeLink("#" + TEST_SUMMARIES_ID, "Top");
    topLink.addAttribute("class", "top_of_page");

    pageNameBar.add(anchor);
    pageNameBar.add(topLink);
    writeData(pageNameBar.html());

    writeData("<div class=\"alternating_block\">");
  }

  private void maybeWriteTestOutputDiv() {
    if (!printedTestOutput) {
      HtmlTag outputTitle = new HtmlTag("h2", "Test Output");
      writeData(outputTitle.html());
      printedTestOutput = true;
    }
  }
  
  private void maybeWriteTestSystem() {
    if (testSystemFullName != null) {
      HtmlTag systemTitle = new HtmlTag("h2", String.format("Test System: %s", testSystemFullName));
      writeData(systemTitle.html());
      // once we write it out we don't need it any more
      testSystemFullName = null;
    }
  }


  @Override
  public void newTestStarted(TestPage newTest, TimeMeasurement timeMeasurement) {
    String relativeName = getRelativeName(newTest);
    
    PageCrawler pageCrawler = getPage().getPageCrawler();
    WikiPagePath fullPath = pageCrawler.getFullPath(newTest.getSourcePage());
    String fullPathName = PathParser.render(fullPath);

    announceStartNewTest(relativeName, fullPathName);
  }

  private String getProgressHtml() {
    float percentFinished = (currentTest - 1) * 1000 / totalTests;
    percentFinished = percentFinished / 10;

    String text = "Running tests ... (" + currentTest + "/" + totalTests + ")";
    text = text.replaceAll(" ", "&nbsp;");
    HtmlTag progressDiv = new HtmlTag("div", text);

    // need some results before we can check pageCounts for results
    String cssClass = (currentTest == 1) ? "pass" : cssClassFor(this.pageCounts);
    progressDiv.addAttribute("id", "progressBar");
    progressDiv.addAttribute("class", cssClass);
    progressDiv.addAttribute("style", "width:" + percentFinished + "%");

    return progressDiv.html();
  }

  public void processTestResults(String relativeName, TestSummary testSummary) throws IOException {
    finishOutputForTest();

    getAssertionCounts().add(testSummary);

    HtmlTag mainDiv = HtmlUtil.makeDivTag("alternating_row");

    mainDiv.add(HtmlUtil.makeSpanTag("test_summary_results " + cssClassFor(testSummary), testSummary.toString()));

    HtmlTag link = HtmlUtil.makeLink("#" + relativeName + currentTest, relativeName);
    link.addAttribute("class", "test_summary_link");
    mainDiv.add(link);
    
    if (latestTestTime != null) {
      mainDiv.add(HtmlUtil.makeSpanTag("", String.format("(%.03f seconds)", latestTestTime.elapsedSeconds())));
    }

    pageCounts.tallyPageCounts(testSummary);
    HtmlTag insertScript = HtmlUtil.makeAppendElementScript(TEST_SUMMARIES_ID, mainDiv.html(2));
    writeData(insertScript.html());
  }

  private void finishOutputForTest() {
    writeData("</div>" + HtmlTag.endl);
  }
  
  @Override
  public void writeHead(String pageType) throws IOException {
    super.writeHead(pageType);
    writeTestSummaries();
  }
  
  @Override
  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws IOException {
    latestTestTime = totalTimeMeasurement;
    removeStopTestLink();
    publishAndAddLog();
    finishWritingOutput();
    close();

    super.allTestingComplete(totalTimeMeasurement);
  }

  protected void publishAndAddLog() throws IOException {
    if (log != null) {
      log.publish();
      writeData(HtmlUtil.makeReplaceElementScript("test-action", executionStatus(log)).html());
    }
  }

  @Override
  public void testOutputChunk(String output) throws IOException {
    writeData(output);
  }

  @Override
  public void testComplete(TestPage testPage, TestSummary testSummary, TimeMeasurement timeMeasurement) throws IOException {
    super.testComplete(testPage, testSummary, timeMeasurement);
    latestTestTime = timeMeasurement;

    processTestResults(getRelativeName(testPage), testSummary);
  }

  @Override
  public void errorOccured() {
    latestTestTime = null;
    super.errorOccured();
  }

  @Override
  public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) {
    testSystemFullName = (testSystemName + ":" + testRunner).replaceAll("\\\\", "/");
    String tag = String.format("<h3>%s</h3>\n", testSystemFullName);
    HtmlTag insertScript = HtmlUtil.makeAppendElementScript(TEST_SUMMARIES_ID, tag);
    writeData(insertScript.html());

  }

  protected String makeSummaryContent() {
    String summaryContent = "<strong>Test Pages:</strong> " + pageCounts.toString() + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
    if (latestTestTime != null) {
      summaryContent += String.format("<strong>Assertions:</strong> %s (%.03f seconds)", getAssertionCounts(), latestTestTime.elapsedSeconds());
    } else {
      summaryContent += String.format("<strong>Assertions:</strong> %s ", getAssertionCounts());
    }
    return summaryContent;
  }

  @Override
  public void finishWritingOutput() throws IOException {
    writeData(testSummary());
    super.finishWritingOutput();
  }
  
  @Override
  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) {
    this.log = log;
    addStopLink(stopResponderId);
  }

  private void writeTestSummaries() {
    HtmlTag outputTitle = new HtmlTag("h2", "Test Summaries");

    HtmlTag summariesDiv = HtmlUtil.makeDivTag(TEST_SUMMARIES_ID);
    summariesDiv.addAttribute("id", TEST_SUMMARIES_ID);
    summariesDiv.add(outputTitle);
    writeData(summariesDiv.html());
  }
}



