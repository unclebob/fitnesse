// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.revisioncontrol;

import fitnesse.FitNesseContext;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.BasicResponder;
import fitnesse.responders.NotFoundResponder;
import fitnesse.revisioncontrol.RevisionControlOperation;
import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.SymbolicPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public abstract class RevisionControlResponder extends BasicResponder {
  private final RevisionControlOperation operation;
  private boolean exceptionHasOccurred = false;
  private String returnMsg;

  protected RevisionControlResponder(RevisionControlOperation operation) {
    this.operation = operation;
    this.returnMsg = "Operation: '" + operation.getName();
  }

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    WikiPage root = context.root;
    PageCrawler crawler = root.getPageCrawler();
    String resource = request.getResource();
    WikiPagePath path = PathParser.parse(resource);

    WikiPage page = crawler.getPage(root, path);
    if (page == null)
      return new NotFoundResponder().makeResponse(context, request);

    page = resolveSymbolicLinks(page);

    SimpleResponse response = new SimpleResponse();
    response.setMaxAge(0);
    if (!(page instanceof FileSystemPage)) {
      response.setContent(makeHtml(resource, context, invalidWikiPageContent(resource)));
      return response;
    }
    executeRevisionControlOperation((FileSystemPage) page);
    response.setContent(makeHtml(resource, context, content(resource, returnMsg)));

    return response;
  }

  private WikiPage resolveSymbolicLinks(WikiPage page) throws Exception {
    while (page instanceof SymbolicPage)
      page = ((SymbolicPage) page).getRealPage();
    return page;
  }

  protected void executeRevisionControlOperation(FileSystemPage page) {
    try {
      beforeOperation(page);
      if (!exceptionHasOccurred) {
        performOperation(page);
        returnMsg += "' was successful.";
      }
    } catch (Exception e) {
      e.printStackTrace();
      returnMsg += "' failed. Following exception occured: " + e.getClass().getName() + ": " + e.getMessage();
      exceptionHasOccurred = true;
    }
  }

  protected void beforeOperation(FileSystemPage page) throws Exception {
  }

  protected abstract void performOperation(FileSystemPage page) throws Exception;

  protected String responseMessage(String resource) throws Exception {
    return "Click " + HtmlUtil.makeLink(resource, "here").html() + " to view the page.";
  }

  private String makeHtml(String resource, FitNesseContext context, String content) throws Exception {
    HtmlPage html = context.htmlPageFactory.newPage();
    html.title.use(operation.getName() + " " + resource);
    html.header.use(HtmlUtil.makeBreadCrumbsWithPageType(resource, operation.getName() + " Page"));
    html.main.use(content);
    return html.html();
  }

  private String content(String resource, String result) throws Exception {
    StringBuffer buffer = new StringBuffer("Attempted to '" + operation.getName() + "' the page '");
    buffer.append(resource);
    buffer.append("'. The result was:<br/><br/><pre>");
    buffer.append(result);
    buffer.append("</pre><br/>");
    buffer.append(responseMessage(resource));
    return buffer.toString();
  }

  private String invalidWikiPageContent(String resource) {
    return "The page " + resource + " doesn't support '" + operation.getName() + "' operation.";
  }

}
