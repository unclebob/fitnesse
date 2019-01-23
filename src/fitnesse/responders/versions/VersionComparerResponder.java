package fitnesse.responders.versions;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ErrorResponder;
import fitnesse.responders.NotFoundResponder;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class VersionComparerResponder implements Responder {

  private VersionComparer comparer;
  private String firstVersion;
  private String secondVersion;
  private FitNesseContext context;
  public boolean testing;

  public VersionComparerResponder(VersionComparer comparer) {
    this.comparer = comparer;
  }

  public VersionComparerResponder() {
    this(new VersionComparer());
  }

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    String resource = request.getResource();
    PageCrawler pageCrawler = context.getRootPage().getPageCrawler();
    WikiPagePath path = PathParser.parse(resource);
    WikiPage page = pageCrawler.getPage(path);
    if (page == null)
      return new NotFoundResponder().makeResponse(context, request);

    this.context = context;
    getVersionsFromRequest(request);
    if (firstVersion.equals("") && secondVersion.equals("")) {
      String message = "Compare failed because no input files were given. Select one or two please.";
      return makeErrorResponse(context, request, message);
    }
    WikiPage firstVersionPage = page.getVersion(firstVersion);
    WikiPage secondVersionPage;
    if (secondVersion.equals(""))
      secondVersionPage = page;
    else
      secondVersionPage = page.getVersion(secondVersion);
    comparer.compare(firstVersion, firstVersionPage.getData().getContent(), secondVersion.equals("") ? "latest" : secondVersion, secondVersionPage.getData().getContent());
    return makeValidResponse(request);
  }

  private boolean getVersionsFromRequest(Request request) {
    firstVersion = "";
    secondVersion = "";
    Map<String, String> inputs = request.getMap();
    Set<String> keys = inputs.keySet();
    return setFileNames(keys);
  }

  private boolean setFileNames(Set<String> keys) {
    List<String> sortedkeys = Arrays.asList(keys.toArray(new String[keys.size()]));
    Collections.sort(sortedkeys);
    for (String key : sortedkeys) {
      if (key.contains("Version_"))
        if (setFileNames(key))
          return false;
    }
    return !(firstVersion.equals("") || secondVersion.equals(""));
  }

  private boolean setFileNames(String key) {
    if (firstVersion.equals(""))
      firstVersion = key.substring(key.indexOf("_") + 1);
    else if (secondVersion.equals(""))
      secondVersion = key.substring(key.indexOf("_") + 1);
    else
      return true;
    return false;
  }

  private Response makeErrorResponse(FitNesseContext context, Request request,
      String message) throws Exception {
    return new ErrorResponder(message).makeResponse(context, request);
  }

  private Response makeValidResponse(Request request) throws IOException {
    HtmlPage page = context.pageFactory.newPage();
    page.setTitle("Version Comparison");
    page.setPageTitle(makePageTitle(request.getResource()));
    page.setNavTemplate("compareVersionsNav.vm");
    page.put("localPath", request.getResource());
    page.put("original", firstVersion);
    page.put("revised", secondVersion.equals("") ? "latest" : secondVersion);
    if (!testing) {
      page.put("differences", comparer.getDifferences());
      page.setMainTemplate("compareVersions");
    }
    SimpleResponse response = new SimpleResponse();
    response.setContent(page.html(request));
    return response;
  }

  private PageTitle makePageTitle(String resource) {

    String tags="";
    if(context.getRootPage() != null){
      WikiPagePath path = PathParser.parse(resource);
      PageCrawler crawler = context.getRootPage().getPageCrawler();
      WikiPage wikiPage = crawler.getPage(path);
      if(wikiPage != null) {
        PageData pageData = wikiPage.getData();
        tags = pageData.getAttribute(PageData.PropertySUITES);
      }
    }

    return new PageTitle("Version Compare", PathParser.parse(resource),tags);
  }

}
