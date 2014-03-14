package fitnesse.responders.refactoring;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.wiki.refactoring.ReferenceRenamer;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ErrorResponder;
import fitnesse.responders.NotFoundResponder;
import fitnesse.wiki.*;

import java.util.List;

public abstract class PageMovementResponder implements SecureResponder {

  protected String oldNameOfPageToBeMoved;
  protected WikiPage oldRefactoredPage;
  protected WikiPage newParentPage;
  protected WikiPagePath newParentPath;

  protected abstract boolean getAndValidateNewParentPage(FitNesseContext context, Request request);

  protected abstract boolean getAndValidateRefactoringParameters(Request request);

  protected abstract ReferenceRenamer getReferenceRenamer(FitNesseContext context);

  protected abstract String getNewPageName();

  protected abstract String getErrorMessageHeader();

  protected abstract void execute() throws RefactorException;

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    if (!getAndValidateRefactoredPage(context, request)) {
      return new NotFoundResponder().makeResponse(context, request);
    }

    if (!getAndValidateNewParentPage(context, request)) {
      return makeErrorMessageResponder(newParentPath == null ? "null" : newParentPath.toString() + " does not exist.").makeResponse(context, request);
    }

    if (!getAndValidateRefactoringParameters(request)) {
      return makeErrorMessageResponder("").makeResponse(context, request);
    }

    if (targetPageExists()) {
      return makeErrorMessageResponder(makeLink(getNewPageName()) + " already exists").makeResponse(context, request);
    }

    if (request.hasInput("refactorReferences")) {
      getReferenceRenamer(context).renameReferences();
    }
    execute();

    SimpleResponse response = new SimpleResponse();
    response.redirect(context.contextRoot, createRedirectionUrl(newParentPage, getNewPageName()));

    return response;
  }

  protected boolean getAndValidateRefactoredPage(FitNesseContext context, Request request) {

    oldNameOfPageToBeMoved = request.getResource();

    WikiPagePath path = PathParser.parse(oldNameOfPageToBeMoved);
    oldRefactoredPage = context.root.getPageCrawler().getPage(path);
    return (oldRefactoredPage != null);
  }

  private Responder makeErrorMessageResponder(String message) {
    return new ErrorResponder(getErrorMessageHeader() + "<br/>" + message);
  }

  private boolean targetPageExists() {
    return newParentPage.hasChildPage(getNewPageName());
  }

  protected String makeLink(String page) {
    return HtmlUtil.makeLink(page, page).html();
  }

  protected String createRedirectionUrl(WikiPage newParent, String newName) {
    if(newParent.isRoot()) {
      return newName;
    }
    PageCrawler crawler = newParent.getPageCrawler();
    return PathParser.render(crawler.getFullPath().addNameToEnd(newName));
  }

  protected void movePage(WikiPage movedPage, WikiPage newParentPage, String pageName) throws RefactorException {

  	if (isSymlinkedPage(movedPage)) {
  	  if (isSymlinkedPage(movedPage.getParent())) {
  	    throw new RefactorException("Can not move symlink page when parent page is also a symlink");
  	  }
  		WikiPage referencedPage = ((SymbolicPage) movedPage).getRealPage();
  		removeSymlink(movedPage);
  		createSymlink(referencedPage, newParentPage, pageName);
  	} else {
  		PageData pageData = movedPage.getData();
  		WikiPage targetPage = newParentPage.addChildPage(pageName);

	    targetPage.commit(pageData);

	    moveChildren(movedPage, targetPage);

	    WikiPage parentOfMovedPage = movedPage.getParent();
	    parentOfMovedPage.removeChildPage(movedPage.getName());
  	}
  }

  protected void moveChildren(WikiPage movedPage, WikiPage newParentPage) throws RefactorException {
    List<WikiPage> children = movedPage.getChildren();
    for (WikiPage page : children) {
      movePage(page, newParentPage, page.getName());
    }
  }

  private boolean isSymlinkedPage(WikiPage page) {
	  return page instanceof SymbolicPage;
  }

  private void removeSymlink(WikiPage movedPage) {
    WikiPage parent = movedPage.getParent();
    PageData data = parent.getData();
    WikiPageProperty symLinks = data.getProperties().getProperty(SymbolicPage.PROPERTY_NAME);
    symLinks.remove(movedPage.getName());
    parent.commit(data);
  }

  private void createSymlink(WikiPage referencedPage, WikiPage newParentPage,
    String pageName) {
    PageData newParentData = newParentPage.getData();
    WikiPageProperty symLinks = newParentData.getProperties().getProperty(SymbolicPage.PROPERTY_NAME);
    WikiPagePath fullPath;
    if (isChildOf(referencedPage, oldRefactoredPage)) {
      // Watch out: the referenced page will also be moved
      WikiPagePath relativePath = PathParser.parse(oldRefactoredPage.getParent().getPageCrawler().getRelativeName(referencedPage));
      fullPath = this.newParentPage.getPageCrawler().getFullPathOfChild(relativePath);
    } else {
      fullPath = referencedPage.getPageCrawler().getFullPath();
    }
    fullPath.makeAbsolute();
    symLinks.set(pageName, PathParser.render(fullPath));
    newParentPage.commit(newParentData);
  }



  private boolean isChildOf(WikiPage childPage, WikiPage parentPage) {
	  String childPath = PathParser.render(childPage.getPageCrawler().getFullPath());
	  String parentPath = PathParser.render(parentPage.getPageCrawler().getFullPath());
	  return childPath.startsWith(parentPath);
  }

  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }

}