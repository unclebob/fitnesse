package fitnesse.responders.testHistory;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.VelocityFactory;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ErrorResponder;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.wiki.PathParser;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import java.io.File;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;

public class HistoryComparerResponder implements Responder {
  public HistoryComparer comparer;
  public String baseDir = "";
  private VelocityContext velocityContext;
  private String firstFileName = "";
  private String secondFileName = "";
  private String firstFilePath;
  private String secondFilePath;

  public HistoryComparerResponder(HistoryComparer historyComparer) {
    comparer = historyComparer;
  }

  public HistoryComparerResponder() {
    comparer = new HistoryComparer();
  }

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    initializeReponseComponents(request);
    if (!getFileNameFromRequest(request))
      return makeErrorResponse(context, request, "Compare Failed because the wrong number of Input Files were given. " +
                                                  "Select two please.");
    firstFilePath = composeFileName(request, firstFileName);
    secondFilePath = composeFileName(request, secondFileName);

    if (!filesExist())
      return makeErrorResponse(context, request, "Compare Failed because the files were not found.");

    return makeResponseFromComparison(context, request);
  }

  private Response makeResponseFromComparison(FitNesseContext context, Request request) throws Exception {
    if (comparer.compare(firstFilePath, secondFilePath))
      return makeValidResponse();
    else
      return makeErrorResponse(context, request, "Comparison Failed. Try different files.");
  }

  private boolean filesExist() {
    return ((new File(firstFilePath)).exists()) || ((new File(secondFilePath)).exists());
  }

  private void initializeReponseComponents(Request request) {
    if (comparer == null)
      comparer = new HistoryComparer();
    if (baseDir.equals(""))
      baseDir = "FitNesseRoot/files/testResults/";
    velocityContext = new VelocityContext();
    velocityContext.put("pageTitle", makePageTitle(request.getResource()));
  }

  private String composeFileName(Request request, String fileName) {
    return baseDir + request.getResource() + "/" + fileName;
  }

  private boolean getFileNameFromRequest(Request request) {
    firstFileName = "";
    secondFileName = "";
    Map<String, Object> inputs = request.getMap();
    Set<String> keys = inputs.keySet();
    return setFileNames(keys);
  }

  private boolean setFileNames(Set<String> keys) {
    for (String key : keys) {
      if (key.contains("TestResult_"))
        if (setFileNames(key)) return false;
    }
    if (firstFileName.equals("") || secondFileName.equals(""))
      return false;
    return true;
  }

  private boolean setFileNames(String key) {
    if (firstFileName.equals(""))
      firstFileName = key.substring(key.indexOf("_") + 1);
    else if (secondFileName.equals(""))
      secondFileName = key.substring(key.indexOf("_") + 1);
    else
      return true;
    return false;
  }

  private Response makeValidResponse() throws Exception {
    velocityContext.put("resultContent", comparer.getResultContent());
    velocityContext.put("firstPage", comparer.firstFileContent);
    velocityContext.put("secondPage", comparer.secondFileContent);
    String velocityTemplate = "compareHistory.vm";
    Template template = VelocityFactory.getVelocityEngine().getTemplate(velocityTemplate);
    return makeResponseFromTemplate(template);

  }

  private Response makeResponseFromTemplate(Template template) throws Exception {
    StringWriter writer = new StringWriter();
    SimpleResponse response = new SimpleResponse();
    template.merge(velocityContext, writer);
    response.setContent(writer.toString());
    return response;
  }

  private PageTitle makePageTitle(String resource) {
    return new PageTitle("Test History", PathParser.parse(resource));

  }

  private Response makeErrorResponse(FitNesseContext context, Request request, String message) throws Exception {
    return new ErrorResponder(message).makeResponse(context, request);
  }
}
