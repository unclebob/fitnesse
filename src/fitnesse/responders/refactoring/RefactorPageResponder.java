// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.refactoring;

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

public class RefactorPageResponder implements SecureResponder {
  private String resource;

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    resource = request.getResource();
    SimpleResponse response = new SimpleResponse();
    response.setContent(html(context));
    return response;
  }

  public String html(FitNesseContext context) throws Exception {
    HtmlPage html = context.htmlPageFactory.newPage();
    html.title.use("Refactor: " + resource);
    html.header.use(HtmlUtil.makeBreadCrumbsWithPageType(resource, "Refactor"));
    html.main.use(mainContent());
    return html.html();
  }

  private HtmlTag mainContent() throws Exception {
    TagGroup group = new TagGroup();
    group.add(deletePageForm());
    group.add(renamePageForm());
    group.add(movePageForm());
    return group;
  }

  private HtmlTag deletePageForm() throws Exception {
    TagGroup group = new TagGroup();
    group.add(makeHeaderTag("Delete:"));
    group.add("Delete this entire sub-wiki.");
    group.add(makeDeletePageForm());
    return group;
  }

  private HtmlTag makeHeaderTag(String content) throws Exception {
    return new HtmlTag("h3", content);
  }

  private HtmlTag makeDeletePageForm() throws Exception {
    HtmlTag form = HtmlUtil.makeFormTag("get", resource);
    form.add(HtmlUtil.makeInputTag("submit", "", "Delete Page"));
    form.add(HtmlUtil.makeInputTag("hidden", "responder", "deletePage"));
    return form;
  }

  private HtmlTag movePageForm() throws Exception {
    TagGroup group = new TagGroup();
    group.add(HtmlUtil.BR);
    group.add(makeHeaderTag("Move:"));
    group.add(makeMovePageForm());
    return group;
  }

  private HtmlTag makeMovePageForm() throws Exception {
    HtmlTag form = HtmlUtil.makeFormTag("get", resource);
    form.add(HtmlUtil.makeInputTag("hidden", "responder", "movePage"));
    form.add("New Location: ");
    HtmlTag input = HtmlUtil.makeInputTag("text", "newLocation", "");
    input.addAttribute("size", "80");
    form.add(input);
    form.add(HtmlUtil.BR);
    form.add(HtmlUtil.makeInputTag("checkbox", "refactorReferences"));
    form.add(" - Find all references to this page and change them accordingly (May take several minutes)");
    form.add(HtmlUtil.BR);
    form.add(HtmlUtil.makeInputTag("submit", "", "Move Page"));
    return form;
  }

  private HtmlTag renamePageForm() throws Exception {
    TagGroup group = new TagGroup();
    group.add(HtmlUtil.BR);
    group.add(makeHeaderTag("Rename:"));
    group.add(makeRenamePageForm());
    return group;
  }

  private HtmlTag makeRenamePageForm() throws Exception {
    HtmlTag form = HtmlUtil.makeFormTag("get", resource);
    form.add(HtmlUtil.makeInputTag("hidden", "responder", "renamePage"));
    form.add("  New Name: ");
    //form.add(HtmlUtil.makeInputTag("text", "newName", ""));
    HtmlTag input = HtmlUtil.makeInputTag("text", "newName", "");
    input.addAttribute("size", "50");
    form.add(input);
    form.add(HtmlUtil.BR);
    form.add(HtmlUtil.makeInputTag("checkbox", "refactorReferences"));
    form.add(" - Find all references to this page and change them accordingly (May take several minutes)");
    form.add(HtmlUtil.BR);
    form.add(HtmlUtil.makeInputTag("submit", "", "Rename Page"));
    return form;
  }

  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
