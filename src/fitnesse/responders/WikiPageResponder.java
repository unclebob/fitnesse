// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders;

import fitnesse.http.*;
import fitnesse.wiki.*;
import fitnesse.*;
import fitnesse.authentication.*;
import fitnesse.html.*;

public class WikiPageResponder implements SecureResponder
{
	protected WikiPage page;
	protected PageData pageData;
	protected String pageTitle;
	protected Request request;

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

		WikiPagePath path = PathParser.parse(resource);
		PageCrawler crawler = context.root.getPageCrawler();
		crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
		page = crawler.getPage(context.root, path);
		if(page == null)
			return new NotFoundResponder().makeResponse(context, request);
		pageData = page.getData();

		pageTitle = PathParser.render(crawler.getFullPath(page));
		String html = makeHtml(context);

		SimpleResponse response = new SimpleResponse();
		response.setMaxAge(0);
		response.setContent(html);

		return response;
	}

	public String makeHtml(FitNesseContext context) throws Exception
	{
		WikiPage page = pageData.getWikiPage();
		HtmlPage html = context.htmlPageFactory.newPage();
		WikiPagePath fullPath = page.getPageCrawler().getFullPath(page);
		String fullPathName = PathParser.render(fullPath);
		html.title.use(fullPathName);
		html.header.use(HtmlUtil.makeBreadCrumbs(fullPathName));
		html.actions.use(HtmlUtil.makeActions(pageData));
		html.main.use(HtmlUtil.addHeaderAndFooter(page, HtmlUtil.testableHtml(pageData)));

		if(pageData.hasAttribute("WikiImportSource"))
			html.body.addAttribute("class", "imported");
		else if(page instanceof ProxyPage)
			html.body.addAttribute("class", "virtual");

		return html.html();
	}

	public SecureOperation getSecureOperation()
	{
		return new SecureReadOperation();
	}
}
