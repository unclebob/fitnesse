// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import java.io.File;

import fitnesse.FitNesseContext;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.TagGroup;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;

public class DeleteConfirmationResponder implements SecureResponder {
  private String resource;

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    SimpleResponse response = new SimpleResponse();
    resource = request.getResource();
    String filename = (String) request.getInput("filename");
    response.setContent(makeDirectoryListingPage(filename, context));
    response.setLastModifiedHeader("Delete");
    return response;
  }

  private String makeDirectoryListingPage(String filename, FitNesseContext context) throws Exception {
    HtmlPage page = context.htmlPageFactory.newPage();
    page.title.use("Delete File(s): ");
    page.header.use(HtmlUtil.makeBreadCrumbsWithPageType(resource + filename, "/", "Delete File"));
    page.main.use(makeConfirmationHTML(filename, context));

    return page.html();
  }

  private HtmlTag makeConfirmationHTML(String filename, FitNesseContext context) throws Exception {
    String pathname = context.rootPagePath + "/" + resource + filename;
    File file = new File(pathname);
    boolean isDir = file.isDirectory();

    TagGroup group = new TagGroup();
    group.add(messageText(filename, isDir, file));

    group.add(HtmlUtil.BR);
    group.add(HtmlUtil.BR);
    group.add(HtmlUtil.BR);
    group.add(makeYesForm(filename));
    group.add(makeNoForm());
    group.add(HtmlUtil.NBSP);
    group.add(HtmlUtil.NBSP);
    return group;
  }

  private String messageText(String filename, boolean dir, File file) {
    String message = "Are you sure you would like to delete <b>" + filename + "</b> ";
    if (dir)
      message += " and all " + file.listFiles().length + " files inside";

    return message + "?";
  }

  private HtmlTag makeNoForm() {
    HtmlTag noForm = HtmlUtil.makeFormTag("get", "/" + resource);
    HtmlTag noButton = HtmlUtil.makeInputTag("submit", "", "No");
    noButton.addAttribute("accesskey", "n");
    noForm.add(noButton);
    return noForm;
  }

  private HtmlTag makeYesForm(String filename) {
    HtmlTag yesForm = HtmlUtil.makeFormTag("get", "/" + resource);
    HtmlTag yesButton = HtmlUtil.makeInputTag("submit", "", "Yes");
    yesButton.addAttribute("accesskey", "y");
    yesForm.add(yesButton);
    yesForm.add(HtmlUtil.makeInputTag("hidden", "responder", "deleteFile"));
    yesForm.add(HtmlUtil.makeInputTag("hidden", "filename", filename));
    return yesForm;
  }

  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
