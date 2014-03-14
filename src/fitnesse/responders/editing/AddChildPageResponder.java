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
  private String childName;
  private WikiPagePath childPath;
  private String childContent;
  private String pageType;
  private String helpText;
  private String suites;
  private WikiPage pageTemplate;

  public SecureOperation getSecureOperation() {
    return new SecureWriteOperation();
  }

  public Response makeResponse(FitNesseContext context, Request request) {
    parseRequest(context, request);
    if (currentPage == null)
      return notFoundResponse(context, request);
    else if (nameIsInvalid(childName))
      return errorResponse(context, request);

    return createChildPageAndMakeResponse(context);
  }

  private void parseRequest(FitNesseContext context, Request request) {
    childName = (String) request.getInput(EditResponder.PAGE_NAME);
    childName = childName == null ? "null" : childName;
    childPath = PathParser.parse(childName);
    WikiPagePath currentPagePath = PathParser.parse(request.getResource());
    PageCrawler pageCrawler = context.root.getPageCrawler();
    currentPage = pageCrawler.getPage(currentPagePath);
    if (request.hasInput(NewPageResponder.PAGE_TEMPLATE)) {
      pageTemplate = pageCrawler.getPage(PathParser.parse((String) request.getInput(NewPageResponder.PAGE_TEMPLATE)));
    } else {
      pageType = (String) request.getInput(EditResponder.PAGE_TYPE);
    }
    childContent = (String) request.getInput(EditResponder.CONTENT_INPUT_NAME);
    helpText = (String) request.getInput(EditResponder.HELP_TEXT);
    suites = (String) request.getInput(EditResponder.SUITES);
    if (childContent == null)
      childContent = "!contents\n";
    if (pageTemplate == null && pageType == null)
      pageType = "Default";
  }

  private Response createChildPageAndMakeResponse(FitNesseContext context) {
    createChildPage();
    SimpleResponse response = new SimpleResponse();
    WikiPagePath fullPathOfCurrentPage = currentPage.getPageCrawler().getFullPath();
    response.redirect(context.contextRoot, fullPathOfCurrentPage.toString());
    return response;
  }

  private boolean nameIsInvalid(String name) {
    if (name.equals(""))
      return true;
    return !WikiWordPath.isSingleWikiWord(name);
  }

  private void createChildPage() {
    WikiPage childPage = WikiPageUtil.addPage(currentPage, childPath, childContent);
    setAttributes(childPage);
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

