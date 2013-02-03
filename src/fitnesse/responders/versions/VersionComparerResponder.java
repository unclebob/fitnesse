package fitnesse.responders.versions;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ErrorResponder;
import fitnesse.responders.templateUtilities.HtmlPage;

public class VersionComparerResponder implements Responder {

  private VersionComparer comparer;
  private String firstFileName;
  private String secondFileName;
  private String firstFilePath;
  private String secondFilePath;
  private FitNesseContext context;
  public boolean testing;

  public VersionComparerResponder(VersionComparer comparer) {
    this.comparer = comparer;
  }

  public VersionComparerResponder() {
    this(new VersionComparer());
  }

  @Override
  public Response makeResponse(FitNesseContext context, Request request)
      throws Exception {
    this.context = context;
    getFileNamesFromRequest(request);
    firstFilePath = composeFileName(request, firstFileName);
    secondFilePath = composeFileName(request, secondFileName);
    if (firstFileName.equals("") && secondFileName.equals("")) {
      String message = String.format("Compare Failed because no Input Files were given. Select one or two please.");
      return makeErrorResponse(context, request, message);
    }
    comparer.compare(firstFilePath, secondFilePath);
    return makeValidResponse(request);
  }

  private String composeFileName(Request request, String fileName) {
    if (fileName==null || fileName.length()==0)
      return context.getRootPagePath() + File.separator
          + request.getResource() + File.separator + "contents.txt";
    return context.getRootPagePath() + File.separator
        + request.getResource() + File.separator + fileName + ".zip#contents.txt";
  }

  private boolean getFileNamesFromRequest(Request request) {
    firstFileName = "";
    secondFileName = "";
    Map<String, Object> inputs = request.getMap();
    Set<String> keys = inputs.keySet();
    return setFileNames(keys);
  }

  private boolean setFileNames(Set<String> keys) {
    List<String> sortedkeys = Arrays.asList(keys.toArray(new String[keys.size()]));
    Collections.sort(sortedkeys);
    for (String key : sortedkeys) {
      if (key.contains("Version_"))
        if (setFileNames(key))
          return false;
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

  private Response makeErrorResponse(FitNesseContext context, Request request,
      String message) {
    return new ErrorResponder(message).makeResponse(context, request);
  }

  private Response makeValidResponse(Request request) {
    HtmlPage page = context.pageFactory.newPage();
    page.setTitle("Version Comparison");
    if (!testing) {
      page.put("differences", comparer.getDifferences());
      page.setMainTemplate("compareVersions");
    }
    SimpleResponse response = new SimpleResponse();
    response.setContent(page.html());
    return response;
  }
}
