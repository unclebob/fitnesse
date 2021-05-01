// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.reporting;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;

import static fitnesse.reporting.DecimalSeparatorUtil.getDecimalSeparator;
import static fitnesse.reporting.DecimalSeparatorUtil.getDecimalSeparatorForRegExp;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static util.RegexTestCase.*;

import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;
import fitnesse.wiki.fs.InMemoryPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import fitnesse.util.Clock;
import fitnesse.util.DateAlteringClock;

public class SuiteHtmlFormatterTest {
  private SuiteHtmlFormatter formatter;
  private StringWriter pageBuffer;
  private DateAlteringClock clock;

  @Before
  public void setUp() throws Exception {
    clock = new DateAlteringClock(new Date()).freeze();
    pageBuffer = new StringWriter();
    createFormatter(true);
  }

  @After
  public void restoreDefaultClock() {
    Clock.restoreDefaultClock();
  }

  @Test
  public void testTestSummary() throws Exception {
    formatter.processTestResults("TestName", new TestSummary(49, 0, 0, 0));
    formatter.processTestResults("TestName2", new TestSummary(1, 0, 2, 0));
    formatter.processTestResults("TestName3", new TestSummary(1, 1, 0, 0));
    formatter.finishWritingOutput();

    assertPageBufferContains("<strong>Test Pages:</strong> 2 right, 1 wrong, 0 ignored, 0 exceptions");
    assertPageBufferContains("<strong>Assertions:</strong> 51 right, 1 wrong, 2 ignored, 0 exceptions");
  }

  private void testSuiteMetaTestSummaryWithTestResults(String pageName) throws Exception {
    formatter.processTestResults(pageName, new TestSummary(2, 0, 0, 0));
    formatter.finishWritingOutput();

    assertPageBufferContains("<span class=\\\"results pass\\\">2 right, 0 wrong, 0 ignored, 0 exceptions</span>");
    assertPageBufferContains("<strong>Test Pages:</strong> 1 right, 0 wrong, 0 ignored, 0 exceptions");
    assertPageBufferContains("<strong>Assertions:</strong> 2 right, 0 wrong, 0 ignored, 0 exceptions");
  }

  @Test
  public void testSuiteSetUpSummaryWithTestResults() throws Exception {
    testSuiteMetaTestSummaryWithTestResults("SuiteSetUp");
  }

  @Test
  public void testSuiteTearDownSummaryWithTestResults() throws Exception {
    testSuiteMetaTestSummaryWithTestResults("SuiteTearDown");
  }

  private void testSuiteMetaTestSummaryWithoutTestResults(String pageName) throws Exception {
    formatter.processTestResults(pageName, new TestSummary(0, 0, 0, 0));
    formatter.finishWritingOutput();

    assertPageBufferContains("<span class=\\\"results pass\\\">0 right, 0 wrong, 0 ignored, 0 exceptions</span>");
    assertPageBufferContains("<strong>Test Pages:</strong> 1 right, 0 wrong, 0 ignored, 0 exceptions");
    assertPageBufferContains("<strong>Assertions:</strong> 0 right, 0 wrong, 0 ignored, 0 exceptions");
  }

  @Test
  public void testSuiteSetUpSummaryWithoutTestResults() throws Exception {
    testSuiteMetaTestSummaryWithoutTestResults("SuiteSetUp");
  }

  @Test
  public void testSuiteTearDownSummaryWithoutTestResults() throws Exception {
    testSuiteMetaTestSummaryWithoutTestResults("SuiteTearDown");
  }

  @Test
  public void testCountsRightHtml() throws Exception {
    formatter.processTestResults("RelativePageName", new TestSummary(1, 0, 0, 0));

    assertPageBufferContains("<span class=\\\"results pass\\\">1 right, 0 wrong, 0 ignored, 0 exceptions</span>");
    assertPageBufferContains("<a href=\\\"#RelativePageName0\\\" class=\\\"link\\\">RelativePageName</a>");
  }

