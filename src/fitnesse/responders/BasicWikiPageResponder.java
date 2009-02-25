// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

// TODO This class could just be "WikiPageResponder" (already exists)
public abstract class BasicWikiPageResponder extends BasicResponder {
  protected Request request;

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    this.request = request;
    WikiPage requestedPage = getRequestedPage(request, context);

    Response response;
    if (requestedPage == null)
      response = pageNotFoundResponse(context, request);
    else
      response = responseWith(contentFrom(requestedPage));

    return response;
  }

  private WikiPage getRequestedPage(Request request, FitNesseContext context) throws Exception {
    WikiPagePath path = PathParser.parse(request.getResource());
    WikiPage requestedPage = context.root.getPageCrawler().getPage(context.root, path);
    return requestedPage;
  }

  protected abstract String contentFrom(WikiPage requestedPage) throws Exception;
}
