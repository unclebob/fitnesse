// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import java.util.regex.Pattern;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wikitext.widgets.WikiWordWidget;

// TODO: Some of this code may now be obsolete, because this responder is no longer used for some

// scenarios (we skip directly to an EditResponder...).
public class NotFoundResponder implements Responder {
  private String resource;

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    SimpleResponse response = new SimpleResponse(404);
    resource = request.getResource();

    response.setContent(makeHtml(context));
    return response;
  }

  private String makeHtml(FitNesseContext context) throws Exception {
    HtmlPage page = context.htmlPageFactory.newPage();
    HtmlUtil.addTitles(page, "Not Found:" + resource);
    page.main.use(makeRightColumn(resource));
    return page.html();
  }

  private String makeRightColumn(String name) throws Exception {
    StringBuffer buffer = new StringBuffer();
    buffer.append("The requested resource: <i>" + name + "</i> was not found.");
    if (Pattern.matches(WikiWordWidget.REGEXP, name)) {
      makeCreateThisPageWithButton(name, buffer);
    }
    return buffer.toString();
  }

  private void makeCreateThisPageWithButton(String name, StringBuffer buffer)
    throws Exception {
    HtmlTag createPageForm = HtmlUtil.makeFormTag("POST", name + "?edit", "createPageForm");
    HtmlTag submitButton = HtmlUtil.makeInputTag("submit", "createPageSubmit", "Create This Page");
    submitButton.addAttribute("accesskey", "c");
    createPageForm.add(submitButton);
    buffer.append(HtmlUtil.BR);
    buffer.append(HtmlUtil.BR);
    buffer.append(createPageForm.html());
  }

}
