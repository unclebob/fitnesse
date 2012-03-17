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
import fitnesse.responders.templateUtilities.HtmlPage;
import fitnesse.responders.templateUtilities.PageTitle;
import org.apache.velocity.VelocityContext;

import java.io.File;

public class TestHistoryResponder implements SecureResponder {

  private FitNesseContext context;
  
  public Response makeResponse(FitNesseContext context, Request request) {
    this.context = context;
    File resultsDirectory = context.getTestHistoryDirectory();
    String pageName = request.getResource();
    TestHistory testHistory = new TestHistory();
    testHistory.readPageHistoryDirectory(resultsDirectory, pageName);

    if (formatIsXML(request)) {
      return makeTestHistoryXmlResponse(testHistory);
    } else {
      return makeTestHistoryResponse(testHistory, pageName);
    }
  }

  private Response makeTestHistoryResponse(TestHistory testHistory, String pageName) {
    HtmlPage page = context.htmlPageFactory.newPage();
    page.setTitle("Test History");
    page.setPageTitle(new PageTitle(makePageTitle(pageName)));
    page.put("testHistory", testHistory);
    page.setMainTemplate("testHistory.vm");
    SimpleResponse response = new SimpleResponse();
    response.setContent(page.html());
    return response;
  }

  private Response makeTestHistoryXmlResponse(TestHistory history) {
    SimpleResponse response = new SimpleResponse();
    VelocityContext velocityContext = new VelocityContext();
    velocityContext.put("testHistory", history);
    response.setContentType(Format.XML);
    response.setContent(VelocityFactory.translateTemplate(velocityContext, "testHistoryXML.vm"));
    return response;
  }
  
  private String makePageTitle(String pageName) {
    return "".equals(pageName) ?
      "Test History" :
      "Test History for " + pageName;
  }

  private boolean formatIsXML(Request request) {
    return (request.getInput("format") != null && request.getInput("format").toString().toLowerCase().equals("xml"));
  }

  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
