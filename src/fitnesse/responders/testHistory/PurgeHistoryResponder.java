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
import fitnesse.responders.ErrorResponder;
import fitnesse.responders.run.TestResponder;
import util.Clock;
import util.FileUtil;

public class PurgeHistoryResponder implements SecureResponder {
  private File resultsDirectory;
  private Date todaysDate;


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
    todaysDate = Clock.currentDate();
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

  private Response makeErrorResponse(FitNesseContext context, Request request) {
    return new ErrorResponder("Invalid Amount Of Days").makeResponse(context, request);
  }

  public void setResultsDirectory(File directory) {
    resultsDirectory = directory;
  }

  public void setTodaysDate(Date date) {
    todaysDate = new Date(date.getTime());
  }

  public void deleteTestHistoryOlderThanDays(int days) {
    Date expirationDate = getDateDaysAgo(days);
    File[] files = FileUtil.getDirectoryListing(resultsDirectory);
    deleteExpiredFiles(files, expirationDate);
  }

  private void deleteExpiredFiles(File[] files, Date expirationDate) {
    for (File file : files)
      deleteIfExpired(file, expirationDate);
  }

  public Date getDateDaysAgo(int days) {
    long now = todaysDate.getTime();
    long millisecondsPerDay = 1000L * 60L * 60L * 24L;
    Date daysEarlier = new Date(now - (millisecondsPerDay * days));
    return daysEarlier;
  }

  private void deleteIfExpired(File file, Date expirationDate) {
    if (file.isDirectory()) {
      deleteDirectoryIfExpired(file, expirationDate);
    } else
      deleteFileIfExpired(file, expirationDate);
  }

  private void deleteDirectoryIfExpired(File file, Date expirationDate) {
    File[] files = FileUtil.getDirectoryListing(file);
    deleteExpiredFiles(files, expirationDate);
    if (file.list().length == 0)
      FileUtil.deleteFileSystemDirectory(file);
  }

  private void deleteFileIfExpired(File file, Date purgeOlder) {
    String name = file.getName();
    Date date = getDateFromPageHistoryFileName(name);
    if (date.getTime() < purgeOlder.getTime())
      FileUtil.deleteFile(file);
  }

  private Date getDateFromPageHistoryFileName(String name) {
    try {
      return tryExtractDateFromTestHistoryName(name);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  private Date tryExtractDateFromTestHistoryName(String testHistoryName) throws ParseException {
    SimpleDateFormat dateFormat = new SimpleDateFormat(TestResponder.TEST_RESULT_FILE_DATE_PATTERN);
    String dateString = testHistoryName.split("_")[0];
    return dateFormat.parse(dateString);
  }


  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
