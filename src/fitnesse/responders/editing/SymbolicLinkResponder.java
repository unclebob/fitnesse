package fitnesse.responders.editing;

import fitnesse.*;
import fitnesse.responders.*;
import fitnesse.wiki.*;
import fitnesse.http.*;

public class SymbolicLinkResponder implements Responder
{
	private Response response;
	private String resource;
	private PageCrawler crawler;
	private FitNesseContext context;

	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		resource = request.getResource();
		this.context = context;
		crawler = context.root.getPageCrawler();
		WikiPage page = crawler.getPage(context.root, PathParser.parse(resource));
		if(page == null)
			return new NotFoundResponder().makeResponse(context, request);


		response = new SimpleResponse();
		if(request.hasInput("removal"))
			removeSymbolicLink(request, page);
		else
			addSymbolicLink(request, page);

		return response;
	}

	private void setRedirect(String resource)
	{
		response.redirect(resource + "?properties");
	}

	private void removeSymbolicLink(Request request, WikiPage page) throws Exception
	{
		String linkToRemove = (String)request.getInput("removal");

		PageData data = page.getData();
		data.getProperties().removeSymbolicLink(linkToRemove);
		page.commit(data);
		setRedirect(resource);
	}

	private void addSymbolicLink(Request request, WikiPage page) throws Exception
	{
		String linkName = (String)request.getInput("linkName");
		String linkPath = (String)request.getInput("linkPath");

    if(!crawler.pageExists(context.root, PathParser.parse(linkPath)))
    {
	    response = new ErrorResponder("The page to which you are attemting to link, " + linkPath + ", doesn't exist.").makeResponse(context, null);
	    response.setStatus(404);
    }
    else if(page.hasChildPage(linkName))
    {
	    response = new ErrorResponder(resource + " already has a child named " + linkName + ".").makeResponse(context, null);
	    response.setStatus(412);
    }
		else
    {
			PageData data = page.getData();
			data.getProperties().addSymbolicLink(linkName, PathParser.parse(linkPath));
			page.commit(data);
			setRedirect(resource);
    }
	}
}
