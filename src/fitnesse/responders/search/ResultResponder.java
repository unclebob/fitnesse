// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
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

		response.add(page.preDivision + "<ul>");
		startSearching();
		response.add("</ul>\n" + getPageFooterInfo(hits) + "\n" + page.postDivision);
		response.closeAll();
	}

	public void hit(WikiPage page) throws Exception
	{
		hits++;
		String fullPathName = PathParser.render(getPageCrawler().getFullPath(page));
		response.add("<li><a href=\"" + fullPathName + "\">" + fullPathName + "</a>\n");
	}

	protected abstract String getTitle() throws Exception;

	protected abstract String getPageFooterInfo(int hits) throws Exception;

	protected abstract void startSearching() throws Exception;

	public SecureOperation getSecureOperation()
	{
		return new SecureReadOperation();
	}
}

