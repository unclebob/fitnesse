// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.formatters;

import fitnesse.responders.run.TestPage;
import util.TimeMeasurement;
import fitnesse.FitNesseContext;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestSystem;
import fitnesse.html.HtmlPageFactory;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public abstract class SuiteHtmlFormatter extends TestHtmlFormatter {
  private static final String cssSuffix1 = "1";
  private static final String cssSuffix2 = "2";

  private TestSummary pageCounts = new TestSummary();
  private static final String TEST_SUMMARIES_ID = "test_summaries";

  private String cssSuffix = cssSuffix1;
  private int currentTest = 0;
  private String testSystemFullName = null;
  private boolean printedTestOutput = false;
  private int totalTests = 1;


  public SuiteHtmlFormatter(FitNesseContext context, WikiPage page, HtmlPageFactory pageFactory) throws Exception {
    super(context, page, pageFactory);
  }

  public SuiteHtmlFormatter(FitNesseContext context) {
    super(context);
  }

  public String getTestSystemHeader(String testSystemName) throws Exception {
    String tag = String.format("<h3>%s</h3>\n", testSystemName);
    HtmlTag insertScript = HtmlUtil.makeAppendElementScript("test_summaries", tag);
    return insertScript.html();
  }

  @Override
  public void announceNumberTestsToRun(int testsToRun) {
    super.announceNumberTestsToRun(testsToRun);
    totalTests = (testsToRun != 0) ? testsToRun : 1;
  }

  public void announceStartNewTest(String relativeName, String fullPathName) throws Exception {
    currentTest++;
    maybeWriteTestOutputDiv();
    maybeWriteTestSystem();
    updateSummaryDiv(getProgressHtml());
    writeTestOuputDiv(relativeName, fullPathName);
  }

  private void writeTestOuputDiv(String relativeName, String fullPathName)
    throws Exception {
    HtmlTag pageNameBar = HtmlUtil.makeDivTag("test_output_name");
    HtmlTag anchor = HtmlUtil.makeLink(fullPathName, relativeName);
    anchor.addAttribute("id", relativeName + currentTest);
    anchor.addAttribute("class", "test_name");

    HtmlTag topLink = HtmlUtil.makeLink("#" + TEST_SUMMARIES_ID, "Top");
    topLink.addAttribute("class", "top_of_page");

    pageNameBar.add(anchor);
    pageNameBar.add(topLink);
    writeData(pageNameBar.html());

    writeData("<div class=\"alternating_block_" + cssSuffix + "\">");
  }

  private void maybeWriteTestOutputDiv() throws Exception {
    if (!printedTestOutput) {
      HtmlTag outputTitle = new HtmlTag("h2", "Test Output");
      outputTitle.addAttribute("class", "centered");
      writeData(outputTitle.html());
      printedTestOutput = true;
    }
  }

  private void maybeWriteTestSystem() throws Exception {
    if (testSystemFullName != null) {
      HtmlTag systemTitle = new HtmlTag("h2", String.format("Test System: %s", testSystemFullName));
      systemTitle.addAttribute("class", "centered");
      writeData(systemTitle.html());
      // once we write it out we don't need it any more
      testSystemFullName = null;
    }
  }

  @Override
  public void newTestStarted(TestPage newTest, TimeMeasurement timeMeasurement) throws Exception {
    String relativeName = getRelativeName(newTest);
    
    PageCrawler pageCrawler = getPage().getPageCrawler();
    WikiPagePath fullPath = pageCrawler.getFullPath(newTest.getSourcePage());
    String fullPathName = PathParser.render(fullPath);

    announceStartNewTest(relativeName, fullPathName);
  }

  private String getProgressHtml() throws Exception {
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

  @Override
  public void processTestResults(String relativeName, TestSummary testSummary) throws Exception {
    finishOutputForTest();

    getAssertionCounts().add(testSummary);

    switchCssSuffix();
    HtmlTag mainDiv = HtmlUtil.makeDivTag("alternating_row_" + cssSuffix);

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

  protected TestSummary getFinalSummary() {
    return pageCounts;
  }

  private void finishOutputForTest() throws Exception {
    writeData("</div>" + HtmlTag.endl);
  }

  @Override
  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws Exception {
    latestTestTime = totalTimeMeasurement;
    super.allTestingComplete(totalTimeMeasurement);
  }

  private void switchCssSuffix() {
    if (cssSuffix1.equals(cssSuffix))
      cssSuffix = cssSuffix2;
    else
      cssSuffix = cssSuffix1;
  }

  @Override
  public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner)
    throws Exception {
    testSystemFullName = (testSystemName + ":" + testRunner).replaceAll("\\\\", "/");
    String tag = String.format("<h3>%s</h3>\n", testSystemFullName);
    HtmlTag insertScript = HtmlUtil.makeAppendElementScript(TEST_SUMMARIES_ID, tag);
    writeData(insertScript.html());

  }

  @Override
  protected String makeSummaryContent() {
    String testPagesSummary = "<strong>Test Pages:</strong> " + pageCounts.toString() + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
    return testPagesSummary + super.makeSummaryContent();
  }

  public void finishWritingOutput() throws Exception {
    writeData(testSummary());
    writeData(getHtmlPage().postDivision);
  }

  @Override
  public void writeHead(String pageType) throws Exception {
    super.writeHead(pageType);

    HtmlTag outputTitle = new HtmlTag("h2", "Test Summaries");
    outputTitle.addAttribute("class", "centered");

    HtmlTag summariesDiv = HtmlUtil.makeDivTag(TEST_SUMMARIES_ID);
    summariesDiv.addAttribute("id", TEST_SUMMARIES_ID);
    summariesDiv.add(outputTitle);
    writeData(summariesDiv.html());
  }
}



