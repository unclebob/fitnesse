package fitnesse.responders.testHistory;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.Response.Format;
import fitnesse.http.SimpleResponse;
import fitnesse.reporting.history.ExecutionReport;
import fitnesse.reporting.history.PageHistory;
import fitnesse.reporting.history.SuiteExecutionReport;
import fitnesse.reporting.history.TestExecutionReport;
import fitnesse.reporting.history.TestHistory;
import fitnesse.reporting.history.TestResultRecord;
import fitnesse.responders.ErrorResponder;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.wiki.PathParser;
import org.apache.velocity.VelocityContext;
import util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PageHistoryResponder implements SecureResponder {
  private SimpleDateFormat dateFormat = PageHistory.getDateFormat();
  private SimpleResponse response;
  private PageHistory pageHistory;
  private HtmlPage page;
  private FitNesseContext context;

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
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

  private Response makePageHistoryResponse(Request request) throws UnsupportedEncodingException {
    page.setTitle("Page History");
    page.put("pageHistory", pageHistory);
    page.setNavTemplate("viewNav");
    page.put("viewLocation", request.getResource());
    page.setMainTemplate("pageHistory");
    return makeResponse(request);
  }

  private Response makePageHistoryXmlResponse() throws UnsupportedEncodingException {
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

  private Response tryToMakeTestExecutionReport(Request request) throws Exception {
    Date resultDate;
    String date = request.getInput("resultDate");
    if ("latest".equals(date)) {
      resultDate = pageHistory.getLatestDate();
    } else {
      resultDate = dateFormat.parse(date);
    }
    TestResultRecord testResultRecord = pageHistory.get(resultDate);
    try {
      return makeTestExecutionReportResponse(request, resultDate, testResultRecord);
    } catch (Exception e) {
      return makeCorruptFileResponse(request);
    }
  }

  private Response makeCorruptFileResponse(Request request) throws Exception {
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

    return makeResponse(request);
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

    return makeResponse(request);
  }

  private Response generateXMLResponse(File file) throws UnsupportedEncodingException {
    try {
      response.setContent(FileUtil.getFileContent(file));
    } catch (IOException e) {
      response.setContent("Error: Unable to read file '" + file.getName() + "'\n");
    }
    response.setContentType(Format.XML);
    return response;
  }

  private Response makeResponse(Request request) throws UnsupportedEncodingException {
    response.setContent(page.html(request));
    return response;
  }

  private void prepareResponse(Request request) {
    response = new SimpleResponse();
    File resultsDirectory = context.getTestHistoryDirectory();
    String pageName = request.getResource();
    TestHistory history = new TestHistory(resultsDirectory, pageName);
    pageHistory = history.getPageHistory(pageName);
    page = context.pageFactory.newPage();
    PageTitle pageTitle = new PageTitle("Test History", PathParser.parse(request.getResource()), "");
    page.setPageTitle(pageTitle);
  }

  @Override
  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }
}
