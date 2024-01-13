package fitnesse.responders.testHistory;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.reporting.history.PageHistory;
import fitnesse.responders.ErrorResponder;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class HistoryComparerResponder implements Responder {
  public HistoryComparer comparer;
  private SimpleDateFormat dateFormat = PageHistory.getDateFormat();
  private String firstFileName = "";
  private String secondFileName = "";
  private String firstFilePath;
  private String secondFilePath;
  public boolean testing = false;

  private FitNesseContext context;

  public HistoryComparerResponder(HistoryComparer historyComparer) {
    comparer = historyComparer;
  }

  public HistoryComparerResponder() {
    comparer = new HistoryComparer();
  }

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    this.context = context;
    initializeReponseComponents();
    if (!getFileNameFromRequest(request))
      return makeErrorResponse(context, request,
          "Compare Failed because the wrong number of Input Files were given. "
              + "Select two please.");
    firstFilePath = composeFileName(request, firstFileName);
    secondFilePath = composeFileName(request, secondFileName);

    if (!filesExist())
      return makeErrorResponse(context, request,
          "Compare Failed because the files were not found.");

    return makeResponseFromComparison(context, request);
  }

  private Response makeResponseFromComparison(FitNesseContext context,
      Request request) throws Exception {
    if (comparer.compare(firstFilePath, secondFilePath))
      return makeValidResponse(request);
    else {
      String message = "These files could not be compared."
          + "  They might be suites, or something else might be wrong.";
      return makeErrorResponse(context, request, message);
    }
  }

  private boolean filesExist() {
    return ((new File(firstFilePath)).exists())
        || ((new File(secondFilePath)).exists());
  }

  private void initializeReponseComponents() {
    if (comparer == null)
      comparer = new HistoryComparer();
  }

  private String composeFileName(Request request, String fileName) {
    return context.getTestHistoryDirectory().getPath() + File.separator
        + request.getResource() + File.separator + fileName;
  }

  private boolean getFileNameFromRequest(Request request) {
    firstFileName = "";
    secondFileName = "";
    Map<String, String> inputs = request.getMap();
    Set<String> keys = inputs.keySet();
    return setFileNames(keys);
  }

  private boolean setFileNames(Set<String> keys) {
    for (String key : keys) {
      if (key.contains("TestResult_"))
        if (setFileNames(key))
          return false;
    }
    return !(firstFileName.equals("") || secondFileName.equals(""));
  }

  private boolean setFileNames(String key) {
    if (firstFileName.equals(""))
      firstFileName = key.substring(key.indexOf("_") + 1);
    else if (secondFileName.equals(""))
      secondFileName = key.substring(key.indexOf("_") + 1);
    else
      return true;
    return false;
  }

  private Response makeValidResponse(Request request) throws UnsupportedEncodingException {
    int count = 0;
    HtmlPage page = context.pageFactory.newPage();
    page.setTitle("History Comparison");
    page.setPageTitle(makePageTitle(request.getResource()));
    if (!testing) {
      page.put("firstFileName", formatDate(firstFileName));
      page.put("secondFileName", formatDate(secondFileName));
      page.put("completeMatch", comparer.allTablesMatch());
      page.put("comparer", comparer);
    }
    page.put("resultContent", comparer.getResultContent());
    page.put("firstTables", comparer.firstTableResults);
    page.put("secondTables", comparer.secondTableResults);
    page.put("count", count);
    page.setMainTemplate("compareHistory");

    SimpleResponse response = new SimpleResponse();
    response.setContent(page.html(request));
    return response;
  }

  private Date formatDate(String fileName) {
    try {
      return dateFormat.parse(firstFileName);
    } catch (ParseException e) {
      throw new RuntimeException("File name '" + fileName + "' does not parse to a date", e);
    }
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

    return new PageTitle("Test History", PathParser.parse(resource),tags);
  }

  private Response makeErrorResponse(FitNesseContext context, Request request,
      String message) throws Exception {
    return new ErrorResponder(message).makeResponse(context, request);
  }
}
