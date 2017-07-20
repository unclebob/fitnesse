package fitnesse.responders.editing;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.authentication.SecureWriteOperation;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ErrorResponder;
import fitnesse.responders.NotFoundResponder;
import fitnesse.wiki.*;

public class AddChildPageResponder implements SecureResponder {
  private WikiPage currentPage;
  private String childName;
  private WikiPagePath childPath;
  private String childContent;
  private String pageType;
  private String helpText;
  private String suites;
  private WikiPage pageTemplate;
  private String user;

  @Override
  public SecureOperation getSecureOperation() {
    return new SecureWriteOperation();
  }

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    parseRequest(context, request);
    if (currentPage == null)
      return notFoundResponse(context, request);
    else if (nameIsInvalid(childName))
      return errorResponse(context, request);
    else if (pageAlreadyExists(childName))
      return alreadyExistsResponse(context, request);

    return createChildPageAndMakeResponse(context);
  }

  private void parseRequest(FitNesseContext context, Request request) {
	  user = request.getAuthorizationUsername();
    childName = request.getInput(EditResponder.PAGE_NAME);
    childName = childName == null ? "null" : childName;
    childPath = PathParser.parse(childName);
    WikiPagePath currentPagePath = PathParser.parse(request.getResource());
    PageCrawler pageCrawler = context.getRootPage().getPageCrawler();
    currentPage = pageCrawler.getPage(currentPagePath);
    if (request.hasInput(NewPageResponder.PAGE_TEMPLATE)) {
      pageTemplate = pageCrawler.getPage(PathParser.parse(request.getInput(NewPageResponder.PAGE_TEMPLATE)));
    } else {
      pageType = request.getInput(EditResponder.PAGE_TYPE);
    }
    childContent = request.getInput(EditResponder.CONTENT_INPUT_NAME);
    helpText = request.getInput(EditResponder.HELP_TEXT);
    suites = request.getInput(EditResponder.SUITES);
    if (childContent == null)
      childContent = "!contents\n";
    if (pageTemplate == null && pageType == null)
      pageType = "Default";
  }

  private Response createChildPageAndMakeResponse(FitNesseContext context) {
    createChildPage(context);
    SimpleResponse response = new SimpleResponse();
    WikiPagePath fullPathOfCurrentPage = currentPage.getPageCrawler().getFullPath();
    response.redirect(context.contextRoot, fullPathOfCurrentPage.toString());
    return response;
  }

  private boolean nameIsInvalid(String name) {
    if (name.equals(""))
      return true;
    return !PathParser.isSingleWikiWord(name);
  }

  private boolean pageAlreadyExists(String childName) {
    return currentPage.getPageCrawler().pageExists(PathParser.parse(childName));
  }

  private void createChildPage(FitNesseContext context) {
    WikiPage childPage = WikiPageUtil.addPage(currentPage, childPath, childContent);
    setAttributes(childPage);
    context.recentChanges.updateRecentChanges(childPage);

  }

  private void setAttributes(WikiPage childPage) {
    PageData childPageData = childPage.getData();
    if (pageTemplate != null) {
      childPageData.setProperties(pageTemplate.getData().getProperties());
    } else if (pageType.equals("Static")) {
      childPageData.getProperties().remove("Test");
      childPageData.getProperties().remove("Suite");
    } else if ("Test".equals(pageType) || "Suite".equals(pageType)) {
      childPageData.getProperties().remove("Test");
      childPageData.getProperties().remove("Suite");
      childPageData.setAttribute(pageType);
    }
    childPageData.setOrRemoveAttribute(PageData.PropertyHELP, helpText);
    childPageData.setOrRemoveAttribute(PageData.PropertySUITES, suites);
    childPageData.setOrRemoveAttribute(PageData.LAST_MODIFYING_USER, user);
    childPage.commit(childPageData);
  }

  private Response errorResponse(FitNesseContext context, Request request) throws Exception {
    return new ErrorResponder("Invalid Child Name").makeResponse(context, request);
  }

  private Response alreadyExistsResponse(FitNesseContext context, Request request) throws Exception {
    return new ErrorResponder("Child page already exists", 409).makeResponse(context, request);
  }

  private Response notFoundResponse(FitNesseContext context, Request request) throws Exception {
    return new NotFoundResponder().makeResponse(context, request);
  }
}

