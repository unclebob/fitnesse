package fitnesse.responders.testHistory;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.run.TestExecutionReport;
import fitnesse.responders.run.XmlFormatter;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.responders.ErrorResponder;
import fitnesse.wiki.PathParser;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import java.io.File;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

public class PageHistoryResponder implements Responder {
  private File resultsDirectory;
  private SimpleDateFormat dateFormat = new SimpleDateFormat(XmlFormatter.TEST_RESULT_FILE_DATE_PATTERN);
  private SimpleResponse response;
  private TestHistory history;
  private String pageName;
  private PageHistory pageHistory;
  private VelocityContext velocityContext;

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    prepareResponse(context, request);

    if (request.hasInput("resultDate")) {
      return tryToMakeTestExecutionReport(context, request);
    } else {
      return makePageHistoryResponse(context);
    }

  }

  private Response makePageHistoryResponse(FitNesseContext context) throws Exception {
    velocityContext.put("pageHistory", pageHistory);
    Template template = context.getVelocityEngine().getTemplate("pageHistory.vm");
    return makeResponseFromTemplate(template);
  }

  private Response tryToMakeTestExecutionReport(FitNesseContext context, Request request) throws Exception {
    Date resultDate = getResultDate(request);
    PageHistory.TestResultRecord testResultRecord = pageHistory.get(resultDate);
    try {
      return makeTestExecutionReportResponse(context, resultDate, testResultRecord);
    } catch (Exception e) {
      return makeCorruptFileResponse(context, request);
    }
  }

  private Date getResultDate(Request request) throws ParseException {
    String date = (String) request.getInput("resultDate");
    Date resultDate = dateFormat.parse(date);
    return resultDate;
  }

  private Response makeCorruptFileResponse(FitNesseContext context, Request request) throws Exception {
    return new ErrorResponder("Corrupt Test Result File").makeResponse(context, request);
  }

  private Response makeTestExecutionReportResponse(FitNesseContext context, Date resultDate, PageHistory.TestResultRecord testResultRecord) throws Exception {
    TestExecutionReport report;
    report = new TestExecutionReport(testResultRecord.getFile());
    report.setDate(resultDate);
    velocityContext.put("testExecutionReport", report);
    Template template = context.getVelocityEngine().getTemplate("testExecutionReport.vm");
    return makeResponseFromTemplate(template);
  }

  private Response makeResponseFromTemplate(Template template) throws Exception {
    StringWriter writer = new StringWriter();
    template.merge(velocityContext, writer);
    response.setContent(writer.toString());
    return response;
  }

  private void prepareResponse(FitNesseContext context, Request request) {
    response = new SimpleResponse();
    if (resultsDirectory == null)
      resultsDirectory = context.getTestHistoryDirectory();
    history = new TestHistory();
    history.readHistoryDirectory(resultsDirectory);
    pageName = request.getResource();
    pageHistory = history.getPageHistory(pageName);
    velocityContext = new VelocityContext();
    velocityContext.put("pageTitle", makePageTitle(request.getResource()));
  }

  private PageTitle makePageTitle(String resource) {
    return new PageTitle("Test History", PathParser.parse(resource));
  }

  public void setResultsDirectory(File resultsDirectory) {
    this.resultsDirectory = resultsDirectory;
  }

}
