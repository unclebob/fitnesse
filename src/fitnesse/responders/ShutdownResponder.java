// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import java.util.logging.Level;
import java.util.logging.Logger;

import fitnesse.FitNesseContext;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;

public class ShutdownResponder implements SecureResponder {
  private static final Logger LOG = Logger.getLogger(ShutdownResponder.class.getName());

  @Override
  public Response makeResponse(final FitNesseContext context, Request request) throws Exception {
    SimpleResponse response = new SimpleResponse();

    HtmlPage html = context.pageFactory.newPage();
    html.setTitle("Shutdown");
    html.setPageTitle(new PageTitle("Shutdown"));

    html.setMainTemplate("shutdownPage.vm");
    response.setContent(html.html());

    Thread shutdownThread = new Thread() {
      @Override
      public void run() {
        try {
          context.fitNesse.stop();
        }
        catch (Exception e) {
          LOG.log(Level.WARNING, "Error while stopping FitNesse", e);
        }
      }
    };
    shutdownThread.start();

    return response;
  }

  @Override
  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
