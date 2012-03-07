// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.refactoring;

import java.util.List;

import fitnesse.FitNesseContext;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.RawHtml;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.templateUtilities.HtmlPage;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class DeletePageResponder implements SecureResponder {
  
  public Response makeResponse(final FitNesseContext context, final Request request) {
    SimpleResponse response = new SimpleResponse();
    String qualifiedPageName = request.getResource();
    WikiPagePath path = PathParser.parse(qualifiedPageName);

    if ("FrontPage".equals(qualifiedPageName)) {
      response.redirect("FrontPage");
      return response;
    }

    String confirmedString = (String) request.getInput("confirmed");
    if (!"yes".equalsIgnoreCase(confirmedString)) {
      response.setContent(buildConfirmationHtml(context.root, qualifiedPageName, context));
      return response;
    }

    String nameOfPageToBeDeleted = path.last();
    path.removeNameFromEnd();
    WikiPage parentOfPageToBeDeleted = context.root.getPageCrawler().getPage(context.root, path);
    if (parentOfPageToBeDeleted != null) {
      parentOfPageToBeDeleted.removeChildPage(nameOfPageToBeDeleted);
    }
    redirect(path, response);

    return response;
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
    html.setTitle("Delete Confirmation");
    html.setPageTitle(new PageTitle("Confirm Deletion", qualifiedPageName, "/"));
    makeMainContent(html, root, qualifiedPageName);
    html.setMainTemplate("deletePage");
    return html.html();
  }

  private void makeMainContent(final HtmlPage html, final WikiPage root, final String qualifiedPageName) {
    WikiPagePath path = PathParser.parse(qualifiedPageName);
    WikiPage pageToDelete = root.getPageCrawler().getPage(root, path);
    List<WikiPage> children = pageToDelete.getChildren();

    html.put("deleteSubPages", children != null && !children.isEmpty());
    html.put("pageName", qualifiedPageName);
  }

  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