  @Test
  public void testCountsWrongHtml() throws Exception {
    formatter.processTestResults("AnotherPageName", new TestSummary(0, 1, 0, 0));

    assertPageBufferContains("<span class=\\\"results fail\\\">0 right, 1 wrong, 0 ignored, 0 exceptions</span>");
    assertPageBufferContains("<a href=\\\"#AnotherPageName0\\\" class=\\\"link\\\">AnotherPageName</a>");
  }

  @Test
  public void testResultsHtml() {
    sendTwoSystemRunToFormatter();

    assertPageBufferContains("document.getElementById(\"test-summaries\")");
    assertPageBufferContains("document.getElementById(\"test-system-Fit:laughing.fit\").innerHTML");
    assertPageBufferContains("document.getElementById(\"test-system-Slim:very.slim\").innerHTML");

    assertPageBufferContains("<h2>Test System: Slim:very.slim</h2>");
    assertPageBufferContains("<h2>Test System: Fit:laughing.fit</h2>");
  }

  @Test
  public void testResultsHtmlNoSummaries() {
    createFormatter(false);

    sendTwoSystemRunToFormatter();

    String output = pageBuffer.toString();
    assertNotSubString("document.getElementById(\"test-summaries\")", output);
    assertNotSubString("document.getElementById(\"test-system-Fit:laughing.fit\").innerHTML", output);
    assertNotSubString("document.getElementById(\"test-system-Slim:very.slim\").innerHTML", output);

    assertNotSubString("<h2>Test System: Slim:very.slim</h2>", output);
    assertNotSubString("<h2>Test System: Fit:laughing.fit</h2>", output);
  }

  private void sendTwoSystemRunToFormatter() {
    TestSystem fitMock = mock(TestSystem.class);
    when(fitMock.getName()).thenReturn("Fit:laughing.fit");
    TestSystem slimMock = mock(TestSystem.class);
    when(slimMock.getName()).thenReturn("Slim:very.slim");

    formatter.testSystemStarted(fitMock);
    formatter.announceNumberTestsToRun(2);
    formatter.announceStartNewTest("RelativeName", "FullName");
    TestPage testPage = new WikiTestPage(new WikiPageDummy("RelativeName", "Content", null));
    formatter.testOutputChunk(testPage, "starting");
    formatter.testOutputChunk(testPage, " output");
    formatter.processTestResults("RelativeName", new TestSummary(1, 0, 0, 0));

    formatter.testSystemStarted(slimMock);
    formatter.announceStartNewTest("NewRelativeName", "NewFullName");
    TestPage newTestPage = new WikiTestPage(new WikiPageDummy("NewRelativeName", "NewContent", null));
    formatter.testOutputChunk(newTestPage, "second");
    formatter.testOutputChunk(newTestPage, " test");
    formatter.processTestResults("NewRelativeName", new TestSummary(0, 1, 0, 0));
    formatter.finishWritingOutput();

    assertPageBufferContains("<a href=\"FullName\" class=\"test_name\">RelativeName</a>");
    assertPageBufferContains("<a name=\"RelativeName1\"/>");
    assertPageBufferContains("<div class=\"alternating_block\">starting output</div>");

    assertPageBufferContains("<a href=\"NewFullName\" class=\"test_name\">NewRelativeName</a>");
    assertPageBufferContains("<div class=\"alternating_block\">second test</div>");
  }

