package fitnesse.responders.testHistory;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import fitnesse.FitNesseContext;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.reporting.history.HistoryPurger;
import fitnesse.responders.ErrorResponder;
import fitnesse.wiki.WikiPagePath;

public class PurgeHistoryResponder implements SecureResponder {

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    if (hasValidInputs(request)) {
      purgeHistory(request, context);
      return makeValidResponse();
    } else {
      return makeErrorResponse(context, request);
    }
  }

  private SimpleResponse makeValidResponse() {
    SimpleResponse response = new SimpleResponse();
    response.redirect("", "?testHistory");
    return response;
  }

  private void purgeHistory(Request request, FitNesseContext context) {
    WikiPagePath currentPath = new WikiPagePath(request.getResource().split("\\."));
    String purgeGlobalInput = request.getInput("purgeGlobal");
    
    File resultsDirectory = context.getTestHistoryDirectory();
    int days = getDaysInput(request);
    // If purgeGlobal is not set then only delete current path
    if (StringUtils.isBlank(purgeGlobalInput) || !Boolean.parseBoolean(purgeGlobalInput)) {
      deleteTestHistoryOlderThanDays(resultsDirectory, days, currentPath);
    } else {
      deleteTestHistoryOlderThanDays(resultsDirectory, days, null);
    }
  }

  public void deleteTestHistoryOlderThanDays(File resultsDirectory, int days, WikiPagePath path) {
    HistoryPurger historyPurger = new HistoryPurger(resultsDirectory, days);
    if (path != null) {
      historyPurger.deleteTestHistoryOlderThanDays(path);
    } else {
      historyPurger.deleteTestHistoryOlderThanDays();
    }
  }

  private Integer getDaysInput(Request request) {
    String daysInput = request.getInput("days");
    return parseInt(daysInput);
  }

  private Integer parseInt(String daysInput) {
    try {
      return Integer.parseInt(daysInput);
    }
    catch (Exception e) {
      return -1;
    }
  }

  private boolean hasValidInputs(Request request) {
    return request.getInput("days") != null && getDaysInput(request) >= 0;

  }

  private Response makeErrorResponse(FitNesseContext context, Request request) throws Exception {
    return new ErrorResponder("Invalid Amount Of Days").makeResponse(context, request);
  }

  @Override
  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
