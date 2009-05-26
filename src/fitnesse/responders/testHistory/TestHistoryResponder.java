package fitnesse.responders.testHistory;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import org.apache.velocity.VelocityContext;

import java.io.File;

public class TestHistoryResponder implements Responder {
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
      response.setContent(context.translateTemplate(velocityContext, "testHistory.vm"));
    }
    return response;
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
}
