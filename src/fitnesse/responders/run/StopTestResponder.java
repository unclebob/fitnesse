// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.BasicResponder;
import fitnesse.responders.templateUtilities.HtmlPage;

public class StopTestResponder extends BasicResponder {

  String testId = null;
  
  public Response makeResponse(FitNesseContext context, Request request) {
    SimpleResponse response = new SimpleResponse();
    
    if (request.hasInput("id")) {
      testId = request.getInput("id").toString();
    }
    
    response.setContent(html(context));

    return response;
  }

  private String html(FitNesseContext context) {
    HtmlPage page = context.pageFactory.newPage();
    HtmlUtil.addTitles(page, "Stopping tests");
    page.put("testId", testId);
    page.put("runningTestingTracker", context.runningTestingTracker);
    page.setMainTemplate("stopTestPage.vm");
    return page.html();
  }
  
}
