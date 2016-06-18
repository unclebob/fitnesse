// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import java.io.UnsupportedEncodingException;
import java.util.Map;

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

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    WikiPage page = loadPage(context, request.getResource(), request.getMap());
    if (page == null)
      return notFoundResponse(context, request);
    else
      return makePageResponse(context, page);
  }

  protected WikiPage loadPage(FitNesseContext context, String pageName, Map<String,String> inputs) {
    WikiPage page;
    if (RecentChanges.RECENT_CHANGES.equals(pageName)) {
      page = context.recentChanges.toWikiPage(context.getRootPage());
    } else {
      WikiPagePath path = PathParser.parse(pageName);
      PageCrawler crawler = context.getRootPage(inputs).getPageCrawler();
      page = crawler.getPage(path);
    }
    return page;
  }

  private Response notFoundResponse(FitNesseContext context, Request request) throws Exception {
    if (dontCreateNonExistentPage(request))
      return new NotFoundResponder().makeResponse(context, request);
    return new EditResponder().makeResponseForNonExistentPage(context, request);
  }

  private boolean dontCreateNonExistentPage(Request request) {
    String dontCreate = request.getInput("dontCreatePage");
    return dontCreate != null && (dontCreate.isEmpty() || Boolean.parseBoolean(dontCreate));
  }

  private SimpleResponse makePageResponse(FitNesseContext context, WikiPage page) throws UnsupportedEncodingException {
      String html = makeHtml(context, page);

      SimpleResponse response = new SimpleResponse();
      response.setMaxAge(0);
      response.setContent(html);
      return response;
  }

  public String makeHtml(FitNesseContext context, WikiPage page) {
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

    if (WikiPageUtil.isTestPage(page)) {
      // Add test url inputs to context's variableSource.
      WikiTestPage testPage = new TestPageWithSuiteSetUpAndTearDown(page);
      html.put("content", new WikiTestPageRenderer(testPage));
    } else {
      html.put("content", new WikiPageRenderer(page));
    }

    html.setMainTemplate("wikiPage");
    html.setFooterTemplate("wikiFooter");
    html.put("footerContent", new WikiPageFooterRenderer(page));
    handleSpecialProperties(html, page);
    return html.html();
  }

  private void handleSpecialProperties(HtmlPage html, WikiPage page) {
    WikiImportingResponder.handleImportProperties(html, page);
  }

  @Override
  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }

  public static class WikiPageRenderer {
    private WikiPage page;

    public WikiPageRenderer(WikiPage page){
      this.page = page;
    }

    public String render() {
        return WikiPageUtil.makePageHtml(page);
    }
  }

  public static class WikiTestPageRenderer {
    private WikiTestPage page;

    public WikiTestPageRenderer(WikiTestPage page){
      this.page = page;
    }

    public String render() {
      return WikiTestPageUtil.makePageHtml(page);
    }
  }

  public class WikiPageFooterRenderer {
    private WikiPage page;

    public WikiPageFooterRenderer(WikiPage page){
      this.page = page;
    }

    public String render() {
        return WikiPageUtil.getFooterPageHtml(page);
    }
  }

}
