// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.authentication.InsecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public abstract class BasicResponder implements SecureResponder {

  public Response makeResponse(FitNesseContext context, Request request) {
    WikiPage requestedPage = getRequestedPage(request, context);

    Response response;
    if (requestedPage == null)
      response = pageNotFoundResponse(context, request);
    else
      response = responseWith(contentFrom(context, request, requestedPage));

    return response;
  }

  protected WikiPage getRequestedPage(Request request, FitNesseContext context) {
    WikiPagePath path = PathParser.parse(request.getResource());
    WikiPage requestedPage = context.getRootPage().getPageCrawler().getPage(path);
    return requestedPage;
  }

  protected abstract String contentFrom(FitNesseContext context, Request request, WikiPage requestedPage);

  protected Response pageNotFoundResponse(FitNesseContext context, Request request) {
    return new NotFoundResponder().makeResponse(context, request);
  }

  protected Response responseWith(String content) {
    SimpleResponse response = new SimpleResponse();
    response.setContentType(getContentType());
    response.setContent(content);
    return response;
  }

  protected String getContentType() {
    return Response.Format.HTML.getContentType();
  }

  public SecureOperation getSecureOperation() {
    return new InsecureOperation();
  }
}
