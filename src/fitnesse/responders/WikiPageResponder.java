// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlUtil;
import fitnesse.html.SetupTeardownIncluder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.editing.EditResponder;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.VirtualEnabledPageCrawler;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class WikiPageResponder implements SecureResponder {
  protected WikiPage page;
  protected PageData pageData;
  protected String pageTitle;
  protected Request request;
  protected PageCrawler crawler;

  public WikiPageResponder() {
  }

  public WikiPageResponder(WikiPage page) throws Exception {
    this.page = page;
    pageData = page.getData();
  }

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    loadPage(request.getResource(), context);
    if (page == null)
      return notFoundResponse(context, request);
    else
      return makePageResponse(context);
  }

  protected void loadPage(String pageName, FitNesseContext context) throws Exception {
    WikiPagePath path = PathParser.parse(pageName);
    crawler = context.root.getPageCrawler();
    crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
    page = crawler.getPage(context.root, path);
    if (page != null)
      pageData = page.getData();
  }

  private Response notFoundResponse(FitNesseContext context, Request request) throws Exception {
    if (dontCreateNonExistentPage(request))
      return new NotFoundResponder().makeResponse(context, request);
    return new EditResponder().makeResponseForNonExistentPage(context, request);
  }

  private boolean dontCreateNonExistentPage(Request request) {
    String dontCreate = (String) request.getInput("dontCreatePage");
    return dontCreate != null && (dontCreate.length() == 0 || Boolean.parseBoolean(dontCreate));
  }

  private SimpleResponse makePageResponse(FitNesseContext context) throws Exception {
    pageTitle = PathParser.render(crawler.getFullPath(page));
    String html = makeHtml(context);

    SimpleResponse response = new SimpleResponse();
    response.setMaxAge(0);
    response.setContent(html);
    return response;
  }

  public String makeHtml(FitNesseContext context) throws Exception {
    WikiPage page = pageData.getWikiPage();
    HtmlPage html = context.htmlPageFactory.newPage();
    WikiPagePath fullPath = page.getPageCrawler().getFullPath(page);
    String fullPathName = PathParser.render(fullPath);
    html.title.use(fullPathName);
    html.header.use(HtmlUtil.makeBreadCrumbsWithCurrentPageNotLinked(fullPathName));
    html.actions.use(HtmlUtil.makeActions(page.getActions()));
    SetupTeardownIncluder.includeInto(pageData);
    html.main.use(generateHtml(pageData));
    handleSpecialProperties(html, page);
    return html.html();
  }

  /* hook for subclasses */
  protected String generateHtml(PageData pageData) throws Exception {
    return HtmlUtil.makePageHtmlWithHeaderAndFooter(pageData);
  }

  private void handleSpecialProperties(HtmlPage html, WikiPage page) throws Exception {
    WikiImportProperty.handleImportProperties(html, page, pageData);
  }

  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }
}
