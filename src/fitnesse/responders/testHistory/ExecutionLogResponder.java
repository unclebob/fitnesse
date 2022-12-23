package fitnesse.responders.testHistory;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.reporting.history.ExecutionReport;
import fitnesse.reporting.history.PageHistory;
import fitnesse.reporting.history.TestExecutionReport;
import fitnesse.reporting.history.TestHistory;
import fitnesse.reporting.history.TestResultRecord;
import fitnesse.responders.ErrorResponder;
import fitnesse.wiki.PathParser;
import util.FileUtil;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExecutionLogResponder implements SecureResponder {
  private SimpleDateFormat dateFormat = PageHistory.getDateFormat();
  private File resultsDirectory;
  private FitNesseContext context;

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    this.context = context;
    PageHistory pageHistory = getPageHistory(request);
    String date = request.getInput("resultDate");
    Date resultDate;
    if (date == null || "latest".equals(date)) {
      resultDate = pageHistory.getLatestDate();
    } else {
      try {
        resultDate = dateFormat.parse(date);
      } catch (ParseException e) {
        String message = "Invalid date format provided: should be " + PageHistory.getDateFormat().toPattern() + ".";
        return new ErrorResponder(message).makeResponse(context, request);
      }
    }
    TestResultRecord testResultRecord = pageHistory.get(resultDate);
    try {
      return makeExecutionLogResponse(request, resultDate, testResultRecord);
    } catch (Exception e) {
      return makeCorruptFileResponse(request);
    }
  }

  private Response makeCorruptFileResponse(Request request) throws Exception {
    return new ErrorResponder("Corrupt Test Result File").makeResponse(context, request);
  }

  private Response makeExecutionLogResponse(Request request, Date resultDate, TestResultRecord testResultRecord) throws Exception {
    String content = FileUtil.getFileContent(testResultRecord.getFile());
    ExecutionReport report = ExecutionReport.makeReport(content);
    HtmlPage page = context.pageFactory.newPage();
    String tags = "";
    if (report instanceof TestExecutionReport && !((TestExecutionReport) report).getResults().isEmpty()) {
      tags = ((TestExecutionReport) report).getResults().get(0).getTags();
    }
    PageTitle pageTitle = new PageTitle("Execution Log", PathParser.parse(request.getResource()), tags);
    page.setPageTitle(pageTitle);
    page.setTitle("Execution Log");
    page.setNavTemplate("viewNav");
    page.put("currentDate", resultDate);
    page.put("resultDate", dateFormat.format(resultDate));
    page.put("version", report.getVersion());
    page.put("viewLocation", request.getResource());
    page.put("runTime", report.getTotalRunTimeInMillis() / 1000);
    page.put("logs", report.getExecutionLogs());
    page.setMainTemplate("executionLog");
    SimpleResponse response = new SimpleResponse();
    response.setContent(page.html(request));
    return response;
  }

  private PageHistory getPageHistory(Request request) {
    if (resultsDirectory == null)
      resultsDirectory = context.getTestHistoryDirectory();
    String pageName = request.getResource();
    TestHistory history = new TestHistory(resultsDirectory, pageName);
    return history.getPageHistory(pageName);
  }

  public void setResultsDirectory(File resultsDirectory) {
    this.resultsDirectory = resultsDirectory;
  }

  @Override
  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }
}
