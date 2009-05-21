package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.*;

public class AddChildPageResponder implements Responder {

  protected WikiPage page;
  protected PageCrawler crawler;

  public AddChildPageResponder() {
  }

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    crawler = context.root.getPageCrawler();
    page = crawler.getPage(context.root, PathParser.parse(request.getResource()));
    if (page == null)
      return notFoundResponse(context, request);
    if (request.getInput("name") == "")
      return errorResponse(context, request);
    createChildPage(request);
    SimpleResponse response = new SimpleResponse();
    WikiPagePath path = crawler.getFullPath(page);
    response.redirect(path.toString());
    return response;
  }

  private void createChildPage(Request request) throws Exception {
    WikiPage childPage = crawler.addPage(page, PathParser.parse((String) request.getInput("name")), (String) request.getInput("content"));
    PageData childPageData = childPage.getData();
    String pagetype = (String) request.getInput("pagetype");
    if (pagetype.equals("Normal")){
      childPageData.getProperties().remove("Test");
      childPageData.getProperties().remove("Suite");
    }
    childPageData.setAttribute(pagetype);
    childPage.commit(childPageData);
  }

  private Response errorResponse(FitNesseContext context, Request request) throws Exception {
    return new ErrorResponder("Invalid Child Name").makeResponse(context, request);
  }

  private Response notFoundResponse(FitNesseContext context, Request request) throws Exception {
    return new NotFoundResponder().makeResponse(context, request);
  }
}

