// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.html.HtmlPageFactory;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import util.RegexTestCase;

public class TestHtmlFormatterTest extends RegexTestCase {
  private BaseFormatter formatter;
  private StringBuffer pageBuffer = new StringBuffer();
  private WikiPage page;
  private WikiPage root;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    page = root.addChildPage("NewPage");
    page.getData().setContent("page content here");

    formatter = new TestHtmlFormatter(page, new HtmlPageFactory()) {
      @Override
      protected void writeData(String output) throws Exception {
        pageBuffer.append(output);
      }
    };
  }

  public void tearDown() throws Exception {
  }

  public void testHead() throws Exception {
    formatter.writeHead("test");

    assertSubString("<div id=\"test-summary\">Running Tests ...</div>", pageBuffer.toString());
  }

  public void testTestSummaryTestPass() throws Exception {

    formatter.writeHead("test");
    formatter.announceStartNewTest(page);
    formatter.processTestResults(page, new TestSummary(4, 0, 0, 0));
    formatter.allTestingComplete();
    assertSubString("<script>document.getElementById(\"test-summary\").innerHTML =", pageBuffer.toString());
    assertSubString("<strong>Assertions:</strong> 4 right, 0 wrong, 0 ignored, 0 exceptions", pageBuffer.toString());
    assertSubString("document.getElementById(\"test-summary\").className = \"pass\"", pageBuffer.toString());
  }

  public void testTestSummaryTestFail() throws Exception {
    formatter.writeHead("test");
    formatter.announceStartNewTest(page);
    formatter.processTestResults(page, new TestSummary(4, 1, 0, 0));
    formatter.allTestingComplete();
    assertSubString("<strong>Assertions:</strong> 4 right, 1 wrong, 0 ignored, 0 exceptions", pageBuffer.toString());
    assertSubString("document.getElementById(\"test-summary\").className = \"fail\"", pageBuffer.toString());
  }

  public void testExecutionStatusHtml() throws Exception {
    formatter.writeHead("test");
    formatter.setExecutionLogAndTrackingId("2", new CompositeExecutionLog(root.addChildPage("ErrorLogs")));
    formatter.announceStartNewTest(page);
    formatter.processTestResults(page, new TestSummary(4, 1, 0, 0));
    formatter.allTestingComplete();
    assertSubString("<div id=\"execution-status\">", pageBuffer.toString());
  }

  public void testTail() throws Exception {
    formatter.writeHead("test");
    formatter.announceStartNewTest(page);
    formatter.processTestResults(page, new TestSummary(4, 1, 0, 0));
    formatter.allTestingComplete();

    assertSubString("</html>", pageBuffer.toString());
  }

  public void testStop() throws Exception {
    formatter.writeHead("test");
    formatter.setExecutionLogAndTrackingId("2", new CompositeExecutionLog(root.addChildPage("ErrorLogs")));
    formatter.announceStartNewTest(page);
    formatter.processTestResults(page, new TestSummary(4, 1, 0, 0));
    formatter.allTestingComplete();
    //assert stop button added
    assertSubString("<a href=\"#\" onclick=\"doSilentRequest('?responder=stoptest&id=2')\">", pageBuffer.toString());
    //assert stop button removed
    assertSubString("document.getElementById(\"stop-test\").innerHTML = \"\"", pageBuffer.toString());
  }

  public void testIncompleteMessageAfterException() throws Exception {
    formatter.writeHead("test");
    formatter.setExecutionLogAndTrackingId("2", new CompositeExecutionLog(root.addChildPage("ErrorLogs")));
    formatter.announceStartNewTest(page);
    pageBuffer.setLength(0);
    formatter.errorOccured();
    //assert stop button added
    assertSubString("Testing was interupted", pageBuffer.toString());
    //assert stop button removed
    assertSubString("className = \"fail\"", pageBuffer.toString());
  }
}
