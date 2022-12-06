// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.refactoring;

import fitnesse.FitNesseContext;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.refactoring.ChangeReference;
import fitnesse.wiki.refactoring.PageReferenceRenamer;
import fitnesse.http.Request;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class RenamePageResponder extends PageMovementResponder {
  private String newName;

  @Override
  protected boolean getAndValidateNewParentPage(FitNesseContext context, Request request) {
    newParentPath = PathParser.parse(oldNameOfPageToBeMoved).parentPath();
    newParentPage = oldRefactoredPage.getParent();
    return (newParentPage != null);
  }

  @Override
  protected boolean getAndValidateRefactoringParameters(Request request) {
    newName = request.getInput("newName");
    return (newName != null && PathParser.isSingleWikiWord(newName) && !WikiPageUtil.FRONT_PAGE.equals(oldNameOfPageToBeMoved));
  }

  @Override
  protected ChangeReference getReferenceRenamer() {
    return new PageReferenceRenamer(oldRefactoredPage, getNewPageName());
  }

  @Override
  protected void execute() throws RefactorException {
    WikiPage parentOfPageToRename = oldRefactoredPage.getParent();

    movePage(oldRefactoredPage, parentOfPageToRename, newName);
  }

  @Override
  protected String getNewPageName() {
    return newName;
  }

  @Override
  protected String getErrorMessageHeader() {
    return "Cannot rename " + makeLink(oldNameOfPageToBeMoved) + " to " + newName + ".";
  }
}
