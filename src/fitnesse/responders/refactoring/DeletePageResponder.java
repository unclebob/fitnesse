// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.refactoring;

import fitnesse.FitNesseContext;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageProperty;
import fitnesse.wiki.WikiPageUtil;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class DeletePageResponder implements SecureResponder {
  private SimpleResponse response;
  private String qualifiedPageName;
  private WikiPagePath path;
  private FitNesseContext context;

  @Override
  public Response makeResponse(final FitNesseContext context, final Request request) throws Exception {
    this.context = context;
    intializeResponse(request);

    if (shouldNotDelete())
      response.redirect(context.contextRoot, WikiPageUtil.FRONT_PAGE);
    else
      tryToDeletePage(request);

    return response;
  }

  private void tryToDeletePage(Request request) throws UnsupportedEncodingException {
    String confirmedString = request.getInput("confirmed");
    if (!"yes".equalsIgnoreCase(confirmedString)) {
      response.setContent(buildConfirmationHtml(context.getRootPage(), qualifiedPageName, context, request));
    } else {
      WikiPage parentOfPageToBeDeleted = context.getRootPage().getPageCrawler().getPage(path);
      if (parentOfPageToBeDeleted != null) {
        parentOfPageToBeDeleted.remove();
      }
      path.removeNameFromEnd();
      redirect(path, response);
    }
  }

  private boolean shouldNotDelete() {
    return WikiPageUtil.FRONT_PAGE.equals(qualifiedPageName);
  }

  private void intializeResponse(Request request) {
    response = new SimpleResponse();
    qualifiedPageName = request.getResource();
    path = PathParser.parse(qualifiedPageName);
  }

  private void redirect(final WikiPagePath path, final SimpleResponse response) {
    String location = PathParser.render(path);
    if (location == null || location.isEmpty()) {
      response.redirect(context.contextRoot, "root");
    } else {
      response.redirect(context.contextRoot, location);
    }
  }

  private String buildConfirmationHtml(final WikiPage root, final String qualifiedPageName, final FitNesseContext context, Request request) {
    HtmlPage html = context.pageFactory.newPage();

    String tags = "";

    WikiPagePath path = PathParser.parse(qualifiedPageName);
    PageCrawler crawler = root.getPageCrawler();
    WikiPage wikiPage = crawler.getPage(path);
    if(wikiPage != null) {
      PageData pageData = wikiPage.getData();
      tags = pageData.getAttribute(WikiPageProperty.SUITES);
    }

    html.setTitle("Delete Confirmation");
    html.setPageTitle(new PageTitle("Confirm Deletion", PathParser.parse(qualifiedPageName), tags));

    makeMainContent(html, root, qualifiedPageName);
    html.setMainTemplate("deletePage");
    return html.html(request);
  }

  private void makeMainContent(final HtmlPage html, final WikiPage root, final String qualifiedPageName) {
    WikiPagePath path = PathParser.parse(qualifiedPageName);
    WikiPage pageToDelete = root.getPageCrawler().getPage(path);
    List<WikiPage> children = pageToDelete.getChildren();

    html.put("deleteSubPages", children != null && !children.isEmpty());
    html.put("pageName", qualifiedPageName);
  }

  @Override
  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
