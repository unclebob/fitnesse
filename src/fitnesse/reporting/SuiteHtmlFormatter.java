// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.reporting;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.util.TimeMeasurement;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;

import static fitnesse.testsystems.ExecutionResult.getExecutionResult;

public class SuiteHtmlFormatter extends InteractiveFormatter implements Closeable {
  private static final String TEST_SUMMARIES_ID = "test-summaries";

  private TestSummary pageCounts = new TestSummary();
  private int currentTest = 0;

  private final String testBasePathName;
  private String testSystemName = null;
  private int totalTests = 1;
  private TimeMeasurement latestTestTime;
  private String testSummariesId = TEST_SUMMARIES_ID;
  private boolean testSummariesPresent;
  private TimeMeasurement totalTimeMeasurement;


  public SuiteHtmlFormatter(WikiPage page, boolean testSummariesPresent, Writer writer) {
    super(page, writer);
    this.testSummariesPresent = testSummariesPresent;
    totalTimeMeasurement = new TimeMeasurement().start();
    testBasePathName = PathParser.render(page.getFullPath());
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
      HtmlTag title = new HtmlTag("h3");

      HtmlTag anchor = HtmlUtil.makeLink(fullPathName, relativeName);
      anchor.addAttribute("class", "test_name");
      title.add(anchor);

      HtmlTag name = new HtmlTag("a");
      name.addAttribute("name", relativeName + currentTest);
      title.add(name);

      HtmlTag topLink = HtmlUtil.makeLink("#", "Top");
      topLink.addAttribute("class", "top_of_page");

      title.add(new HtmlTag("small", topLink));
      writeData(title.html());
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
  public void testStarted(TestPage testPage) {
    latestTestTime = new TimeMeasurement().start();
    super.testStarted(testPage);

    String fullPathName = testPage.getFullPath();

    announceStartNewTest(getRelativeName(), fullPathName);
  }

  private String getProgressHtml(String relativeName) {
    float percentFinished = (currentTest - 1) * 100f / totalTests;

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

  public void processTestResults(String relativeName, TestSummary testSummary) {
    finishOutputForTest();

    getAssertionCounts().add(testSummary);

    if (hasTestSummaries()) {
      addToTestSummaries(relativeName, testSummary);
    }
  }

  protected void addToTestSummaries(String relativeName, TestSummary testSummary) {
    HtmlTag tag = new HtmlTag("li");

    tag.add(HtmlUtil.makeSpanTag("results " + getExecutionResult(relativeName, testSummary), testSummary.toString()));

    HtmlTag link = HtmlUtil.makeLink("#" + relativeName + currentTest, relativeName);
    link.addAttribute("class", "link");
    tag.add(link);

    if (latestTestTime != null) {
      tag.add(HtmlUtil.makeSpanTag("", String.format("(%.03f seconds)", latestTestTime.elapsedSeconds())));
    }

    pageCounts.add(getExecutionResult(relativeName, testSummary, wasInterrupted()));
    HtmlTag insertScript = JavascriptUtil.makeAppendElementScript(testSummariesId, tag.html());
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
    AddLogLink();
    maybeMakeErrorNavigatorVisible();
    finishWritingOutput();
  }

  @Override
  public void testOutputChunk(TestPage testPage, String output) {
    writeData(output);
  }


  @Override
  public void testComplete(TestPage testPage, TestSummary testSummary) {
    latestTestTime.stop();
    super.testComplete(testPage, testSummary);

    processTestResults(getRelativeName(testPage), testSummary);
    latestTestTime = null;
  }

  @Override
  public void testSystemStarted(TestSystem testSystem) {
    if (hasTestSummaries()) {
      testSystemName = testSystem.getName();
      testSummariesId = "test-system-" + testSystemName;
      String tag = String.format("<h3>%s</h3>\n<ul id=\"%s\"></ul>", testSystemName, testSummariesId);
      HtmlTag insertScript = JavascriptUtil.makeAppendElementScript(TEST_SUMMARIES_ID, tag);
      writeData(insertScript.html());
    }
  }

  @Override
  protected String makeSummaryContent() {
    String summaryContent = "<strong>Test Pages:</strong> " + pageCounts.toString() + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
    if (latestTestTime != null) {
      summaryContent += String.format("<strong>Assertions:</strong> %s (%.03f seconds)", getAssertionCounts(), latestTestTime.elapsedSeconds());
    } else {
      summaryContent += String.format("<strong>Assertions:</strong> %s ", getAssertionCounts());
    }
    return summaryContent;
  }

  protected boolean hasTestSummaries() {
    return testSummariesPresent;
  }
}



