package fitnesse.responders.testHistory;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.authentication.SecureReadOperation;
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

public class ExecutionLogResponder implements SecureResponder {
  private SimpleDateFormat dateFormat = new SimpleDateFormat(PageHistory.TEST_RESULT_FILE_DATE_PATTERN);
  private File resultsDirectory;
  private FitNesseContext context;

  public Response makeResponse(FitNesseContext context, Request request) {
    this.context = context;
    PageHistory pageHistory = getPageHistory(request);

    return tryToMakeExecutionLog(request, pageHistory);
  }

  private Response tryToMakeExecutionLog(Request request, PageHistory pageHistory) {
    Date resultDate;
    String date = request.getInput("resultDate");
    if (date == null || "latest".equals(date)) {
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
      return makeExecutionLogResponse(request, resultDate, testResultRecord);
    } catch (Exception e) {
      return makeCorruptFileResponse(request);
    }
  }

  private Response makeCorruptFileResponse(Request request) {
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
    response.setContent(page.html());
    return response;
  }

  private PageHistory getPageHistory(Request request) {
    if (resultsDirectory == null)
      resultsDirectory = context.getTestHistoryDirectory();
    TestHistory history = new TestHistory();
    String pageName = request.getResource();
    history.readPageHistoryDirectory(resultsDirectory, pageName);
    return history.getPageHistory(pageName);
  }

  public void setResultsDirectory(File resultsDirectory) {
    this.resultsDirectory = resultsDirectory;
  }

  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }
}
