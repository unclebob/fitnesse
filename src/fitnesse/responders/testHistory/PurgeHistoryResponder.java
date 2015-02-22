package fitnesse.responders.testHistory;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import fitnesse.FitNesseContext;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.reporting.history.HistoryPurger;
import fitnesse.responders.ErrorResponder;
import fitnesse.responders.run.SuiteResponder;
import fitnesse.util.Clock;
import util.FileUtil;

public class PurgeHistoryResponder implements SecureResponder {
  private File resultsDirectory;

  public Response makeResponse(FitNesseContext context, Request request) {
    initializeResponder(context);
    if (hasValidInputs(request)) {
      purgeHistory(request);
      return makeValidResponse();
    } else {
      return makeErrorResponse(context, request);
    }
  }

  private void initializeResponder(FitNesseContext context) {
    if (resultsDirectory == null)
      resultsDirectory = context.getTestHistoryDirectory();
  }

  private SimpleResponse makeValidResponse() {
    SimpleResponse response = new SimpleResponse();
    response.redirect("", "?testHistory");
    return response;
  }

  private void purgeHistory(Request request) {
    int days = getDaysInput(request);
    deleteTestHistoryOlderThanDays(days);
  }

  public void deleteTestHistoryOlderThanDays(int days) {
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

  private Response makeErrorResponse(FitNesseContext context, Request request) {
    return new ErrorResponder("Invalid Amount Of Days").makeResponse(context, request);
  }

  public void setResultsDirectory(File directory) {
    resultsDirectory = directory;
  }

  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
