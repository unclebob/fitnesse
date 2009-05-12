package fitnesse.responders.testHistory;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.run.TestExecutionReport;
import fitnesse.responders.run.XmlFormatter;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.wiki.PathParser;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.io.BufferedInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PageHistoryResponder implements Responder {
  private File resultsDirectory;
  private SimpleDateFormat dateFormat = new SimpleDateFormat(XmlFormatter.TEST_RESULT_FILE_DATE_PATTERN);

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    SimpleResponse response = new SimpleResponse();
    if (resultsDirectory == null)
      resultsDirectory = context.getTestHistoryDirectory();
    TestHistory history = new TestHistory();
    history.readHistoryDirectory(resultsDirectory);
    String pageName = request.getResource();
    PageHistory pageHistory = history.getPageHistory(pageName);
    VelocityContext velocityContext = new VelocityContext();
    velocityContext.put("pageTitle", makePageTitle(request.getResource()));
    Template template;

    if (request.hasInput("resultDate")) {
      String date = (String) request.getInput("resultDate");
      Date resultDate = dateFormat.parse(date);
      PageHistory.TestResultRecord testResultRecord = pageHistory.get(resultDate);
      TestExecutionReport report = new TestExecutionReport(testResultRecord.getFile());
      report.setDate(resultDate);
      velocityContext.put("testExecutionReport", report);
      template = context.getVelocityEngine().getTemplate("testExecutionReport.vm");
    } else {
      velocityContext.put("pageHistory", pageHistory);
      template = context.getVelocityEngine().getTemplate("pageHistory.vm");
    }
    StringWriter writer = new StringWriter();

    template.merge(velocityContext, writer);
    response.setContent(writer.toString());
    return response;

  }

  private PageTitle makePageTitle(String resource) {
    return new PageTitle("Test History", PathParser.parse(resource));
  }

  public void setResultsDirectory(File resultsDirectory) {
    this.resultsDirectory = resultsDirectory;
  }

}
