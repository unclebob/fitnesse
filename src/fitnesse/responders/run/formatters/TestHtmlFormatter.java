// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.formatters;

import fitnesse.FitNesseContext;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.TagGroup;
import fitnesse.responders.run.TestPage;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import util.TimeMeasurement;

import java.io.IOException;

public abstract class TestHtmlFormatter extends InteractiveFormatter {
  protected TimeMeasurement latestTestTime;

  public TestHtmlFormatter(FitNesseContext context, final WikiPage page) {
    super(context, page);
  }

  //special constructor for TestRunner.  Used only for formatting.
  //todo this is nasty coupling.
  public TestHtmlFormatter(FitNesseContext context) {
    super(context, null);
  }

  @Override
  public void newTestStarted(TestPage testPage, TimeMeasurement timeMeasurement) throws IOException {
	super.newTestStarted(testPage, timeMeasurement);
    writeData(WikiPageUtil.getHeaderPageHtml(getPage()));
  }

  @Override
  public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) {
  }

  @Override
  public void testComplete(TestPage testPage, TestSummary testSummary, TimeMeasurement timeMeasurement) throws IOException {
    super.testComplete(testPage, testSummary, timeMeasurement);
    latestTestTime = timeMeasurement;

    processTestResults(getRelativeName(testPage), testSummary);
  }

  public void processTestResults(String relativeName, TestSummary testSummary) throws IOException {
    getAssertionCounts().add(testSummary);
  }

  @Override
  public void testOutputChunk(String output) throws IOException {
    writeData(output);
  }

  @Override
  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws IOException {
    super.allTestingComplete(totalTimeMeasurement);
    removeStopTestLink();
    publishAndAddLog();
    maybeMakeErrorNavigatorVisible();
    finishWritingOutput();
    close();
  }

  @Override
  protected void finishWritingOutput() throws IOException {
    writeData(testSummary());
    super.finishWritingOutput();
  }

  protected String makeSummaryContent() {
    String summaryContent;
    if (latestTestTime != null) {
      summaryContent = String.format("<strong>Assertions:</strong> %s (%.03f seconds)", getAssertionCounts(), latestTestTime.elapsedSeconds());
    } else {
      summaryContent = String.format("<strong>Assertions:</strong> %s ", getAssertionCounts());
    }
    return summaryContent;
  }

  @Override
  public int getErrorCount() {
    return getAssertionCounts().getWrong() + getAssertionCounts().getExceptions();
  }

  @Override
  public void addMessageForBlankHtml() {
    TagGroup html = new TagGroup();
    HtmlTag h2 = new HtmlTag("h2");
    h2.add("Oops!  Did you forget to add to some content to this ?");
    html.add(h2.html());
    html.add(HtmlUtil.HR.html());
    writeData(html.html());
  }

  @Override
  public void errorOccured() {
    latestTestTime = null;
    super.errorOccured();
  }
}
