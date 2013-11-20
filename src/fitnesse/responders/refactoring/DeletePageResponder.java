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

import java.util.List;

public class DeletePageResponder implements SecureResponder {
  private SimpleResponse response;
  private String qualifiedPageName;
  private WikiPagePath path;
  private FitNesseContext context;

  public Response makeResponse(final FitNesseContext context, final Request request) {
    this.context = context;
    intializeResponse(request);

    if (shouldNotDelete())
      response.redirect("FrontPage");
    else
      tryToDeletePage(request);

    return response;
  }

  private void tryToDeletePage(Request request) {
    String confirmedString = (String) request.getInput("confirmed");
    if (!"yes".equalsIgnoreCase(confirmedString)) {
      response.setContent(buildConfirmationHtml(context.root, qualifiedPageName, context));
    } else {
      String nameOfPageToBeDeleted = path.last();
      path.removeNameFromEnd();
      WikiPage parentOfPageToBeDeleted = context.root.getPageCrawler().getPage(path);
      if (parentOfPageToBeDeleted != null) {
        parentOfPageToBeDeleted.removeChildPage(nameOfPageToBeDeleted);
      }
      redirect(path, response);
    }
  }

  private boolean shouldNotDelete() {
    return "FrontPage".equals(qualifiedPageName);
  }

  private void intializeResponse(Request request) {
    response = new SimpleResponse();
    qualifiedPageName = request.getResource();
    path = PathParser.parse(qualifiedPageName);
  }

  private void redirect(final WikiPagePath path, final SimpleResponse response) {
    String location = PathParser.render(path);
    if (location == null || location.length() == 0) {
      response.redirect("root");
    } else {
      response.redirect(location);
    }
  }

  private String buildConfirmationHtml(final WikiPage root, final String qualifiedPageName, final FitNesseContext context) {
    HtmlPage html = context.pageFactory.newPage();
    
    String tags = "";
    if(context.root!=null){
      WikiPagePath path = PathParser.parse(qualifiedPageName);
      PageCrawler crawler = context.root.getPageCrawler();
      WikiPage wikiPage = crawler.getPage(path);
      if(wikiPage != null) {
        PageData pageData = wikiPage.getData();
        tags = pageData.getAttribute(PageData.PropertySUITES);
      }
    }
      
    html.setTitle("Delete Confirmation");
    html.setPageTitle(new PageTitle("Confirm Deletion", PathParser.parse(qualifiedPageName), tags));

    makeMainContent(html, root, qualifiedPageName);
    html.setMainTemplate("deletePage");
    return html.html();
  }

  private void makeMainContent(final HtmlPage html, final WikiPage root, final String qualifiedPageName) {
    WikiPagePath path = PathParser.parse(qualifiedPageName);
    WikiPage pageToDelete = root.getPageCrawler().getPage(path);
    List<WikiPage> children = pageToDelete.getChildren();

    html.put("deleteSubPages", children != null && !children.isEmpty());
    html.put("pageName", qualifiedPageName);
  }

  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
