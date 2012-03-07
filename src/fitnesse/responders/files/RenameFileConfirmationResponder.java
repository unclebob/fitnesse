// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import fitnesse.FitNesseContext;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.templateUtilities.HtmlPage;
import fitnesse.responders.templateUtilities.PageTitle;

public class RenameFileConfirmationResponder implements SecureResponder {
  private String resource;

  public Response makeResponse(FitNesseContext context, Request request) {
    resource = request.getResource();
    String filename = (String) request.getInput("filename");
    
    HtmlPage page = context.htmlPageFactory.newPage();
    page.setTitle("Rename " + filename);
    page.setPageTitle(new PageTitle("Rename File", resource + filename, "/"));
    page.setMainTemplate("renameFileConfirmation.vm");
    page.put("filename", filename);

    SimpleResponse response = new SimpleResponse();
    response.setContent(page.html());
    return response;
  }

  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
