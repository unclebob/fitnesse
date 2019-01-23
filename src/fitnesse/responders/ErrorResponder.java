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

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    SimpleResponse response = new SimpleResponse(statusCode);
    HtmlPage html = context.pageFactory.newPage();
    html.addTitles("Error Occurred");
    html.setMainTemplate("error");
    html.put("exception", exception);
    if (exception != null)
      html.put("exception", exception);
    if (message != null)
      html.put("message", message);
    response.setContent(html.html(request));

    return response;
  }

  public static String makeExceptionString(Throwable e) {
    StringBuilder builder = new StringBuilder();
    builder.append(e.toString()).append("\n");
    for (StackTraceElement stackTraceElement : e.getStackTrace()) {
      builder.append("\t")
              .append(stackTraceElement)
              .append("\n");
    }

    return builder.toString();
  }
}
