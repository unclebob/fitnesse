// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import util.RegexTestCase;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlPageFactory;
import fitnesse.FitNesseContext;

public class SuiteHtmlFormatterTest extends RegexTestCase {
  private HtmlPage htmlPage;
  private SuiteHtmlFormatter formatter;
  private StringBuffer pageBuffer = new StringBuffer();

  public void setUp() throws Exception {
    FitNesseContext context = new FitNesseContext();
    htmlPage = new HtmlPageFactory().newPage();
    formatter = new SuiteHtmlFormatter(context) {
      @Override
      protected HtmlPage buildHtml(String pageType) throws Exception {
        return htmlPage;
      }
      
      @Override
      protected void writeData(String output) throws Exception {
        pageBuffer.append(output);
      }
    };
  }

  public void tearDown() throws Exception {
  }

  public void testTestSummary() throws Exception {
    formatter.processTestResults("TestName", new TestSummary(49, 0, 0, 0));
    formatter.processTestResults("TestName2", new TestSummary(1, 0, 2, 0));
    formatter.processTestResults("TestName3", new TestSummary(1, 1, 0, 0));
    formatter.finishWritingOutput();

    assertSubString("<strong>Test Pages:</strong> 2 right, 1 wrong, 0 ignored, 0 exceptions", pageBuffer.toString());
    assertSubString("<strong>Assertions:</strong> 51 right, 1 wrong, 2 ignored, 0 exceptions", pageBuffer.toString());
  }

  public void testCountsHtml() throws Exception {
    formatter.processTestResults("RelativePageName", new TestSummary(1, 0, 0, 0));

    assertSubString("<div class=\\\"alternating_row_2\\\">", pageBuffer.toString());
    assertSubString("<span class=\\\"test_summary_results pass\\\">1 right, 0 wrong, 0 ignored, 0 exceptions</span>", pageBuffer.toString());
    assertSubString("<a href=\\\"#RelativePageName0\\\" class=\\\"test_summary_link\\\">RelativePageName</a>", pageBuffer.toString());

    pageBuffer.setLength(0);
    formatter.processTestResults("AnotherPageName", new TestSummary(0, 1, 0, 0));

    assertSubString("<div class=\\\"alternating_row_1\\\">", pageBuffer.toString());
    assertSubString("<span class=\\\"test_summary_results fail\\\">0 right, 1 wrong, 0 ignored, 0 exceptions</span>", pageBuffer.toString());
    assertSubString("<a href=\\\"#AnotherPageName0\\\" class=\\\"test_summary_link\\\">AnotherPageName</a>", pageBuffer.toString());
  }

  public void testResultsHtml() throws Exception {
    formatter.announceStartTestSystem(null, "Fit", "laughing.fit");
    formatter.announceNumberTestsToRun(2);
    formatter.announceStartNewTest("RelativeName", "FullName");
    formatter.processTestOutput("starting");
    formatter.processTestOutput(" output");
    formatter.processTestResults("RelativeName", new TestSummary(1, 0, 0, 0));
    formatter.announceStartTestSystem(null, "Slim", "very.slim");
    formatter.announceStartNewTest("NewRelativeName", "NewFullName");
    formatter.processTestOutput("second");
    formatter.processTestOutput(" test");
    formatter.processTestResults("NewRelativeName", new TestSummary(0, 1, 0, 0));
    formatter.finishWritingOutput();

    String results = pageBuffer.toString();    
    assertSubString("<h2 class=\"centered\">Test Output</h2>", results);
    assertSubString("<h2 class=\"centered\">Test System: Slim:very.slim</h2>", results);

    assertSubString("<div class=\"test_output_name\">", results);
    assertSubString("<a href=\"FullName\" id=\"RelativeName1\" class=\"test_name\">RelativeName</a>", results);
    assertSubString("<div class=\"alternating_block_1\">starting output</div>", results);

    assertSubString("<a href=\"NewFullName\" id=\"NewRelativeName2\" class=\"test_name\">NewRelativeName</a>", results);
    assertSubString("<div class=\"alternating_block_2\">second test</div>", results);
  }
  
  public void testTestingProgressIndicator() throws Exception {
    formatter.announceStartTestSystem(null, "Fit", "laughing.fit");
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
}
