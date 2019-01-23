// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import java.io.File;

import fitnesse.FitNesseContext;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;

public class DeleteConfirmationResponder implements SecureResponder {
  private String resource;

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    SimpleResponse response = new SimpleResponse();
    resource = request.getResource();
    String filename = request.getInput("filename");
    response.setContent(makeDirectoryListingPage(resource, filename, context, request));
    return response;
  }

  private String makeDirectoryListingPage(String pageName, String filename, FitNesseContext context, Request request) {
    HtmlPage page = context.pageFactory.newPage();
    page.setTitle("Delete File(s)");
    page.setPageTitle(new PageTitle("Delete File", resource + filename, "/"));
    page.put("resource", resource);
    makeConfirmationHTML(page, filename, context);
    page.setMainTemplate("deleteConfirmation");

    return page.html(request);
  }

  private void makeConfirmationHTML(HtmlPage page, String filename, FitNesseContext context) {
    String pathname = context.getRootPagePath() + "/" + resource + filename;
    File file = new File(pathname);
    boolean isDir = file.isDirectory();

    page.put("filename", filename);
    page.put("isDir", isDir);
    if (isDir) {
      page.put("nFiles", file.listFiles().length);
    }
  }

  @Override
  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
