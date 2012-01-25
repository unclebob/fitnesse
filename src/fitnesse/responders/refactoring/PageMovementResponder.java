package fitnesse.responders.refactoring;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.components.ReferenceRenamer;
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

  protected abstract void execute();

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
    response.redirect(createRedirectionUrl(newParentPage, getNewPageName()));

    return response;
  }

  protected boolean getAndValidateRefactoredPage(FitNesseContext context, Request request) {
    PageCrawler crawler = context.root.getPageCrawler();

    oldNameOfPageToBeMoved = request.getResource();

    WikiPagePath path = PathParser.parse(oldNameOfPageToBeMoved);
    oldRefactoredPage = crawler.getPage(context.root, path);
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
    PageCrawler crawler = newParent.getPageCrawler();
    if(crawler.isRoot(newParent)) {
      return newName;
    }
    return PathParser.render(crawler.getFullPath(newParent).addNameToEnd(newName));
  }

  protected void movePage(WikiPage movedPage, WikiPage targetPage) {
    PageData pageData = movedPage.getData();

    targetPage.commit(pageData);

    moveChildren(movedPage, targetPage);

    WikiPage parentOfMovedPage = movedPage.getParent();
    parentOfMovedPage.removeChildPage(movedPage.getName());
  }

  protected void moveChildren(WikiPage movedPage, WikiPage newParentPage) {
    List<WikiPage> children = movedPage.getChildren();
    for (WikiPage page : children) {
      movePage(page, newParentPage.addChildPage(page.getName()));
    }
  }

  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }

}