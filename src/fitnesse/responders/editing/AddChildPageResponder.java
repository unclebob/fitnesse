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
import fitnesse.wikitext.parser.WikiWordPath;

public class AddChildPageResponder implements SecureResponder {
  private WikiPage currentPage;
  private PageCrawler crawler;
  private String childName;
  private WikiPagePath currentPagePath;
  private WikiPagePath childPath;
  private String childContent;
  private String pageType;
  private String helpText;
  private String suites;
    
  public SecureOperation getSecureOperation() {
    return new SecureWriteOperation();
  }

  public Response makeResponse(FitNesseContext context, Request request) {
    parseRequest(context, request);
    if (currentPage == null)
      return notFoundResponse(context, request);
    else if (nameIsInvalid(childName))
      return errorResponse(context, request);

    return createChildPageAndMakeResponse(request);
  }

  private void parseRequest(FitNesseContext context, Request request) {
    childName = (String) request.getInput(EditResponder.PAGE_NAME);
    childName = childName == null ? "null" : childName;
    childPath = PathParser.parse(childName);
    currentPagePath = PathParser.parse(request.getResource());
    crawler = context.root.getPageCrawler();
    currentPage = crawler.getPage(context.root, currentPagePath);
    childContent = (String) request.getInput(EditResponder.CONTENT_INPUT_NAME);
    pageType = (String) request.getInput(EditResponder.PAGE_TYPE);
    helpText = (String) request.getInput(EditResponder.HELP_TEXT);
    suites = (String) request.getInput(EditResponder.SUITES);
    if (childContent == null)
      childContent = "!contents\n";
    if (pageType == null)
      pageType = "Default";
  }

  private Response createChildPageAndMakeResponse(Request request) {
    createChildPage(request);
    SimpleResponse response = new SimpleResponse();
    WikiPagePath fullPathOfCurrentPage = crawler.getFullPath(currentPage);
    response.redirect(fullPathOfCurrentPage.toString());
    return response;
  }

  private boolean nameIsInvalid(String name) {
    if (name.equals(""))
      return true;
    if (!WikiWordPath.isSingleWikiWord(name))
      return true;
    return false;
  }

  private void createChildPage(Request request) {
    WikiPage childPage = crawler.addPage(currentPage, childPath, childContent);
    setAttributes(childPage);
    
  }

  private void setAttributes(WikiPage childPage) {
    PageData childPageData = childPage.getData();
    if (pageType.equals("Static")) {
      childPageData.getProperties().remove("Test");
      childPageData.getProperties().remove("Suite");
    } else if ("Test".equals(pageType) || "Suite".equals(pageType))
      childPageData.setAttribute(pageType);
    childPageData.setAttribute(PageData.PropertyHELP, helpText);
    childPageData.setAttribute(PageData.PropertySUITES, suites);
    childPage.commit(childPageData);
  }

  private Response errorResponse(FitNesseContext context, Request request) {
    return new ErrorResponder("Invalid Child Name").makeResponse(context, request);
  }

  private Response notFoundResponse(FitNesseContext context, Request request) {
    return new NotFoundResponder().makeResponse(context, request);
  }
}

