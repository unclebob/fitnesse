package fitnesse.responders.testHistory;

import java.io.File;

import fitnesse.FitNesseContext;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.reporting.history.HistoryPurger;
import fitnesse.responders.ErrorResponder;

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
    File resultsDirectory = context.getTestHistoryDirectory();
    int days = getDaysInput(request);
    deleteTestHistoryOlderThanDays(resultsDirectory, days);
  }

  public void deleteTestHistoryOlderThanDays(File resultsDirectory, int days) {
    new HistoryPurger(resultsDirectory, days).deleteTestHistoryOlderThanDays();
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
