package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.*;
import fitnesse.wikitext.MarkUpSystem;

import java.io.UnsupportedEncodingException;
import java.util.*;

public class ScopeVariablesResponder extends BasicResponder {
  private HashMap<String,String> variables;
  private HtmlPage responsePage;

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    responsePage = context.pageFactory.newPage();

    WikiPage requestedPage = getRequestedPage(request, context);
    String pageName = request.getResource();
    WikiPagePath path = PathParser.parse(pageName);
    PageCrawler crawler = context.getRootPage().getPageCrawler();
    WikiPage page = crawler.getPage(path);

    Response response;
    if (requestedPage == null)
      response = pageNotFoundResponse(context, request);
    else {
      variables = new HashMap<>();
      listVariablesLoc(page);
      response = makeResponse(request);
    }
    return response;
  }

  private void listVariablesLoc(WikiPage page) {
    List<String> variableList = MarkUpSystem.listVariables(page);

    for (String var : variableList) {
      if (variables.get(var) == null) {
        variables.put(var, page.getFullPath().toString());
      }
    }

    if (page.getParent() != page) listVariablesLoc(page.getParent());
  }

  private Response makeResponse(Request request) throws UnsupportedEncodingException {
    responsePage.setTitle("Available variables");
    responsePage.put("viewLocation", request.getResource());
    responsePage.setNavTemplate("viewNav");
    responsePage.setPageTitle(new PageTitle(PathParser.parse(request.getResource())));
    responsePage.put("variables",variables);
    responsePage.setMainTemplate("scopeVariables");

    SimpleResponse response = new SimpleResponse();
    response.setContent(responsePage.html(request));
    return response;
  }
}
