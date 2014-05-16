// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.reporting;

import static fitnesse.testsystems.ExecutionResult.getExecutionResult;

import java.io.Closeable;
import java.io.IOException;

import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.*;
import util.TimeMeasurement;
import fitnesse.FitNesseContext;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public abstract class SuiteHtmlFormatter extends InteractiveFormatter implements Closeable {
  private static final String TEST_SUMMARIES_ID = "test-summaries";

  private TestSummary pageCounts = new TestSummary();
  private int currentTest = 0;

  private final String testBasePathName;
  private String testSystemName = null;
  private int totalTests = 1;
  private TimeMeasurement latestTestTime;
  private String testSummariesId = TEST_SUMMARIES_ID;
  private TimeMeasurement totalTimeMeasurement;


  public SuiteHtmlFormatter(FitNesseContext context, WikiPage page) {
    super(context, page);
    totalTimeMeasurement = new TimeMeasurement().start();
    testBasePathName = PathParser.render(page.getPageCrawler().getFullPath());
  }

  @Override
  public void announceNumberTestsToRun(int testsToRun) {
    super.announceNumberTestsToRun(testsToRun);
    totalTests = (testsToRun != 0) ? testsToRun : 1;
  }

  public void announceStartNewTest(String relativeName, String fullPathName) {
    currentTest++;
    updateSummaryDiv(getProgressHtml(relativeName));

    maybeWriteTestSystem();
    writeTestOutputDiv(relativeName, fullPathName);
  }

  private void writeTestOutputDiv(String relativeName, String fullPathName) {
    if (!testBasePathName.equals(fullPathName)) {
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
    }
    writeData("<div class=\"alternating_block\">");
  }

  private void maybeWriteTestSystem() {
    if (testSystemName != null) {
      HtmlTag systemTitle = new HtmlTag("h2", String.format("Test System: %s", testSystemName));
      writeData(systemTitle.html());
      // once we write it out we don't need it any more
      testSystemName = null;
    }
  }

  @Override
  public void testStarted(WikiTestPage testPage) {
    latestTestTime = new TimeMeasurement().start();
    super.testStarted(testPage);

    WikiPagePath fullPath = testPage.getSourcePage().getPageCrawler().getFullPath();
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

    pageCounts.tallyPageCounts(getExecutionResult(relativeName, testSummary, wasInterrupted()));
    HtmlTag insertScript = HtmlUtil.makeAppendElementScript(testSummariesId, tag.html());
    writeData(insertScript.html());
  }

  private void finishOutputForTest() {
    writeData("</div>" + HtmlTag.endl);
  }

  @Override
  public void close() throws IOException {
    // Todo: why assign it to this variable, looks inconsistent.
    latestTestTime = totalTimeMeasurement.stop();
    removeStopTestLink();
    publishAndAddLog();
    maybeMakeErrorNavigatorVisible();
    finishWritingOutput();
  }

  @Override
  public void testOutputChunk(String output) throws IOException {
    writeData(output);
  }


  @Override
  public void testComplete(WikiTestPage testPage, TestSummary testSummary) throws IOException {
    latestTestTime.stop();
    super.testComplete(testPage, testSummary);

    processTestResults(getRelativeName(testPage), testSummary);
    latestTestTime = null;
  }

  @Override
  public void errorOccurred(Throwable cause) {
    latestTestTime = null;
    super.errorOccurred(cause);
  }

  @Override
  public void testSystemStarted(TestSystem testSystem) {
    testSystemName = testSystem.getName();
    testSummariesId = "test-system-" + testSystemName;
    String tag = String.format("<h3>%s</h3>\n<ul id=\"%s\"></ul>", testSystemName, testSummariesId);
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

}



