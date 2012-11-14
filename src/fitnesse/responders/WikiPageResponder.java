// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.HtmlUtil;
import fitnesse.html.SetupTeardownAndLibraryIncluder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.editing.EditResponder;
import fitnesse.responders.templateUtilities.HtmlPage;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.VirtualEnabledPageCrawler;
import fitnesse.wiki.WikiImportProperty;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageActions;
import fitnesse.wiki.WikiPagePath;

public class WikiPageResponder implements SecureResponder {
  private WikiPage page;
  private PageData pageData;
  private PageCrawler crawler;

  public Response makeResponse(FitNesseContext context, Request request) {
    loadPage(request.getResource(), context);
    if (page == null)
      return notFoundResponse(context, request);
    else
      return makePageResponse(context);
  }

  protected void loadPage(String pageName, FitNesseContext context) {
    WikiPagePath path = PathParser.parse(pageName);
    crawler = context.root.getPageCrawler();
    crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
    page = crawler.getPage(context.root, path);
    if (page != null)
      pageData = page.getData();
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

  private SimpleResponse makePageResponse(FitNesseContext context) {
      PathParser.render(crawler.getFullPath(page));
      String html = makeHtml(context);

      SimpleResponse response = new SimpleResponse();
      response.setMaxAge(0);
      response.setContent(html);
      return response;
  }

  public String makeHtml(FitNesseContext context) {
    WikiPage page = pageData.getWikiPage();
    HtmlPage html = context.pageFactory.newPage();
    WikiPagePath fullPath = page.getPageCrawler().getFullPath(page);
    String fullPathName = PathParser.render(fullPath);
    PageTitle pt = new PageTitle(fullPath);
    
    String tags = "";
    if (pageData != null) {
      tags = pageData.getAttribute(PageData.PropertySUITES);
    }
    pt.setPageTags(tags);
    
    html.setTitle(fullPathName);
    html.setPageTitle(pt.notLinked());
    
    html.setNavTemplate("wikiNav.vm");
    html.put("actions", new WikiPageActions(page));
    html.put("helpText", pageData.getProperties().get(PageData.PropertyHELP));

    SetupTeardownAndLibraryIncluder.includeInto(pageData, true);

    html.setMainTemplate("wikiPage");
    html.setFooterTemplate("wikiFooter");
    html.put("content", new WikiPageRenderer());
    html.put("footerContent", new WikiPageFooterRenderer());
    handleSpecialProperties(html, page);
    return html.html();
  }

  private void handleSpecialProperties(HtmlPage html, WikiPage page) {
    WikiImportProperty.handleImportProperties(html, page, pageData);
  }

  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }

  public class WikiPageRenderer {
    public String render() {
        return HtmlUtil.makePageHtml(pageData);
    }
  }

  public class WikiPageFooterRenderer {
    public String render() {
        return HtmlUtil.makePageFooterHtml(pageData);
    }
  }

}
