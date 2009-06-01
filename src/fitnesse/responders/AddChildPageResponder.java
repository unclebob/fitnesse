package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.*;
import fitnesse.wikitext.widgets.WikiWordWidget;

public class AddChildPageResponder implements Responder {
  private WikiPage currentPage;
  private PageCrawler crawler;
  private String childName;
  private WikiPagePath currentPagePath;
  private WikiPagePath childPath;
  private String childContent;
  private String pageType;

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    parseRequest(context, request);
    if (currentPage == null)
      return notFoundResponse(context, request);
    else if (nameIsInvalid(childName))
      return errorResponse(context, request);
    else {
      return createChildPageAndMakeResponse(request);
    }
  }

  private void parseRequest(FitNesseContext context, Request request) throws Exception {
    childName = (String) request.getInput("name");
    currentPagePath = PathParser.parse(request.getResource());
    crawler = context.root.getPageCrawler();
    currentPage = crawler.getPage(context.root, currentPagePath);
    childPath = PathParser.parse(childName);
    childContent = (String) request.getInput("content");
    pageType = (String) request.getInput("pageType");
    if (childContent == null)
      childContent = "!contents\n";
    if (pageType == null)
      pageType = "Default";
  }

  private Response createChildPageAndMakeResponse(Request request) throws Exception {
    createChildPage(request);
    SimpleResponse response = new SimpleResponse();
    WikiPagePath fullPathOfCurrentPage = crawler.getFullPath(currentPage);
    response.redirect(fullPathOfCurrentPage.toString());
    return response;
  }

  private boolean nameIsInvalid(String name) {
    if (name.equals(""))
      return true;
    if (!WikiWordWidget.isSingleWikiWord(name))
      return true;
    return false;
  }

  private void createChildPage(Request request) throws Exception {
    WikiPage childPage = crawler.addPage(currentPage, childPath, childContent);
    setTestAndSuiteAttributes(childPage);
  }

  private void setTestAndSuiteAttributes(WikiPage childPage) throws Exception {
    PageData childPageData = childPage.getData();
    if (pageType.equals("Normal")) {
      childPageData.getProperties().remove("Test");
      childPageData.getProperties().remove("Suite");
    } else if ("Test".equals(pageType) || "Suite".equals(pageType))
      childPageData.setAttribute(pageType);
    childPage.commit(childPageData);
  }

  private Response errorResponse(FitNesseContext context, Request request) throws Exception {
    return new ErrorResponder("Invalid Child Name").makeResponse(context, request);
  }

  private Response notFoundResponse(FitNesseContext context, Request request) throws Exception {
    return new NotFoundResponder().makeResponse(context, request);
  }
}

