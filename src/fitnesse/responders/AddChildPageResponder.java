package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.*;
import fitnesse.wikitext.widgets.WikiWordWidget;

public class AddChildPageResponder implements Responder {

  protected WikiPage page;
  protected PageCrawler crawler;
  private String message;

  public AddChildPageResponder() {
  }

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    crawler = context.root.getPageCrawler();
    page = crawler.getPage(context.root, PathParser.parse(request.getResource()));
    if (page == null)
      return notFoundResponse(context, request);
    if (nameIsInvalid((String) request.getInput("name")))
      return errorResponse(context, request);
    createChildPage(request);
    SimpleResponse response = new SimpleResponse();
    WikiPagePath path = crawler.getFullPath(page);
    response.redirect(path.toString());
    return response;
  }

  private boolean nameIsInvalid(String name) {
    if(name.equals(""))
      return true;
    if(!WikiWordWidget.isSingleWikiWord(name))
      return true;
    return false;
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

  public Response makeErrorResponse(FitNesseContext context, Request request) throws Exception {
    SimpleResponse response = new SimpleResponse(400);
    HtmlPage html = context.htmlPageFactory.newPage();
    HtmlUtil.addTitles(html, "Error Occured");
    if (message != null)
      html.main.add(makeErrorMessage());
    response.setContent(html.html());

    return response;
  }

  public HtmlTag makeErrorMessage() {
    HtmlTag tag = HtmlUtil.makeDivTag("centered");
    tag.add(message);
    return tag;
  }
}

