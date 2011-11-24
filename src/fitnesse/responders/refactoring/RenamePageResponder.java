// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.refactoring;

import fitnesse.FitNesseContext;
import fitnesse.components.PageReferenceRenamer;
import fitnesse.components.ReferenceRenamer;
import fitnesse.http.Request;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.WikiWordPath;

public class RenamePageResponder extends PageMovementResponder {
  private String newName;

  @Override
  protected boolean getAndValidateNewParentPage(FitNesseContext context, Request request) throws Exception {
    newParentPath = PathParser.parse(oldNameOfPageToBeMoved).parentPath();
    newParentPage = oldRefactoredPage.getParent();
    return (newParentPage != null);
  }

  @Override
  protected boolean getAndValidateRefactoringParameters(Request request) throws Exception {
    newName = (String) request.getInput("newName");
    return (newName != null && WikiWordPath.isSingleWikiWord(newName) && !"FrontPage".equals(oldNameOfPageToBeMoved));
  }

  @Override
  protected ReferenceRenamer getReferenceRenamer(FitNesseContext context)
  throws Exception {
    return new PageReferenceRenamer(context.root, oldRefactoredPage, getNewPageName());
  }

  @Override
  protected void execute() throws Exception {
    WikiPage parentOfPageToRename = oldRefactoredPage.getParent();

    WikiPage renamedPage = parentOfPageToRename.addChildPage(newName);

    movePage(oldRefactoredPage, renamedPage);
  }

  @Override
  protected String getNewPageName() throws Exception {
    return newName;
  }

  @Override
  protected String getErrorMessageHeader() throws Exception {
    return "Cannot rename " + makeLink(oldNameOfPageToBeMoved) + " to " + newName;
  }
}
