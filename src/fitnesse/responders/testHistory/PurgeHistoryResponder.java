package fitnesse.responders.testHistory;

import fitnesse.FitNesseContext;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ErrorResponder;
import util.FileUtil;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PurgeHistoryResponder implements SecureResponder {
  private File resultsDirectory;
  private Date todaysDate;


  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
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
    todaysDate = new Date();
  }

  private SimpleResponse makeValidResponse() throws Exception {
    SimpleResponse response = new SimpleResponse();
    response.redirect("?testHistory");
    return response;
  }

  private void purgeHistory(Request request) throws ParseException {
    int days = getDaysInput(request);
    deleteTestHistoryOlderThanDays(days);
  }

  private Integer getDaysInput(Request request) {
    String daysInput = request.getInput("days").toString();
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

  public void setResultsDirectory(File directory) {
    resultsDirectory = directory;
  }

  public void setTodaysDate(Date date) {
    todaysDate = date;
  }

  public void deleteTestHistoryOlderThanDays(int days) throws ParseException {
    Date purgeOlder = getDateDaysEarlier(days);
    File[] files = FileUtil.getDirectoryListing(resultsDirectory);
    for (File file : files) {
      deleteFileIfAppropriate(purgeOlder, file);
    }
  }

  public Date getDateDaysEarlier(int days) {
    long now = todaysDate.getTime();
    long millisecondsPerDay = 1000L * 60L * 60L * 24L;
    Date daysEarlier = new Date(now - (millisecondsPerDay * days));
    return daysEarlier;
  }

  private void deleteFileIfAppropriate(Date purgeOlder, File file) {
    if (file.isDirectory()) {
      File[] files = FileUtil.getDirectoryListing(file);
      for (File childFile : files)
        deleteFileIfAppropriate(purgeOlder, childFile);
      if (file.list().length == 0)
        FileUtil.deleteFileSystemDirectory(file);
    } else
      deleteFileIfItIsTooOld(purgeOlder, file);
  }

  private void deleteFileIfItIsTooOld(Date purgeOlder, File file) {
    String name = file.getName();
    Date date = getDateFromPageHistoryFileName(name);
    if (date.getTime() < purgeOlder.getTime())
      FileUtil.deleteFile(file);
  }

  private Date getDateFromPageHistoryFileName(String name) {
    Date date;
    try {
      SimpleDateFormat dateFormat = new SimpleDateFormat(TestHistory.TEST_RESULT_FILE_DATE_PATTERN);
      date = dateFormat.parse(name.split("_")[0]);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
    return date;
  }


  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
