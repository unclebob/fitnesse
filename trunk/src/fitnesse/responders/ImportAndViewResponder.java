package fitnesse.responders;

import fitnesse.*;
import fitnesse.http.*;
import fitnesse.wiki.*;

public class ImportAndViewResponder implements Responder, WikiImporterClient
{
	private WikiPage page;

	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		String resource = request.getResource();

		if("".equals(resource))
			resource = "FrontPage";

		loadPage(resource, context);
		if(page == null)
			return new NotFoundResponder().makeResponse(context, request);
		loadPageData();

		SimpleResponse response = new SimpleResponse();
		response.redirect(resource);

		return response;
	}

	protected void loadPage(String resource, FitNesseContext context) throws Exception
	{
		WikiPagePath path = PathParser.parse(resource);
		PageCrawler crawler = context.root.getPageCrawler();
		crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
		page = crawler.getPage(context.root, path);
	}

	protected void loadPageData() throws Exception
	{
		PageData pageData = page.getData();

		WikiImportProperty importProperty = WikiImportProperty.createFrom(pageData.getProperties());

		if(importProperty != null)
		{
			WikiImporter importer = new WikiImporter();
			importer.setWikiImporterClient(this);
			importer.parseUrl(importProperty.getSourceUrl());
			importer.importRemotePageContent(page);
		}
	}

	public void pageImported(WikiPage localPage) throws Exception
	{
	}

	public void pageImportError(WikiPage localPage, Exception e) throws Exception
	{
		e.printStackTrace();
	}
}
