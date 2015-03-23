// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.reporting;

import static fitnesse.testsystems.ExecutionResult.getExecutionResult;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;

import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.*;
import fitnesse.util.TimeMeasurement;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class SuiteHtmlFormatter extends InteractiveFormatter implements Closeable {
  private static final String TEST_SUMMARIES_ID = "test-summaries";

  private TestSummary pageCounts = new TestSummary();
  private int currentTest = 0;

  private final String testBasePathName;
  private String testSystemName = null;
  private int totalTests = 1;
  private TimeMeasurement latestTestTime;
  private String testSummariesId = TEST_SUMMARIES_ID;
  private TimeMeasurement totalTimeMeasurement;


  public SuiteHtmlFormatter(WikiPage page, Writer writer) {
    super(page, writer);
    totalTimeMeasurement = new TimeMeasurement().start();
    testBasePathName = PathParser.render(page.getPageCrawler().getFullPath());
  }

  @Override
  public void announceNumberTestsToRun(int testsToRun) {
    super.announceNumberTestsToRun(testsToRun);
    totalTests = (testsToRun != 0) ? testsToRun : 1;
  }

  public void announceStartNewTest(String relativeName, String fullPathName) throws IOException {
    currentTest++;
    updateSummaryDiv(getProgressHtml(relativeName));

    maybeWriteTestSystem();
    writeTestOutputDiv(relativeName, fullPathName);
  }

  private void writeTestOutputDiv(String relativeName, String fullPathName) throws IOException {
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

  private void maybeWriteTestSystem() throws IOException {
    if (testSystemName != null) {
      HtmlTag systemTitle = new HtmlTag("h2", String.format("Test System: %s", testSystemName));
      writeData(systemTitle.html());
      // once we write it out we don't need it any more
      testSystemName = null;
    }
  }

  @Override
  public void testStarted(WikiTestPage testPage) throws IOException {
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

  private void finishOutputForTest() throws IOException {
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
  public void testSystemStarted(TestSystem testSystem) throws IOException {
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



