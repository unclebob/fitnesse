package fitnesse.responders.testHistory;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import fitnesse.reporting.history.PageHistory;
import fitnesse.reporting.history.TestHistory;
import fitnesse.reporting.history.TestResultRecord;
import org.apache.velocity.VelocityContext;

import util.FileUtil;
import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.Response.Format;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ErrorResponder;
import fitnesse.reporting.history.ExecutionReport;
import fitnesse.reporting.history.SuiteExecutionReport;
import fitnesse.reporting.history.TestExecutionReport;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class PageHistoryResponder implements SecureResponder {
  private SimpleDateFormat dateFormat = new SimpleDateFormat(PageHistory.TEST_RESULT_FILE_DATE_PATTERN);
  private SimpleResponse response;
  private PageHistory pageHistory;
  private HtmlPage page;
  private FitNesseContext context;

  public Response makeResponse(FitNesseContext context, Request request) {
    this.context = context;
    prepareResponse(request);

    if (request.hasInput("resultDate")) {
      return tryToMakeTestExecutionReport(request);
    } else if (formatIsXML(request)) {
      return makePageHistoryXmlResponse();
    } else {
      return makePageHistoryResponse(request);
    }
  }

  private Response makePageHistoryResponse(Request request) {
    page.setTitle("Page History");
    page.put("pageHistory", pageHistory);
    page.setNavTemplate("viewNav");
    page.put("viewLocation", request.getResource());
    page.setMainTemplate("pageHistory");
    return makeResponse();
  }
  
  private Response makePageHistoryXmlResponse() {
    VelocityContext velocityContext = new VelocityContext();
    velocityContext.put("pageHistory", pageHistory);

    response.setContentType("text/xml");
    response.setContent(context.pageFactory.render(velocityContext, "pageHistoryXML.vm"));
    return response;
  }

  private boolean formatIsXML(Request request) {
    String format = request.getInput("format");
    return "xml".equalsIgnoreCase(format);
  }

  private Response tryToMakeTestExecutionReport(Request request) {
    Date resultDate;
    String date = request.getInput("resultDate");
    if ("latest".equals(date)) {
      resultDate = pageHistory.getLatestDate();
    } else {
      try {
        resultDate = dateFormat.parse(date);
      } catch (ParseException e) {
        throw new RuntimeException("Invalid date format provided", e);
      }
    }
    TestResultRecord testResultRecord = pageHistory.get(resultDate);
    try {
      return makeTestExecutionReportResponse(request, resultDate, testResultRecord);
    } catch (Exception e) {
      return makeCorruptFileResponse(request);
    }
  }

  private Response makeCorruptFileResponse(Request request) {
    return new ErrorResponder("Corrupt Test Result File").makeResponse(context, request);
  }

  private Response makeTestExecutionReportResponse(Request request, Date resultDate, TestResultRecord testResultRecord) throws Exception {
    if (formatIsXML(request))
      return generateXMLResponse(testResultRecord.getFile());
    ExecutionReport report;

    String content = FileUtil.getFileContent(testResultRecord.getFile());
    report = ExecutionReport.makeReport(content);
    if (report instanceof TestExecutionReport) {
      report.setDate(resultDate);
      return generateHtmlTestExecutionResponse(request, (TestExecutionReport) report);
    } else if (report instanceof SuiteExecutionReport) {
      return generateHtmlSuiteExecutionResponse(request, (SuiteExecutionReport) report);
    } else
      return makeCorruptFileResponse(request);
  }

  private Response generateHtmlSuiteExecutionResponse(Request request, SuiteExecutionReport report) throws Exception {
    page.setTitle("Suite Execution Report");
    page.setNavTemplate("viewNav");
    page.put("viewLocation", request.getResource());
    page.put("suiteExecutionReport", report);
    page.put("resultDate", dateFormat.format(report.getDate()));
    page.put("ExecutionResult", ExecutionResult.class);
    page.setMainTemplate("suiteExecutionReport");
    PageTitle pageTitle = new PageTitle("Suite History", PathParser.parse(request.getResource()), "");
    page.setPageTitle(pageTitle);

    return makeResponse();
  }

  private Response generateHtmlTestExecutionResponse(Request request, TestExecutionReport report) throws Exception {
    page.setTitle("Test Execution Report");
    page.setNavTemplate("viewNav");
    page.put("viewLocation", request.getResource());
    page.put("testExecutionReport", report);
    if (!report.getExecutionLogs().isEmpty()) {
      page.put("resultDate", dateFormat.format(report.getDate()));
    }
    page.put("ExecutionResult", ExecutionResult.class);
    page.setMainTemplate("testExecutionReport");
    page.setErrorNavTemplate("errorNavigator");
    String tags = report.getResults().get(0).getTags();
    PageTitle pageTitle = new PageTitle("Test History", PathParser.parse(request.getResource()), tags);
    page.setPageTitle(pageTitle);

    return makeResponse();
  }

  private Response generateXMLResponse(File file) {
    try {
      response.setContent(FileUtil.getFileContent(file));
    } catch (IOException e) {
      response.setContent("Error: Unable to read file '" + file.getName() + "'\n");
    }
    response.setContentType(Format.XML);
    return response;
  }

  private Response makeResponse() {
    response.setContent(page.html());
    return response;
  }

  private void prepareResponse(Request request) {
    response = new SimpleResponse();
    File resultsDirectory = context.getTestHistoryDirectory();
    TestHistory history = new TestHistory();
    String pageName = request.getResource();
    history.readPageHistoryDirectory(resultsDirectory, pageName);
    pageHistory = history.getPageHistory(pageName);
    page = context.pageFactory.newPage();
    PageTitle pageTitle = new PageTitle("Test History", PathParser.parse(request.getResource()), "");
    page.setPageTitle(pageTitle);

    String tags = "";    
    if (context.getRootPage() != null){
      WikiPagePath path = PathParser.parse(pageName);
      PageCrawler crawler = context.getRootPage().getPageCrawler();
      WikiPage wikiPage = crawler.getPage(path);
      if(wikiPage != null) {
        PageData pageData = wikiPage.getData();
        tags = pageData.getAttribute(PageData.PropertySUITES);
      }
    }
  }

  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }
}
