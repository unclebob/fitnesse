package fitnesse.responders.testHistory;

import fitnesse.FitNesseContext;
import fitnesse.VelocityFactory;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.Response.Format;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ErrorResponder;
import fitnesse.responders.run.ExecutionReport;
import fitnesse.responders.run.SuiteExecutionReport;
import fitnesse.responders.run.TestExecutionReport;
import fitnesse.responders.templateUtilities.HtmlPage;
import fitnesse.responders.templateUtilities.HtmlPageFactory;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.wiki.PathParser;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PageHistoryResponder implements SecureResponder {
  private File resultsDirectory;
  private SimpleDateFormat dateFormat = new SimpleDateFormat(TestHistory.TEST_RESULT_FILE_DATE_PATTERN);
  private SimpleResponse response;
  private TestHistory history;
  private String pageName;
  private PageHistory pageHistory;
  private HtmlPage page;
  private FitNesseContext context;
  private PageTitle pageTitle;

  public Response makeResponse(FitNesseContext context, Request request) {
    this.context = context;
    prepareResponse(request);

    if (request.hasInput("resultDate")) {
      return tryToMakeTestExecutionReport(request);
    } else if (formatIsXML(request)) {
      return makePageHistoryXmlResponse(request);
    } else {
      return makePageHistoryResponse(request);
    }
  }

  private Response makePageHistoryResponse(Request request) {
    page.setTitle("Page History");
    page.put("pageHistory", pageHistory);
    page.setMainTemplate("pageHistory.vm");
    return makeResponse();
  }
  
  private Response makePageHistoryXmlResponse(Request request) {
    VelocityContext velocityContext = new VelocityContext();
    velocityContext.put("pageHistory", pageHistory);
    Template template = VelocityFactory.getVelocityEngine().getTemplate("pageHistoryXML.vm");

    StringWriter writer = new StringWriter();
    template.merge(velocityContext, writer);

    response.setContentType("text/xml");
    response.setContent(writer.toString());
    return response;
  }

  private boolean formatIsXML(Request request) {
    return (request.getInput("format") != null && request.getInput("format").toString().toLowerCase().equals("xml"));
  }

  private Response tryToMakeTestExecutionReport(Request request) {
    Date resultDate;
    String date = (String) request.getInput("resultDate");
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
      return generateHtmlTestExecutionResponse((TestExecutionReport) report);
    } else if (report instanceof SuiteExecutionReport) {
      pageTitle.setPageType("Suite History");
      return generateHtmlSuiteExecutionResponse((SuiteExecutionReport) report);
    } else
      return makeCorruptFileResponse(request);
  }

  private Response generateHtmlSuiteExecutionResponse(SuiteExecutionReport report) throws Exception {
    page.setTitle("Suite Execution Report");
    page.put("suiteExecutionReport", report);
    page.setMainTemplate("suiteExecutionReport.vm");
    return makeResponse();
  }

  private Response generateHtmlTestExecutionResponse(TestExecutionReport report) throws Exception {
    page.setTitle("Test Execution Report");
    page.put("testExecutionReport", report);
    page.setMainTemplate("testExecutionReport.vm");
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
    StringWriter writer = new StringWriter();
    response.setContent(page.html());
    return response;
  }

  private void prepareResponse(Request request) {
    response = new SimpleResponse();
    if (resultsDirectory == null)
      resultsDirectory = context.getTestHistoryDirectory();
    history = new TestHistory();
    pageName = request.getResource();
    history.readPageHistoryDirectory(resultsDirectory, pageName);
    pageHistory = history.getPageHistory(pageName);
    page = context.htmlPageFactory.newPage();
    pageTitle = new PageTitle("Test History", PathParser.parse(request.getResource()));
    page.setPageTitle(pageTitle);
  }

  public void setResultsDirectory(File resultsDirectory) {
    this.resultsDirectory = resultsDirectory;
  }


  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
