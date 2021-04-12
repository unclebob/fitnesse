// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.refactoring;

import fitnesse.FitNesseContext;
import fitnesse.http.Request;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.refactoring.ChangeReference;
import fitnesse.wiki.refactoring.MovedPageReferenceRenamer;

public class MovePageResponder extends PageMovementResponder {
  private String newParentName;

  @Override
  protected boolean getAndValidateNewParentPage(FitNesseContext context, Request request) {
    PageCrawler crawler = context.getRootPage().getPageCrawler();

    newParentName = getNameofNewParent(request);
    if (newParentName == null)
      return false;

    newParentPath = PathParser.parse(newParentName);
    newParentPage = crawler.getPage(newParentPath);

    return (newParentPage != null);
  }

  private static String getNameofNewParent(Request request) {
    String newParentName = request.getInput("newLocation");
    if (".".equals(newParentName)) {
      return "";
    }
    return newParentName;
  }

  @Override
  protected boolean getAndValidateRefactoringParameters(Request request) {
    WikiPagePath pageToBeMovedPath = oldRefactoredPage.getFullPath();
    WikiPagePath newParentPath = newParentPage.getFullPath();

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
  protected ChangeReference getReferenceRenamer() {
    return new MovedPageReferenceRenamer(oldRefactoredPage, newParentName);
  }

  @Override
  protected void execute() throws RefactorException {
    movePage(oldRefactoredPage, newParentPage, getNewPageName());
  }

  @Override
  protected String getNewPageName() {
    return oldRefactoredPage.getName();
  }

  @Override
  protected String getErrorMessageHeader() {
    return "Cannot move " + makeLink(oldNameOfPageToBeMoved) + " below " + newParentName + ".";
  }

}
