// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.reporting;

import fitnesse.FitNesseContext;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.TagGroup;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testrunner.WikiTestPage;
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
  public void testStarted(WikiTestPage testPage) {
    latestTestTime = new TimeMeasurement().start();
	  super.testStarted(testPage);
    writeData(WikiPageUtil.getHeaderPageHtml(getPage()));
  }

  @Override
  public void testSystemStarted(TestSystem testSystem) {
  }

  @Override
  public void testComplete(WikiTestPage testPage, TestSummary testSummary) throws IOException {
    latestTestTime.stop();
    super.testComplete(testPage, testSummary);

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
  public void close() throws IOException {
    super.close();
    removeStopTestLink();
    publishAndAddLog();
    maybeMakeErrorNavigatorVisible();
    finishWritingOutput();
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
  public void addMessageForBlankHtml() {
    TagGroup html = new TagGroup();
    HtmlTag h2 = new HtmlTag("h2");
    h2.add("Oops!  Did you forget to add to some content to this ?");
    html.add(h2.html());
    html.add(HtmlUtil.HR.html());
    writeData(html.html());
  }

  @Override
  public void errorOccurred(Throwable cause) {
    latestTestTime = null;
    super.errorOccurred(cause);
  }
}
