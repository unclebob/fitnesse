// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.FitNesseShutdownException;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.socketservice.SocketServerShutdownException;

public class ShutdownResponder implements SecureResponder {
  public Response makeResponse(final FitNesseContext context, Request request) throws SocketServerShutdownException {
    SimpleResponse response = new SimpleResponse();

    HtmlPage html = context.pageFactory.newPage();
    html.setTitle("Shutdown");
    html.setPageTitle(new PageTitle("Shutdown"));

    html.setMainTemplate("shutdownPage.vm");
    response.setContent(html.html());

    Thread shutdownThread = new Thread() {
      public void run() {
        try {
          context.fitNesse.stop();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    shutdownThread.start();

    throw new FitNesseShutdownException("FitNesse shutdown by shutdown responder", response);
  }

  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
