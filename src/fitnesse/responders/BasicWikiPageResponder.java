package fitnesse.responders;

import fitnesse.*;
import fitnesse.wiki.*;
import fitnesse.http.*;

// TODO This class could just be "WikiPageResponder" (already exists)
public abstract class BasicWikiPageResponder extends BasicResponder
{
  public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
    WikiPage requestedPage = getRequestedPage(request, context);

    Response response;
    if(requestedPage == null)
			response = pageNotFoundResponse(context, request);
    else
      response = responseWith(contentFrom(requestedPage));

    return response;
  }

  private WikiPage getRequestedPage(Request request, FitNesseContext context) throws Exception
  {
    WikiPagePath path = PathParser.parse(request.getResource());
    WikiPage requestedPage = context.root.getPageCrawler().getPage(context.root, path);
    return requestedPage;
  }

  protected abstract String contentFrom(WikiPage requestedPage) throws Exception;
}
