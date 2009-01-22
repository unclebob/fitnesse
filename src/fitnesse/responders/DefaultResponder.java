// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;

public class DefaultResponder extends BasicResponder {
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    String content = prepareResponseDocument(context).html();
    return responseWith(content);
  }

  private HtmlPage prepareResponseDocument(FitNesseContext context) {
    HtmlPage responseDocument = context.htmlPageFactory.newPage();
    HtmlUtil.addTitles(responseDocument, "Default Responder");
    responseDocument.main.use(content());
    return responseDocument;
  }

  private String content() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("This is the DefaultResponder page.<br/>");
    buffer.append("Because you can see this page something has gone wrong.<br/>");
    buffer.append("If you continue to get this page, please let us know how.<br/>");
    buffer.append("Thanks,<br/>");
    buffer.append("<ul><li><a href=\"mailto:unclebob@objectmentor.com\">The FitNesse development team.</a></ul>");
    return buffer.toString();
  }

}
