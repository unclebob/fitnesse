// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.html.template.HtmlPage;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.wiki.WikiPage;

public class DefaultResponder extends BasicResponder {

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    return responseWith(contentFrom(context, request, null));
  }

  @Override
  protected String contentFrom(FitNesseContext context, Request request, WikiPage requestedPage) {
    return prepareResponseDocument(context).html();
  }

  private HtmlPage prepareResponseDocument(FitNesseContext context) {
    HtmlPage responseDocument = context.pageFactory.newPage();
    responseDocument.addTitles("Default Responder");
    responseDocument.setMainTemplate("defaultPage.vm");
    return responseDocument;
  }
}
