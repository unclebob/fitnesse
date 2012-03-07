// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.refactoring;

import fitnesse.FitNesseContext;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.templateUtilities.HtmlPage;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.wiki.PathParser;

public class RefactorPageResponder implements SecureResponder {

  public Response makeResponse(FitNesseContext context, Request request) {
    String resource = request.getResource();

    HtmlPage page = context.pageFactory.newPage();

    page.setMainTemplate("refactorForm");
    page.setTitle("Refactor: " + resource);
    page.setPageTitle(new PageTitle("Refactor", PathParser.parse(resource)));
    page.put("refactoredRootPage", resource);
    page.put("request", request);
    page.put("type", request.getInput("type"));
    SimpleResponse response = new SimpleResponse();
    response.setContent(page.html());
    return response;
  }

  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
