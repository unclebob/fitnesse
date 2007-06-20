// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.authentication.*;
import fitnesse.html.*;
import fitnesse.http.*;
import fitnesse.wiki.*;

public class WikiPageResponder implements SecureResponder
{
	protected WikiPage page;
	protected PageData pageData;
	protected String pageTitle;
	protected Request request;
	protected PageCrawler crawler;

	public WikiPageResponder()
	{
	}

	public WikiPageResponder(WikiPage page) throws Exception
	{
		this.page = page;
		pageData = page.getData();
	}

	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		String resource = request.getResource();

		if("".equals(resource))
			resource = "FrontPage";

		loadPage(resource, context);
		if(page == null)
			return new NotFoundResponder().makeResponse(context, request);
		loadPageData();

		pageTitle = PathParser.render(crawler.getFullPath(page));
		String html = makeHtml(context);

		SimpleResponse response = new SimpleResponse();
		response.setMaxAge(0);
		response.setContent(html);

		return response;
	}

	protected void loadPageData() throws Exception
	{
		pageData = page.getData();
	}

	protected void loadPage(String resource, FitNesseContext context) throws Exception
	{
		WikiPagePath path = PathParser.parse(resource);
		crawler = context.root.getPageCrawler();
		crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
		page = crawler.getPage(context.root, path);
	}

	public String makeHtml(FitNesseContext context) throws Exception
	{
		WikiPage page = pageData.getWikiPage();
		HtmlPage html = context.htmlPageFactory.newPage();
		WikiPagePath fullPath = page.getPageCrawler().getFullPath(page);
		String fullPathName = PathParser.render(fullPath);
		html.title.use(fullPathName);
		html.header.use(HtmlUtil.makeBreadCrumbsWithCurrentPageNotLinked(fullPathName));
		html.actions.use(HtmlUtil.makeActions(pageData));
		html.main.use(HtmlUtil.addHeaderAndFooter(page, HtmlUtil.testableHtml(pageData)));

		handleSpecialProperties(html, page);

		return html.html();
	}

	private void handleSpecialProperties(HtmlPage html, WikiPage page) throws Exception
	{
		WikiImportProperty.handleImportProperties(html, page, pageData);
	}

	public SecureOperation getSecureOperation()
	{
		return new SecureReadOperation();
	}
}
