// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.TagGroup;

public class TestHtmlFormatter {
  private HtmlPage page;

  public TestHtmlFormatter(HtmlPage page) throws Exception {
    this.page = page;
    page.main.use(HtmlPage.BreakPoint);
    page.divide();
  }

  public String head() throws Exception {
    return page.preDivision + makeSummaryPlaceHolder().html();
  }

  private HtmlTag makeSummaryPlaceHolder() {
    HtmlTag testSummaryDiv = new HtmlTag("div", "Running Tests ...");
    testSummaryDiv.addAttribute("id", "test-summary");

    return testSummaryDiv;
  }

  public String testSummary(TestSummary testSummary) throws Exception {
    String summaryContent = testPageSummary();
    summaryContent += "<strong>Assertions:</strong> " + testSummary.toString();

    HtmlTag script = new HtmlTag("script");
    script.add("document.getElementById(\"test-summary\").innerHTML = \"" + summaryContent + "\";");
    script.add("document.getElementById(\"test-summary\").className = \"" + cssClassFor(testSummary) + "\";");
    return script.html();
  }

  protected String testPageSummary() {
    return "";
  }

  protected String cssClassFor(TestSummary testSummary) {
    if (testSummary.wrong > 0)
      return "fail";
    else if (testSummary.exceptions > 0 || testSummary.right + testSummary.ignores == 0)
      return "error";
    else if (testSummary.ignores > 0 && testSummary.right == 0)
      return "ignore";
    else
      return "pass";
  }

  public String tail() {
    return page.postDivision;
  }

  public String messageForBlankHtml() throws Exception {
    TagGroup html = new TagGroup();
    HtmlTag h2 = new HtmlTag("h2");
    h2.addAttribute("class", "centered");
    h2.add("Oops!  Did you forget to add to some content to this ?");
    html.add(h2.html());
    html.add(HtmlUtil.HR.html());
    return html.html();
  }

  public String executionStatus(ExecutionLog log) throws Exception {
    return log.executionStatusHtml();
  }

  public String executionStatus(CompositeExecutionLog logs) throws Exception {
    return logs.executionStatusHtml();
  }
}
