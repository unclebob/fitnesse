// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import java.io.File;

import fitnesse.FitNesseContext;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.TagGroup;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.templateUtilities.HtmlPage;
import fitnesse.responders.templateUtilities.PageTitle;

public class DeleteConfirmationResponder implements SecureResponder {
  private String resource;

  public Response makeResponse(FitNesseContext context, Request request) {
    SimpleResponse response = new SimpleResponse();
    resource = request.getResource();
    String filename = (String) request.getInput("filename");
    response.setContent(makeDirectoryListingPage(filename, context));
    response.setLastModifiedHeader("Delete");
    return response;
  }

  private String makeDirectoryListingPage(String filename, FitNesseContext context) {
    HtmlPage page = context.htmlPageFactory.newPage();
    page.setTitle("Delete File(s): ");
    page.setPageTitle(new PageTitle("Delete File", resource + filename, "/"));
    makeConfirmationHTML(page, filename, context);
    page.setMainTemplate("deleteConfirmation.vm");
        
    return page.html();
  }

  private void makeConfirmationHTML(HtmlPage page, String filename, FitNesseContext context) {
    String pathname = context.rootPagePath + "/" + resource + filename;
    File file = new File(pathname);
    boolean isDir = file.isDirectory();

    page.put("filename", filename);
    page.put("isDir", isDir);
    if (isDir) {
      page.put("nFiles", file.listFiles().length);
    }
  }

  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
