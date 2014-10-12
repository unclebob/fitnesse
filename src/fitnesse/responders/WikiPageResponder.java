// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.editing.EditResponder;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.testrunner.TestPageWithSuiteSetUpAndTearDown;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testrunner.WikiTestPageUtil;
import fitnesse.wiki.*;

public class WikiPageResponder implements SecureResponder {

  public Response makeResponse(FitNesseContext context, Request request) {
    WikiPage page = loadPage(context, request.getResource());
    if (page == null)
      return notFoundResponse(context, request);
    else
      return makePageResponse(context, page, request);
  }

  protected WikiPage loadPage(FitNesseContext context, String pageName) {
    WikiPage page;
    if (RecentChanges.RECENT_CHANGES.equals(pageName)) {
      page = context.recentChanges.toWikiPage(context.root);
    } else {
      WikiPagePath path = PathParser.parse(pageName);
      PageCrawler crawler = context.root.getPageCrawler();
      page = crawler.getPage(path);
    }
    return page;
  }

  private Response notFoundResponse(FitNesseContext context, Request request) {
    if (dontCreateNonExistentPage(request))
      return new NotFoundResponder().makeResponse(context, request);
    return new EditResponder().makeResponseForNonExistentPage(context, request);
  }

  private boolean dontCreateNonExistentPage(Request request) {
    String dontCreate = (String) request.getInput("dontCreatePage");
    return dontCreate != null && (dontCreate.length() == 0 || Boolean.parseBoolean(dontCreate));
  }

  private SimpleResponse makePageResponse(FitNesseContext context, WikiPage page) {
      return makePageResponse(context, page, null);
  }

  private SimpleResponse makePageResponse(FitNesseContext context, WikiPage page, Request request) {
      String html = makeHtml(context, page, request);

      SimpleResponse response = new SimpleResponse();
      response.setMaxAge(0);
      response.setContent(html);
      return response;
  }

  public String makeHtml(FitNesseContext context, WikiPage page) {
      return makeHtml(context, page, null);
  }

  public String makeHtml(FitNesseContext context, WikiPage page, Request request) {
    PageData pageData = page.getData();
    HtmlPage html = context.pageFactory.newPage();
    WikiPagePath fullPath = page.getPageCrawler().getFullPath();
    String fullPathName = PathParser.render(fullPath);
    PageTitle pt = new PageTitle(fullPath);
    
    String tags = pageData.getAttribute(PageData.PropertySUITES);

    pt.setPageTags(tags);
    
    html.setTitle(fullPathName);
    html.setPageTitle(pt.notLinked());
    
    html.setNavTemplate("wikiNav.vm");
    html.put("actions", new WikiPageActions(page));
    html.put("helpText", pageData.getProperties().get(PageData.PropertyHELP));

    if (WikiTestPage.isTestPage(page)) {
      // Add test url inputs to context's variableSource.
      WikiTestPage testPage = new TestPageWithSuiteSetUpAndTearDown(page,
              new UrlPathVariableSource(context.variableSource, request.getMap()));
      html.put("content", new WikiTestPageRenderer(testPage,request));
    } else {
      html.put("content", new WikiPageRenderer(page,request));
    }

    html.setMainTemplate("wikiPage");
    html.setFooterTemplate("wikiFooter");
    html.put("footerContent", new WikiPageFooterRenderer(page,request));
    handleSpecialProperties(html, page);
    return html.html();
  }

  private void handleSpecialProperties(HtmlPage html, WikiPage page) {
    WikiImportingResponder.handleImportProperties(html, page);
  }

  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }

  public class WikiPageRenderer {
    private WikiPage page;
    private Request request;

    WikiPageRenderer(WikiPage page){
        this(page, null);
    }

    WikiPageRenderer(WikiPage page, Request request) {
      this.page = page;
      this.request = request;
    }

    public String render() {
        return WikiPageUtil.makePageHtml(page, request);
    }
  }

  public class WikiTestPageRenderer {
    private WikiTestPage page;
    private Request request;

    WikiTestPageRenderer(WikiTestPage page){
        this(page, null);
    }

    WikiTestPageRenderer(WikiTestPage page, Request request) {
      this.page = page;
      this.request = request;
    }

    public String render() {
      return WikiTestPageUtil.makePageHtml(page, request);
    }
  }

  public class WikiPageFooterRenderer {
    private WikiPage page;
    private Request request;

    WikiPageFooterRenderer(WikiPage page){
        this(page, null);
    }

    WikiPageFooterRenderer(WikiPage page, Request request) {
      this.page = page;
      this.request = request;
    }

    public String render() {
        return WikiPageUtil.getFooterPageHtml(page,request);
    }
  }

}
