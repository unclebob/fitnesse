// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.formatters;

import static fitnesse.testsystems.ExecutionResult.getExecutionResult;

import java.io.IOException;

import util.TimeMeasurement;
import fitnesse.FitNesseContext;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.responders.run.TestPage;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public abstract class SuiteHtmlFormatter extends InteractiveFormatter {
  private TestSummary pageCounts = new TestSummary();
  private static final String TEST_SUMMARIES_ID = "test-summaries";

  private int currentTest = 0;
  private String testSystemFullName = null;
  private boolean printedTestOutput = false;
  private int totalTests = 1;
  private TimeMeasurement latestTestTime;
  private String testSummariesId = TEST_SUMMARIES_ID;


  public SuiteHtmlFormatter(FitNesseContext context, WikiPage page) {
    super(context, page);
  }

  public SuiteHtmlFormatter(FitNesseContext context) {
    super(context, null);
  }

  @Override
  public void announceNumberTestsToRun(int testsToRun) {
    super.announceNumberTestsToRun(testsToRun);
    totalTests = (testsToRun != 0) ? testsToRun : 1;
  }

  public void announceStartNewTest(String relativeName, String fullPathName) {
    currentTest++;
    updateSummaryDiv(getProgressHtml(relativeName));

    maybeWriteTestOutputDiv();
    maybeWriteTestSystem();
    writeTestOuputDiv(relativeName, fullPathName);
  }

  private void writeTestOuputDiv(String relativeName, String fullPathName) {
    HtmlTag pageNameBar = HtmlUtil.makeDivTag("test_output_name");
    HtmlTag anchor = HtmlUtil.makeLink(fullPathName, relativeName);
    anchor.addAttribute("id", relativeName + currentTest);
    anchor.addAttribute("class", "test_name");
    HtmlTag title = new HtmlTag("h3", anchor);

    HtmlTag topLink = HtmlUtil.makeLink("#" + TEST_SUMMARIES_ID, "Top");
    topLink.addAttribute("class", "top_of_page");

    pageNameBar.add(title);
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
  public void newTestStarted(TestPage testPage, TimeMeasurement timeMeasurement) throws IOException {
    super.newTestStarted(testPage, timeMeasurement);

    PageCrawler pageCrawler = getPage().getPageCrawler();
    WikiPagePath fullPath = pageCrawler.getFullPath(testPage.getSourcePage());
    String fullPathName = PathParser.render(fullPath);

    announceStartNewTest(getRelativeName(), fullPathName);
  }

  private String getProgressHtml(String relativeName) {
    float percentFinished = (currentTest - 1) * 1000 / totalTests;
    percentFinished = percentFinished / 10;

    String text = "Running tests ... (" + currentTest + "/" + totalTests + ")";
    text = text.replaceAll(" ", "&nbsp;");
    HtmlTag progressDiv = new HtmlTag("div", text);

    // need some results before we can check pageCounts for results
    ExecutionResult cssClass = (currentTest == 1) ? ExecutionResult.PASS : getExecutionResult(relativeName, this.pageCounts);
    progressDiv.addAttribute("id", "progressBar");
    progressDiv.addAttribute("class", cssClass.toString());
    progressDiv.addAttribute("style", "width:" + percentFinished + "%");

    return progressDiv.html();
  }

  public void processTestResults(String relativeName, TestSummary testSummary) throws IOException {
    finishOutputForTest();

    getAssertionCounts().add(testSummary);

    HtmlTag tag = new HtmlTag("li");

    tag.add(HtmlUtil.makeSpanTag("results " + getExecutionResult(relativeName, testSummary), testSummary.toString()));

    HtmlTag link = HtmlUtil.makeLink("#" + relativeName + currentTest, relativeName);
    link.addAttribute("class", "link");
    tag.add(link);

    if (latestTestTime != null) {
      tag.add(HtmlUtil.makeSpanTag("", String.format("(%.03f seconds)", latestTestTime.elapsedSeconds())));
    }

    pageCounts.tallyPageCounts(getExecutionResult(relativeName, testSummary, wasInterupted()));
    HtmlTag insertScript = HtmlUtil.makeAppendElementScript(testSummariesId, tag.html());
    writeData(insertScript.html());
  }

  private void finishOutputForTest() {
    writeData("</div>" + HtmlTag.endl);
  }

  @Override
  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws IOException {
    latestTestTime = totalTimeMeasurement;
    removeStopTestLink();
    publishAndAddLog();
    maybeMakeErrorNavigatorVisible();
    finishWritingOutput();
    close();

    super.allTestingComplete(totalTimeMeasurement);
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
    testSummariesId = "test-system-" + testSystemName;
    String tag = String.format("<h3>%s</h3>\n<ul id=\"%s\"></ul>", testSystemFullName, testSummariesId);
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

}



