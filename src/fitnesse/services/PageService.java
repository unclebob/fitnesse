package fitnesse.services;

import fitnesse.FitNesseContext;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.responders.NotFoundResponder;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class PageService {
  private FitNesseContext context;
  private Request request;

  public PageService(FitNesseContext context, Request request) {
    this.context = context;
    this.request = request;
  }

  public WikiPage getPage() {
    String resource = request.getResource();
    WikiPagePath path = PathParser.parse(resource);
    return context.getRootPage().getPageCrawler().getPage(path);
  }

  public Response pageIsNull(WikiPage page) throws Exception {
    if (page == null)
      return new NotFoundResponder().makeResponse(context, request);
    return null;
  }
}
