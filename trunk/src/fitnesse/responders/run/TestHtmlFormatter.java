// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.run;

import fit.Counts;
import fitnesse.html.*;

public class TestHtmlFormatter
{
	private HtmlPage page;

	public TestHtmlFormatter(HtmlPage page) throws Exception
	{
		this.page = page;
		page.main.use(HtmlPage.BreakPoint);
		page.divide();
	}

	public String head() throws Exception
	{
		return page.preDivision + makeSummaryPlaceHolder().html();
	}

	private HtmlTag makeSummaryPlaceHolder()
	{
		HtmlTag testSummaryDiv = new HtmlTag("div", "Running Tests ...");
		testSummaryDiv.addAttribute("id", "test-summary");

		return testSummaryDiv;
	}

	public String testSummary(Counts counts) throws Exception
	{
		String summaryContent = testPageSummary();
		summaryContent += "<strong>Assertions:</strong> " + counts.toString();

		HtmlTag script = new HtmlTag("script");
		script.add("document.getElementById(\"test-summary\").innerHTML = \"" + summaryContent + "\";");
		script.add("document.getElementById(\"test-summary\").className = \"" + cssClassFor(counts) + "\";");
		return script.html();
	}

	protected String testPageSummary()
	{
		return "";
	}

	protected String cssClassFor(Counts count)
	{
		if(count.wrong > 0)
			return "fail";
		else if(count.exceptions > 0 || count.right + count.ignores == 0)
			return "error";
		else if(count.ignores > 0 && count.right == 0)
			return "ignore";
		else
			return "pass";
	}

	public String tail()
	{
		return page.postDivision;
	}

	public String messageForBlankHtml() throws Exception
	{
		TagGroup html = new TagGroup();
		HtmlTag h2 = new HtmlTag("h2");
		h2.addAttribute("class", "centered");
		h2.add("Oops!  Did you forget to add to some content to this ?");
		html.add(h2.html());
		html.add(HtmlUtil.HR.html());
		return html.html();
	}

	public String executionStatus(ExecutionLog log) throws Exception
	{
		return log.executionStatusHtml();
	}
}
