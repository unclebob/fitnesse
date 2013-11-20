// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.reporting;

import java.util.Date;

import static util.RegexTestCase.assertSubString;

import fitnesse.FitNesseContext;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.TestSummary;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.mem.InMemoryPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.Clock;
import util.DateAlteringClock;
import util.TimeMeasurement;

public class TestHtmlFormatterTest {
  private TestHtmlFormatter formatter;
  private StringBuffer pageBuffer = new StringBuffer();
  private WikiTestPage page;
  private DateAlteringClock clock;

  @Before
  public void setUp() throws Exception {
    clock = new DateAlteringClock(new Date()).freeze();
    WikiPage root = InMemoryPage.makeRoot("RooT");
    page = new WikiTestPage(root.addChildPage("NewPage"));
    page.getData().setContent("page content here");
    FitNesseContext context = FitNesseUtil.makeTestContext();

    formatter = new TestHtmlFormatter(context, page.getSourcePage()) {
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
  public void testTestSummaryTestPass() throws Exception {
    formatter.announceNumberTestsToRun(1);
    formatter.testStarted(page);
    formatter.testComplete(page, new TestSummary(4, 0, 0, 0));
    formatter.close();
    assertSubString("<script>document.getElementById(\"test-summary\").innerHTML =", pageBuffer.toString());
    assertSubString("<strong>Assertions:</strong> 4 right, 0 wrong, 0 ignored, 0 exceptions", pageBuffer.toString());
    assertSubString("document.getElementById(\"test-summary\").className = \"pass\"", pageBuffer.toString());
  }

  @Test
  public void testTestSummaryTestFail() throws Exception {
    formatter.announceNumberTestsToRun(1);
    formatter.testStarted(page);
    formatter.testComplete(page, new TestSummary(4, 1, 0, 0));
    formatter.close();
    assertSubString("<strong>Assertions:</strong> 4 right, 1 wrong, 0 ignored, 0 exceptions", pageBuffer.toString());
    assertSubString("document.getElementById(\"test-summary\").className = \"fail\"", pageBuffer.toString());
  }

  @Test
  public void testExecutionStatusHtml() throws Exception {
    formatter.setTrackingId("2");
    formatter.testStarted(page);
    formatter.testComplete(page, new TestSummary(4, 1, 0, 0));
    formatter.close();
    assertSubString("Tests Executed OK", pageBuffer.toString());
  }

  @Test
  public void testTail() throws Exception {
    formatter.announceNumberTestsToRun(1);
    formatter.testStarted(page);
    formatter.testComplete(page, new TestSummary(4, 1, 0, 0));
    formatter.close();

    assertSubString("<strong>Assertions:</strong>", pageBuffer.toString());
  }

  @Test
  public void testStop() throws Exception {
    formatter.setTrackingId("2");
    formatter.announceNumberTestsToRun(1);
    formatter.testStarted(page);
    formatter.testComplete(page, new TestSummary(4, 1, 0, 0));
    formatter.close();
    //assert stop button added - double escaped, since it's in javascript
    assertSubString("<a href=\\\"#\\\" onclick=\\\"doSilentRequest('?responder=stoptest&id=2')\\\" class=\\\"stop\\\">", pageBuffer.toString());
    //assert stop button removed
    assertSubString("document.getElementById(\"test-action\").innerHTML = \"\"", pageBuffer.toString());
  }

  @Test
  public void testIncompleteMessageAfterException() throws Exception {
    formatter.setTrackingId("2");
    formatter.announceNumberTestsToRun(1);
    formatter.testStarted(page);
    pageBuffer.setLength(0);
    formatter.errorOccurred(new Exception("test"));
    //assert stop button added
    assertSubString("Testing was interrupted", pageBuffer.toString());
    //assert stop button removed
    assertSubString("className = \"ignore\"", pageBuffer.toString());
  }

  @Test
  public void testTimingShouldAppearInSummary() throws Exception {
    TimeMeasurement totalTimeMeasurement = newConstantElapsedTimeMeasurement(987).start();
    formatter.announceNumberTestsToRun(1);
    formatter.testStarted(page);
    clock.elapse(600);
    formatter.testComplete(page, new TestSummary(1, 2, 3, 4));
    formatter.close();
    assertSubString("<strong>Assertions:</strong> 1 right, 2 wrong, 3 ignored, 4 exceptions (0.600 seconds)", pageBuffer.toString());
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
