// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.search;

import fitnesse.html.*;
import fitnesse.components.SearchObserver;
import fitnesse.wiki.*;
import fitnesse.responders.*;
import fitnesse.authentication.*;

public abstract class ResultResponder extends ChunkingResponder implements SearchObserver, SecureResponder
{
	private int hits = 0;

	protected PageCrawler getPageCrawler()
	{
		return root.getPageCrawler();
	}

	protected void doSending() throws Exception
	{
		HtmlPage page = context.htmlPageFactory.newPage();
		String renderedPath = getRenderedPath();
		page.title.use(getTitle() + ": " + renderedPath);
		page.header.use(HtmlUtil.makeBreadCrumbsWithPageType(renderedPath, getTitle()));
		page.main.use(HtmlPage.BreakPoint);
		page.divide();

		response.add(page.preDivision);
		response.add(buildFeedbackDiv().html());
		response.add(ChunkedResultsListingUtil.getTableOpenHtml());
		response.add(buildHeaderRow().html());
		startSearching();
		response.add(ChunkedResultsListingUtil.getTableCloseHtml());
		response.add(buildFeedbackModificationScript().html());
		response.add(page.postDivision);
		response.closeAll();
	}

	private HtmlTag buildFeedbackModificationScript()
	  throws Exception
	{
		HtmlTag script = new HtmlTag("script");
		script.addAttribute("language", "javascript");
		script.add("document.getElementById(\"feedback\").innerHTML = '" + getPageFooterInfo(hits) + "'");
		return script;
	}

	private HtmlTag buildHeaderRow()
	{
		HtmlTag headerRow = new HtmlTag("tr");
		HtmlTag pageColumnHeader = new HtmlTag("td", "Page");
		pageColumnHeader.addAttribute("class", "resultsHeader");
		HtmlTag lastModifiedColumnHeader = new HtmlTag("td", "Last Modified");
		lastModifiedColumnHeader.addAttribute("class", "resultsHeader");

		headerRow.add(pageColumnHeader);
		headerRow.add(lastModifiedColumnHeader);
		return headerRow;
	}

	private HtmlTag buildFeedbackDiv()
	{
		HtmlTag feedback = new HtmlTag("div", "Searching...");
		feedback.addAttribute("id", "feedback");
		return feedback;
	}

	public void hit(WikiPage page) throws Exception
	{
		hits++;
		String fullPathName = PathParser.render(getPageCrawler().getFullPath(page));

		HtmlTag row = new HtmlTag("tr");
		row.addAttribute("class", "resultsRow" + getRow());

		HtmlTag link = new HtmlTag("a", fullPathName) ;
		link.addAttribute("href", fullPathName);

		row.add(new HtmlTag("td", link));
		row.add(new HtmlTag("td", "" + page.getData().getLastModificationTime()));
		response.add(row.html());
	}

	private int nextRow = 0;

	private int getRow()
	{
		return (nextRow++ % 2) + 1;
	}

	protected abstract String getTitle() throws Exception;

	protected abstract String getPageFooterInfo(int hits) throws Exception;

	protected abstract void startSearching() throws Exception;

	public SecureOperation getSecureOperation()
	{
		return new SecureReadOperation();
	}
}

