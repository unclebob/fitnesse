// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.html.template.HtmlPage;

public class ErrorResponder implements Responder {
  private Exception exception;
  private String message;
  private int statusCode;

  public ErrorResponder(Exception e) {
    exception = e;
    statusCode = 400;
  }

  public ErrorResponder(String message) {
    this(message, 400);
  }

  public ErrorResponder(String message, int statusCode) {
    this.message = message;
    this.statusCode = statusCode;
  }

  public Response makeResponse(FitNesseContext context, Request request) {
    SimpleResponse response = new SimpleResponse(statusCode);
    HtmlPage html = context.pageFactory.newPage();
    html.addTitles("Error Occurred");
    html.setMainTemplate("error");
    html.put("exception", exception);
    if (exception != null)
      html.put("exception", exception);
    if (message != null)
      html.put("message", message);
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
}
