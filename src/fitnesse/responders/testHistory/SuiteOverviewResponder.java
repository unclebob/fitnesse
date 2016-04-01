package fitnesse.responders.testHistory;

import java.io.UnsupportedEncodingException;
import java.util.List;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.run.SuiteResponder;
import fitnesse.testrunner.SuiteContentsFinder;
import fitnesse.testrunner.SuiteFilter;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class SuiteOverviewResponder implements Responder {

  private FitNesseContext context;

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws UnsupportedEncodingException {
    this.context = context;
    WikiPage root = context.getRootPage();
    WikiPage page = root.getPageCrawler().getPage(PathParser.parse(request.getResource()));

    SuiteFilter filter = SuiteResponder.createSuiteFilter(request, page.getPageCrawler().getFullPath().toString());
    SuiteContentsFinder suiteTestFinder = new SuiteContentsFinder(page, filter, root);

    List<WikiPage> pagelist = suiteTestFinder.getAllPagesToRunForThisSuite();

    SuiteOverviewTree treeview = new SuiteOverviewTree(pagelist);
    treeview.findLatestResults(context.getTestHistoryDirectory());
    treeview.countResults();

    WikiPagePath path = PathParser.parse(request.getResource());
    SimpleResponse response = makeResponse(treeview, path, request);
    return response;

  }

  private SimpleResponse makeResponse(SuiteOverviewTree treeview, WikiPagePath path, Request request) throws UnsupportedEncodingException {
    SimpleResponse response = new SimpleResponse();

    HtmlPage page = context.pageFactory.newPage();
    page.setTitle("Suite Overview");
    page.setPageTitle(new PageTitle("Suite Overview", path));
    page.put("treeRoot", treeview.getTreeRoot());
    page.put("viewLocation", request.getResource());
    page.setMainTemplate("suiteOverview");
    response.setContent(page.html());
    return response;
  }
}
