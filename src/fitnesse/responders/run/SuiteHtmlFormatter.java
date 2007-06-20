// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.run;

import fit.Counts;
import fitnesse.html.*;

public class SuiteHtmlFormatter extends TestHtmlFormatter
{
	private static final String cssSuffix1 = "1";
	private static final String cssSuffix2 = "2";

	private Counts pageCounts = new Counts();

	private String cssSuffix = cssSuffix1;
	private TagGroup testResultsGroup = new TagGroup();
	private HtmlTag currentOutputDiv;

	public SuiteHtmlFormatter(HtmlPage page) throws Exception
	{
		super(page);

		HtmlTag outputTitle = new HtmlTag("h2", "Test Output");
		outputTitle.addAttribute("class", "centered");
		testResultsGroup.add(outputTitle);
	}

	protected String testPageSummary()
	{
		return "<strong>Test Pages:</strong> " + pageCounts.toString() + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	}

	public void setPageAssertions(Counts pageCounts)
	{
		this.pageCounts = pageCounts;
	}

	public String acceptResults(String relativePageName, Counts counts) throws Exception
	{
		switchCssSuffix();
		HtmlTag mainDiv = HtmlUtil.makeDivTag("alternating_row_" + cssSuffix);

		mainDiv.add(HtmlUtil.makeSpanTag("test_summary_results " + cssClassFor(counts), counts.toString()));

		HtmlTag link = HtmlUtil.makeLink("#" + relativePageName, relativePageName);
		link.addAttribute("class", "test_summary_link");
		mainDiv.add(link);

		pageCounts.tallyPageCounts(counts);

		return mainDiv.html(2);
	}

	public void startOutputForNewTest(String relativePageName, String qualifiedPageName) throws Exception
	{
		HtmlTag pageNameBar = HtmlUtil.makeDivTag("test_output_name");
		HtmlTag anchor = HtmlUtil.makeLink(qualifiedPageName, relativePageName);
		anchor.addAttribute("id", relativePageName);
		pageNameBar.add(anchor);
		testResultsGroup.add(pageNameBar);
		currentOutputDiv = HtmlUtil.makeDivTag("alternating_block_" + cssSuffix);
		testResultsGroup.add(currentOutputDiv);
	}

	public void acceptOutput(String output)
	{
		currentOutputDiv.add(output);
	}

	public String testOutput() throws Exception
	{
		return testResultsGroup.html();
	}

	private void switchCssSuffix()
	{
		if(cssSuffix1.equals(cssSuffix))
			cssSuffix = cssSuffix2;
		else
			cssSuffix = cssSuffix1;
	}
}
