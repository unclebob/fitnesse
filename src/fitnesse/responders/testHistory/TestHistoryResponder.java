package fitnesse.responders.testHistory;

import fitnesse.FitNesseContext;
import fitnesse.VelocityFactory;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.templateUtilities.PageTitle;
import org.apache.velocity.VelocityContext;

import java.io.File;

public class TestHistoryResponder implements SecureResponder {
  private File resultsDirectory;
  private boolean generateNullResponseForTest;

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    if (resultsDirectory == null)
      resultsDirectory = context.getTestHistoryDirectory();
    SimpleResponse response = new SimpleResponse();
    if (!generateNullResponseForTest) {
      TestHistory history = new TestHistory();
      history.readHistoryDirectory(resultsDirectory);
      VelocityContext velocityContext = new VelocityContext();
      velocityContext.put("pageTitle", new PageTitle("Test History"));
      velocityContext.put("testHistory", history);
      String velocityTemplate = "testHistory.vm";
      if(formatIsXML(request)){
        response.setContentType("text/xml");
        velocityTemplate = "testHistoryXML.vm";
      }
      response.setContent(VelocityFactory.translateTemplate(velocityContext, velocityTemplate));
    }
    return response;
  }

  private boolean formatIsXML(Request request) {
    return (request.getInput("format") != null && request.getInput("format").toString().toLowerCase().equals("xml"));
  }

  public void setResultsDirectory(File resultsDirectory) {
    this.resultsDirectory = resultsDirectory;
  }

  public File getResultsDirectory() {
    return resultsDirectory;
  }

  public void generateNullResponseForTest() {
    generateNullResponseForTest = true;
  }

  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
