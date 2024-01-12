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
  private List<ScopeVariable> variables;
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
      variables = new ArrayList<>();
      listVariablesLoc(page);
      response = makeResponse(request);
    }
    return response;
  }

  private void listVariablesLoc(WikiPage page) {
    Map<String, String> variableList = MarkUpSystem.listVariables(page);

    for (Map.Entry<String, String> var : variableList.entrySet()) {
      if (variables.stream().noneMatch(variable -> var.getKey().equals(variable.getKey()))) {
        variables.add(new ScopeVariable(var.getKey(), page.getFullPath().toString(), var.getValue()));
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

  public class ScopeVariable {
    private String key;
    private String location;
    private String value;

    public ScopeVariable(String key, String location, String value) {
      this.key = key;
      this.location = location;
      this.value = value;
    }
    
    public String getKey() {
      return this.key;
    }

    public String getLocation() {
      return this.location;
    }

    public String getValue() {
      return this.value;
    }
  }
}
