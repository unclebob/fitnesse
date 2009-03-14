// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import fitnesse.FitNesseContext;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;

public class RenameFileConfirmationResponder implements SecureResponder {
  private String resource;

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    SimpleResponse response = new SimpleResponse();
    resource = request.getResource();
    String filename = (String) request.getInput("filename");
    response.setContent(makePageContent(filename, context));
    return response;
  }

  private String makePageContent(String filename, FitNesseContext context) throws Exception {
    HtmlPage page = context.htmlPageFactory.newPage();
    page.title.use("Rename " + filename);
    page.header.use(HtmlUtil.makeBreadCrumbsWithPageType(resource + filename, "/", "Rename File"));
    page.main.use(makeRenameFormHTML(filename));

    return page.html();
  }

  private HtmlTag makeRenameFormHTML(String filename) throws Exception {
    HtmlTag form = HtmlUtil.makeFormTag("get", "/" + resource);
    form.add(HtmlUtil.makeInputTag("hidden", "responder", "renameFile"));

    form.add("Rename " + HtmlUtil.makeBold(filename).html() + " to ");
    form.add(HtmlUtil.BR);
    form.add(HtmlUtil.BR);
    form.add(HtmlUtil.BR);
    form.add(HtmlUtil.makeInputTag("text", "newName", filename));
    form.add(HtmlUtil.makeInputTag("submit", "renameFile", "Rename"));
    form.add(HtmlUtil.makeInputTag("hidden", "filename", filename));

    return form;
  }

  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
