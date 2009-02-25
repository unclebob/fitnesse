// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlPageFactory;
import fitnesse.testutil.RegexTestCase;

public class TestHtmlFormatterTest extends RegexTestCase {
  private HtmlPage page;
  private BaseFormatter formatter;
  private StringBuffer pageBuffer = new StringBuffer();

  public void setUp() throws Exception {
    page = new HtmlPageFactory().newPage();
    formatter = new TestHtmlFormatter(null, null) {
      @Override
      protected HtmlPage buildHtml(String pageType) throws Exception {
        return page;
      }
      
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

  public void testTestSummary() throws Exception {
    formatter.processTestResults(null, new TestSummary(4, 0, 0, 0));
    formatter.allTestingComplete();
    assertSubString("<script>document.getElementById(\"test-summary\").innerHTML =", pageBuffer.toString());
    assertSubString("<strong>Assertions:</strong> 4 right, 0 wrong, 0 ignored, 0 exceptions", pageBuffer.toString());
    assertSubString("document.getElementById(\"test-summary\").className = \"pass\"", pageBuffer.toString());

    pageBuffer.setLength(0);
    formatter.processTestResults(null, new TestSummary(4, 1, 0, 0));
    formatter.allTestingComplete();
    assertSubString("<strong>Assertions:</strong> 4 right, 1 wrong, 0 ignored, 0 exceptions", pageBuffer.toString());
    assertSubString("document.getElementById(\"test-summary\").className = \"fail\"", pageBuffer.toString());
  }

  public void testExecutionStatusHtml() throws Exception {
    formatter.allTestingComplete();
    assertSubString("<div id=\"execution-status\">", pageBuffer.toString());
  }

  public void testTail() throws Exception {
    formatter.allTestingComplete();

    assertSubString("</html>", pageBuffer.toString());
  }
}