  @Test
  public void testTestingProgressIndicator() {
    TestSystem fitMock = mock(TestSystem.class);
    when(fitMock.getName()).thenReturn("Fit:laughing.fit");

    formatter.testSystemStarted(fitMock);
    formatter.announceNumberTestsToRun(20);
    formatter.announceStartNewTest("RelativeName", "FullName");

    assertPageBufferContains("<script>document.getElementById(\"test-summary\").innerHTML =" +
    		" \"<div id=\\\"progressBar\\\" class=\\\"pass\\\" style=\\\"width:0.0%\\\">");
    assertPageBufferContains("Running&nbsp;tests&nbsp;...&nbsp;(1/20)");
    pageBuffer.getBuffer().setLength(0);

    formatter.processTestResults("RelativeName", new TestSummary(1, 0, 0, 0));
    formatter.announceStartNewTest("RelativeName", "FullName");

    assertPageBufferContains("<script>document.getElementById(\"test-summary\").innerHTML =" +
        " \"<div id=\\\"progressBar\\\" class=\\\"pass\\\" style=\\\"width:5.0%\\\">");
    assertPageBufferContains("(2/20)");
    pageBuffer.getBuffer().setLength(0);


    formatter.processTestResults("RelativeName", new TestSummary(1, 0, 0, 0));
    formatter.announceStartNewTest("RelativeName", "FullName");

    assertPageBufferContains("<script>document.getElementById(\"test-summary\").innerHTML =" +
        " \"<div id=\\\"progressBar\\\" class=\\\"pass\\\" style=\\\"width:10.0%\\\">");
    assertPageBufferContains("(3/20)");
  }

  @Test
  public void testTotalTimingShouldAppearInSummary() throws Exception {
    formatter.announceNumberTestsToRun(1);
    WikiTestPage firstPage = new WikiTestPage(new WikiPageDummy("page1", "content", null));
    formatter.testStarted(firstPage);
    formatter.testComplete(firstPage, new TestSummary(1, 2, 3, 4));
    clock.elapse(900);
    formatter.close();
    assertPageBufferContains("<strong>Assertions:</strong> 1 right, 2 wrong, 3 ignored, 4 exceptions (0" + getDecimalSeparator() + "900 seconds)");
  }

  @Test
  public void testIndividualTestTimingsShouldAppearInSummary() throws Exception {
    sendTwoPageRunToFormatter();

    assertPageBufferContains("document.getElementById(\"test-summaries\")");

    String output = pageBuffer.toString();
    assertHasRegexp("<li.*\\(page1\\).*<span.*>\\(0(" + getDecimalSeparatorForRegExp() + "){1}670 seconds\\)</span>.*</li>", output);
    assertHasRegexp("<li.*\\(page2\\).*<span.*>\\(0(" + getDecimalSeparatorForRegExp() + "){1}890 seconds\\)</span>.*</li>", output);
  }

  @Test
  public void testNoIndividualTestTimingsWithoutSummaries() throws Exception {
    createFormatter(false);

    sendTwoPageRunToFormatter();

    String output = pageBuffer.toString();
    assertNotSubString("document.getElementById(\"test-summaries\")", output);
    assertDoesntHaveRegexp("<li.*\\(page1\\).*<span.*>.*</span>.*</li>", output);
    assertDoesntHaveRegexp("<li.*\\(page2\\).*<span.*>.*</span>.*</li>", output);
  }

  private void sendTwoPageRunToFormatter() throws IOException {
    formatter.announceNumberTestsToRun(2);
    WikiTestPage firstPage = new WikiTestPage(new WikiPageDummy("page1", "content", null));
    WikiTestPage secondPage = new WikiTestPage(new WikiPageDummy("page2", "content", null));
    formatter.testStarted(firstPage);
    clock.elapse(670);
    formatter.testComplete(firstPage, new TestSummary(1, 2, 3, 4));
    formatter.testStarted(secondPage);
    clock.elapse(890);
    formatter.testComplete(secondPage, new TestSummary(5, 6, 7, 8));
    formatter.close();
  }

  private void assertPageBufferContains(String substring) {
    assertSubString(substring, pageBuffer.toString());
  }

  private void createFormatter(boolean testSummariesPresent) {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    formatter = new SuiteHtmlFormatter(root, testSummariesPresent, pageBuffer);
  }
}
