// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.reporting;

import java.util.Date;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static util.RegexTestCase.assertHasRegexp;
import static util.RegexTestCase.assertSubString;

import fitnesse.FitNesseContext;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;
import fitnesse.wiki.mem.InMemoryPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.Clock;
import util.DateAlteringClock;
import util.TimeMeasurement;

public class SuiteHtmlFormatterTest {
  private SuiteHtmlFormatter formatter;
  private StringBuffer pageBuffer = new StringBuffer();
  private DateAlteringClock clock;

  @Before
  public void setUp() throws Exception {
    clock = new DateAlteringClock(new Date()).freeze();
    FitNesseContext context = FitNesseUtil.makeTestContext();
    WikiPage root = InMemoryPage.makeRoot("RooT");
    formatter = new SuiteHtmlFormatter(context, root) {
      @Override
      protected void writeData(String output) {
        pageBuffer.append(output);
      }
    };
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

    assertSubString("<strong>Test Pages:</strong> 2 right, 1 wrong, 0 ignored, 0 exceptions", pageBuffer.toString());
    assertSubString("<strong>Assertions:</strong> 51 right, 1 wrong, 2 ignored, 0 exceptions", pageBuffer.toString());
  }

  private void testSuiteMetaTestSummaryWithTestResults(String pageName) throws Exception {
    formatter.processTestResults(pageName, new TestSummary(2, 0, 0, 0));
    formatter.finishWritingOutput();

    assertSubString("<span class=\\\"results pass\\\">2 right, 0 wrong, 0 ignored, 0 exceptions</span>", pageBuffer.toString());
    assertSubString("<strong>Test Pages:</strong> 1 right, 0 wrong, 0 ignored, 0 exceptions", pageBuffer.toString());
    assertSubString("<strong>Assertions:</strong> 2 right, 0 wrong, 0 ignored, 0 exceptions", pageBuffer.toString());
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

    assertSubString("<span class=\\\"results pass\\\">0 right, 0 wrong, 0 ignored, 0 exceptions</span>", pageBuffer.toString());
    assertSubString("<strong>Test Pages:</strong> 1 right, 0 wrong, 0 ignored, 0 exceptions", pageBuffer.toString());
    assertSubString("<strong>Assertions:</strong> 0 right, 0 wrong, 0 ignored, 0 exceptions", pageBuffer.toString());
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
  public void testCountsHtml() throws Exception {
    formatter.processTestResults("RelativePageName", new TestSummary(1, 0, 0, 0));

    assertSubString("<span class=\\\"results pass\\\">1 right, 0 wrong, 0 ignored, 0 exceptions</span>", pageBuffer.toString());
    assertSubString("<a href=\\\"#RelativePageName0\\\" class=\\\"link\\\">RelativePageName</a>", pageBuffer.toString());

    pageBuffer.setLength(0);
    formatter.processTestResults("AnotherPageName", new TestSummary(0, 1, 0, 0));

    assertSubString("<span class=\\\"results fail\\\">0 right, 1 wrong, 0 ignored, 0 exceptions</span>", pageBuffer.toString());
    assertSubString("<a href=\\\"#AnotherPageName0\\\" class=\\\"link\\\">AnotherPageName</a>", pageBuffer.toString());
  }

  @Test
  public void testResultsHtml() throws Exception {
    TestSystem fitMock = mock(TestSystem.class);
    when(fitMock.getName()).thenReturn("Fit:laughing.fit");
    TestSystem slimMock = mock(TestSystem.class);
    when(slimMock.getName()).thenReturn("Slim:very.slim");

    formatter.testSystemStarted(fitMock);
    formatter.announceNumberTestsToRun(2);
    formatter.announceStartNewTest("RelativeName", "FullName");
    formatter.testOutputChunk("starting");
    formatter.testOutputChunk(" output");
    formatter.processTestResults("RelativeName", new TestSummary(1, 0, 0, 0));
    formatter.testSystemStarted(slimMock);
    formatter.announceStartNewTest("NewRelativeName", "NewFullName");
    formatter.testOutputChunk("second");
    formatter.testOutputChunk(" test");
    formatter.processTestResults("NewRelativeName", new TestSummary(0, 1, 0, 0));
    formatter.finishWritingOutput();

    String results = pageBuffer.toString();    

    assertSubString("<h2>Test Output</h2>", results);
    assertSubString("<h2>Test System: Slim:very.slim</h2>", results);

    assertSubString("<div class=\"test_output_name\">", results);
    assertSubString("<a href=\"FullName\" id=\"RelativeName1\" class=\"test_name\">RelativeName</a>", results);
    assertSubString("<div class=\"alternating_block\">starting output</div>", results);

    assertSubString("<a href=\"NewFullName\" id=\"NewRelativeName2\" class=\"test_name\">NewRelativeName</a>", results);
    assertSubString("<div class=\"alternating_block\">second test</div>", results);
  }

  @Test
  public void testTestingProgressIndicator() throws Exception {
    TestSystem fitMock = mock(TestSystem.class);
    when(fitMock.getName()).thenReturn("Fit:laughing.fit");

    formatter.testSystemStarted(fitMock);
    formatter.announceNumberTestsToRun(20);
    formatter.announceStartNewTest("RelativeName", "FullName");

    assertSubString("<script>document.getElementById(\"test-summary\").innerHTML =" +
    		" \"<div id=\\\"progressBar\\\" class=\\\"pass\\\" style=\\\"width:0.0%\\\">", pageBuffer.toString());
    assertSubString("Running&nbsp;tests&nbsp;...&nbsp;(1/20)", pageBuffer.toString());
    pageBuffer.setLength(0);
    
    formatter.processTestResults("RelativeName", new TestSummary(1, 0, 0, 0));
    formatter.announceStartNewTest("RelativeName", "FullName");

    assertSubString("<script>document.getElementById(\"test-summary\").innerHTML =" +
        " \"<div id=\\\"progressBar\\\" class=\\\"pass\\\" style=\\\"width:5.0%\\\">", pageBuffer.toString());
    assertSubString("(2/20)", pageBuffer.toString());
    pageBuffer.setLength(0);


    formatter.processTestResults("RelativeName", new TestSummary(1, 0, 0, 0));
    formatter.announceStartNewTest("RelativeName", "FullName");

    assertSubString("<script>document.getElementById(\"test-summary\").innerHTML =" +
        " \"<div id=\\\"progressBar\\\" class=\\\"pass\\\" style=\\\"width:10.0%\\\">", pageBuffer.toString());
    assertSubString("(3/20)", pageBuffer.toString());
  }

  @Test
  public void testTotalTimingShouldAppearInSummary() throws Exception {
    formatter.page = new WikiPageDummy();
    formatter.announceNumberTestsToRun(1);
    WikiTestPage firstPage = new WikiTestPage(new WikiPageDummy("page1", "content"));
    formatter.testStarted(firstPage);
    formatter.testComplete(firstPage, new TestSummary(1, 2, 3, 4));
    clock.elapse(900);
    formatter.close();
    assertSubString("<strong>Assertions:</strong> 1 right, 2 wrong, 3 ignored, 4 exceptions (0.900 seconds)", pageBuffer.toString());
  }

  @Test
  public void testIndividualTestTimingsShouldAppearInSummary() throws Exception {
    TimeMeasurement totalTimeMeasurement = newConstantElapsedTimeMeasurement(900).start();
    formatter.page = new WikiPageDummy();
    formatter.announceNumberTestsToRun(2);
    WikiTestPage firstPage = new WikiTestPage(new WikiPageDummy("page1", "content"));
    WikiTestPage secondPage = new WikiTestPage(new WikiPageDummy("page2", "content"));
    formatter.testStarted(firstPage);
    clock.elapse(670);
    formatter.testComplete(firstPage, new TestSummary(1, 2, 3, 4));
    formatter.testStarted(secondPage);
    clock.elapse(890);
    formatter.testComplete(secondPage, new TestSummary(5, 6, 7, 8));
    formatter.close();
    assertHasRegexp("<li.*\\(page1\\).*<span.*>\\(0\\.670 seconds\\)</span>.*</li>", pageBuffer.toString());
    assertHasRegexp("<li.*\\(page2\\).*<span.*>\\(0\\.890 seconds\\)</span>.*</li>", pageBuffer.toString());
  }

  private TimeMeasurement newConstantElapsedTimeMeasurement(final long theElapsedTime) {
    return new TimeMeasurement() {
      @Override
      public long elapsed() {
        return theElapsedTime;
      }
    };
  }
}
