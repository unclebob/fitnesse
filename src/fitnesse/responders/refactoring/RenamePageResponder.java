// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.refactoring;

import java.util.Iterator;
import java.util.List;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.components.PageReferenceRenamer;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ErrorResponder;
import fitnesse.responders.NotFoundResponder;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wikitext.widgets.WikiWordWidget;

public class RenamePageResponder implements SecureResponder {
  private String qualifiedName;
  private String newName;
  private boolean refactorReferences;
  private WikiPagePath pathToRename;
  private WikiPage pageToRename;
  private WikiPage root;
  private WikiPage parentOfPageToRename;

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    root = context.root;
    qualifiedName = request.getResource();
    newName = (String) request.getInput("newName");
    refactorReferences = request.hasInput("refactorReferences");

    Response response;

    if (newName != null && !qualifiedName.equals("FrontPage") && WikiWordWidget.isSingleWikiWord(newName)) {
      PageCrawler pageCrawler = context.root.getPageCrawler();

      pathToRename = PathParser.parse(qualifiedName);
      pageToRename = pageCrawler.getPage(context.root, pathToRename);
      if (pageToRename == null)
        response = new NotFoundResponder().makeResponse(context, request);
      else {
        WikiPagePath parentPath = pathToRename.parentPath();
        parentOfPageToRename = pageCrawler.getPage(context.root, parentPath);
        final boolean pageExists = pageCrawler.pageExists(parentOfPageToRename, PathParser.parse(newName));
        if (!pageExists) {
          qualifiedName = renamePageAndMaybeAllReferences();
          response = new SimpleResponse();
          response.redirect(qualifiedName);
        } else // already exists
        {
          response = makeErrorMessageResponder(makeLink(newName) + " already exists").makeResponse(context, request);
        }
      }
    } else {
      response = makeErrorMessageResponder(newName + " is not a valid simple page name.").makeResponse(context, request);
    }

    return response;
  }

  private Responder makeErrorMessageResponder(String message) throws Exception {
    return new ErrorResponder("Cannot rename " + makeLink(qualifiedName) + " to " + newName + "<br/>" + message);
  }

  private String makeLink(String page) throws Exception {
    return HtmlUtil.makeLink(page, page).html();
  }

  private String renamePageAndMaybeAllReferences() throws Exception {
    if (refactorReferences)
      renameReferences();
    renamePage();

    pathToRename.removeNameFromEnd();
    pathToRename.addNameToEnd(newName);
    return PathParser.render(pathToRename);
  }

  private void renameReferences() throws Exception {
    PageReferenceRenamer renamer = new PageReferenceRenamer(root);
    renamer.renameReferences(pageToRename, newName);
  }

  private boolean renamePage() throws Exception {
    String oldName = pageToRename.getName();
    if (parentOfPageToRename.hasChildPage(oldName) && !parentOfPageToRename.hasChildPage(newName)) {
      WikiPage originalPage = parentOfPageToRename.getChildPage(oldName);
      PageCrawler crawler = originalPage.getPageCrawler();
      PageData data = originalPage.getData();

      WikiPage renamedPage = parentOfPageToRename.addChildPage(newName);
      renamedPage.commit(data);

      List<?> children = originalPage.getChildren();
      for (Iterator<?> iterator = children.iterator(); iterator.hasNext();) {
        WikiPage child = (WikiPage) iterator.next();
        MovePageResponder.movePage(root, crawler.getFullPath(child), crawler.getFullPath(renamedPage));
      }

      parentOfPageToRename.removeChildPage(oldName);
      return true;
    }
    return false;
  }

  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
