// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;

public class ErrorResponder implements Responder {
  Exception exception;
  private String message;

  public ErrorResponder(Exception e) {
    exception = e;
  }

  public ErrorResponder(String message) {
    this.message = message;
  }

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    SimpleResponse response = new SimpleResponse(400);
    HtmlPage html = context.htmlPageFactory.newPage();
    HtmlUtil.addTitles(html, "Error Occured");
    if (exception != null)
      html.main.add("<pre>" + makeExceptionString(exception) + "</pre>");
    if (message != null)
      html.main.add(makeErrorMessage());
    response.setContent(html.html());

    return response;
  }

  public static String makeExceptionString(Throwable e) {
    StringBuffer buffer = new StringBuffer();
    buffer.append(e.toString()).append("\n");
    StackTraceElement[] stackTreace = e.getStackTrace();
    for (int i = 0; i < stackTreace.length; i++)
      buffer.append("\t" + stackTreace[i]).append("\n");

    return buffer.toString();
  }

  public HtmlTag makeErrorMessage() {
    HtmlTag tag = HtmlUtil.makeDivTag("centered");
    tag.add(message);
    return tag;
  }
}
