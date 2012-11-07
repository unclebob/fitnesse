// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.templateUtilities.HtmlPage;
import fitnesse.wikitext.parser.WikiWordPath;

// TODO: Some of this code may now be obsolete, because this responder is no longer used for some
// scenarios (we skip directly to an EditResponder...).
public class NotFoundResponder implements Responder {
  private String resource;

  public Response makeResponse(FitNesseContext context, Request request) {
    SimpleResponse response = new SimpleResponse(404);
    resource = request.getResource();

    response.setContent(makeHtml(context));
    return response;
  }

  private String makeHtml(FitNesseContext context) {
    HtmlPage page = context.pageFactory.newPage();
    HtmlUtil.addTitles(page, "Not Found:" + resource);
    page.put("name", resource);
    page.put("shouldCreate", WikiWordPath.isWikiWord(resource));
    page.setMainTemplate("notFoundPage.vm");
    return page.html();
  }
}
