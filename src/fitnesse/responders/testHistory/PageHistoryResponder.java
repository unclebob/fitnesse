package fitnesse.responders.testHistory;

import fitnesse.FitNesseContext;
import fitnesse.VelocityFactory;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ErrorResponder;
import fitnesse.responders.run.ExecutionReport;
import fitnesse.responders.run.SuiteExecutionReport;
import fitnesse.responders.run.TestExecutionReport;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.wiki.PathParser;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import util.FileUtil;

import java.io.File;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PageHistoryResponder implements SecureResponder {
  private File resultsDirectory;
  private SimpleDateFormat dateFormat = new SimpleDateFormat(TestHistory.TEST_RESULT_FILE_DATE_PATTERN);
  private SimpleResponse response;
  private TestHistory history;
  private String pageName;
  private PageHistory pageHistory;
  private VelocityContext velocityContext;
  private FitNesseContext context;
  private PageTitle pageTitle;

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    this.context = context;
    prepareResponse(request);

    if (request.hasInput("resultDate")) {
      return tryToMakeTestExecutionReport(request);
    } else {
      return makePageHistoryResponse(request);
    }
  }

  private Response makePageHistoryResponse(Request request) throws Exception {
    velocityContext.put("pageHistory", pageHistory);
    String velocityTemplate = "pageHistory.vm";
    if (formatIsXML(request)) {
      response.setContentType("text/xml");
      velocityTemplate = "pageHistoryXML.vm";
    }
    Template template = VelocityFactory.getVelocityEngine().getTemplate(velocityTemplate);
    return makeResponseFromTemplate(template);
  }

  private boolean formatIsXML(Request request) {
    return (request.getInput("format") != null && request.getInput("format").toString().toLowerCase().equals("xml"));
  }

  private Response tryToMakeTestExecutionReport(Request request) throws Exception {
    Date resultDate;
    String date = (String) request.getInput("resultDate");
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
      return generateHtmlTestExecutionResponse((TestExecutionReport) report);
    } else if (report instanceof SuiteExecutionReport) {
      pageTitle.setPageType("Suite History");
      return generateHtmlSuiteExecutionResponse((SuiteExecutionReport) report);
    } else
      return makeCorruptFileResponse(request);
  }

  private Response generateHtmlSuiteExecutionResponse(SuiteExecutionReport report) throws Exception {
    velocityContext.put("suiteExecutionReport", report);
    Template template = VelocityFactory.getVelocityEngine().getTemplate("suiteExecutionReport.vm");
    return makeResponseFromTemplate(template);
  }

  private Response generateHtmlTestExecutionResponse(TestExecutionReport report) throws Exception {
    velocityContext.put("testExecutionReport", report);
    Template template = VelocityFactory.getVelocityEngine().getTemplate("testExecutionReport.vm");
    return makeResponseFromTemplate(template);
  }

  private Response generateXMLResponse(File file) throws Exception {
    response.setContent(FileUtil.getFileContent(file));
    response.setContentType("text/xml");
    return response;
  }

  private Response makeResponseFromTemplate(Template template) throws Exception {
    StringWriter writer = new StringWriter();
    template.merge(velocityContext, writer);
    response.setContent(writer.toString());
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
    velocityContext = new VelocityContext();
    pageTitle = makePageTitle(request.getResource());
    velocityContext.put("pageTitle", pageTitle);
  }

  private PageTitle makePageTitle(String resource) {
    return new PageTitle("Test History", PathParser.parse(resource));
  }

  public void setResultsDirectory(File resultsDirectory) {
    this.resultsDirectory = resultsDirectory;
  }


  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
