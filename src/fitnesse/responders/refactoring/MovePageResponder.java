// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.refactoring;


import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureResponder;
import fitnesse.components.MovedPageReferenceRenamer;
import fitnesse.components.ReferenceRenamer;
import fitnesse.http.Request;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPagePath;

public class MovePageResponder extends PageMovementResponder implements SecureResponder {

  private String newParentName;

  @Override
  protected boolean getAndValidateNewParentPage(FitNesseContext context, Request request) {
    PageCrawler crawler = context.root.getPageCrawler();

    newParentName = getNameofNewParent(request);
    if (newParentName == null)
      return false;

    newParentPath = PathParser.parse(newParentName);
    newParentPage = crawler.getPage(context.root, newParentPath);

    return (newParentPage != null);
  }

  private static String getNameofNewParent(Request request) {
    String newParentName = (String) request.getInput("newLocation");
    if (".".equals(newParentName)) {
      return "";
    }
    return newParentName;
  }

  @Override
  protected boolean getAndValidateRefactoringParameters(Request request) {
    PageCrawler crawler = oldRefactoredPage.getPageCrawler();

    WikiPagePath pageToBeMovedPath = crawler.getFullPath(oldRefactoredPage);
    WikiPagePath newParentPath = crawler.getFullPath(newParentPage);

    return !pageToBeMovedPath.equals(newParentPath) &&
    !selfPage(pageToBeMovedPath, newParentPath) &&
    !pageIsAncestorOfNewParent(pageToBeMovedPath, newParentPath);
  }

  private boolean selfPage(WikiPagePath pageToBeMovedPath, WikiPagePath newParentPath) {
    WikiPagePath originalParentPath = pageToBeMovedPath.parentPath();
    return originalParentPath.equals(newParentPath);
  }

  boolean pageIsAncestorOfNewParent(WikiPagePath pageToBeMovedPath, WikiPagePath newParentPath) {
    return newParentPath.startsWith(pageToBeMovedPath);
  }

  @Override
  protected ReferenceRenamer getReferenceRenamer(FitNesseContext context) {
    return new MovedPageReferenceRenamer(context.root, oldRefactoredPage, newParentName);
  }

  @Override
  protected void execute() {
    movePage(oldRefactoredPage, newParentPage, getNewPageName());
  }

  @Override
  protected String getNewPageName() {
    return oldRefactoredPage.getName();
  }

  @Override
  protected String getErrorMessageHeader() {
    return "Cannot move " + makeLink(oldNameOfPageToBeMoved) + " below " + newParentName;
  }

}
