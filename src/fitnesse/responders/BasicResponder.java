// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import java.io.IOException;

import fitnesse.FitNesseContext;
import fitnesse.authentication.InsecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.template.HtmlPage;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class BasicResponder implements SecureResponder {

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    WikiPage requestedPage = getRequestedPage(request, context);

    Response response;
    if (requestedPage == null)
      response = pageNotFoundResponse(context, request);
    else
      response = responseWith(contentFrom(context, request, requestedPage));

    return response;
  }

  protected WikiPage getPage(FitNesseContext context, Request request) {
    String resource = request.getResource();
    WikiPagePath path = PathParser.parse(resource);
    return context.getRootPage().getPageCrawler().getPage(path);
  }

  protected HtmlPage prepareResponseDocument(FitNesseContext context) {
    HtmlPage responseDocument = context.pageFactory.newPage();
    responseDocument.addTitles("Default Responder");
    responseDocument.setMainTemplate("defaultPage.vm");
    return responseDocument;
  }

  protected WikiPage getRequestedPage(Request request, FitNesseContext context) {
    WikiPagePath path = PathParser.parse(request.getResource());
    WikiPage requestedPage = context.getRootPage().getPageCrawler().getPage(path);
    return requestedPage;
  }

  protected String contentFrom(FitNesseContext context, Request request, WikiPage requestedPage) {
    return prepareResponseDocument(context).html(request);
  }

  protected Response pageNotFoundResponse(FitNesseContext context, Request request) throws Exception {
    return new NotFoundResponder().makeResponse(context, request);
  }

  protected Response responseWith(String content) throws IOException {
    SimpleResponse response = new SimpleResponse();
    response.setContentType(getContentType());
    response.setContent(content);
    return response;
  }

  protected String getContentType() {
    return Response.Format.HTML.getContentType();
  }

  @Override
  public SecureOperation getSecureOperation() {
    return new InsecureOperation();
  }
}
