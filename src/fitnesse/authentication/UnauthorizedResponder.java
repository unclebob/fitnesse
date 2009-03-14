// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.TagGroup;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;

public class UnauthorizedResponder implements Responder {
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    SimpleResponse response = new SimpleResponse(401);
    response.addHeader("WWW-Authenticate", "Basic realm=\"FitNesse\"");

    HtmlPage page = context.htmlPageFactory.newPage();
    HtmlUtil.addTitles(page, "401 Unauthorized");
    page.main.use(makeContent(request));
    response.setContent(page.html());

    return response;
  }

  private HtmlTag makeContent(Request request) throws Exception {
    TagGroup group = new TagGroup();
    group.add(makeSimpleTag("h1", "Unauthorized"));
    group.add("<p>The requested resource: ");
    group.add(makeSimpleTag("b", request.getResource()));
    group.add(" is restricted.");
    group.add("<p> Either your credientials were not supplied or they didn't match the criteria to access this resource.");
    group.add(HtmlUtil.HR);
    group.add(makeSimpleTag("address", "FitNesse"));
    return group;
  }

  private String makeSimpleTag(String tagName, String content) throws Exception {
    HtmlTag tag = new HtmlTag(tagName);
    tag.add(content);
    return tag.html();
  }
}
