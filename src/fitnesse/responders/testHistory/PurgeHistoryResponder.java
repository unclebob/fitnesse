package fitnesse.responders.testHistory;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.run.XmlFormatter;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.responders.ErrorResponder;
import org.apache.velocity.VelocityContext;
import util.FileUtil;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PurgeHistoryResponder implements Responder {
  private File resultsDirectory;
  private Date todaysDate;


  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    if (resultsDirectory == null)
      resultsDirectory = context.getTestHistoryDirectory();
    todaysDate = new Date();
    if (hasValidInputs(request)) {
      purgeHistory(request);
      SimpleResponse response = makeValidResponse(context);
      return response;
    } else {
      Response response = makeErrorResponse(context, request);
      return response;
    }
  }

  private SimpleResponse makeValidResponse(FitNesseContext context) throws Exception {
    SimpleResponse response = new SimpleResponse();
    TestHistory history = new TestHistory();
    history.readHistoryDirectory(resultsDirectory);
    VelocityContext velocityContext = new VelocityContext();
    velocityContext.put("pageTitle", new PageTitle("Test History"));
    velocityContext.put("testHistory", history);
    response.setContent(context.translateTemplate(velocityContext, "testHistory.vm"));
    return response;
  }

  private void purgeHistory(Request request) throws ParseException {
    Integer days = getDaysInput(request);
    deleteTestHistoryOlderThan(days);
  }

  private Integer getDaysInput(Request request) {
    String daysInput = request.getInput("days").toString();
    Integer days;
    try{
      days = Integer.parseInt(daysInput);
     }
    catch (Exception e){
       days = -1;
    }

    return days;
  }

  private boolean hasValidInputs(Request request) {
    if (request.getInput("days") == null)
      return false;
    Integer days = getDaysInput(request);
    if (days < 0)
      return false;
    return true;

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

  public void deleteTestHistoryOlderThan(int days) throws ParseException {
    long minimumDate = getTheMinimumDate(days);
    File[] files = FileUtil.getDirectoryListing(resultsDirectory);
    for (File file : files) {
      deleteFileIfAppropriate(minimumDate, file);
    }
  }

  public long getTheMinimumDate(int days) {
    SimpleDateFormat dateFormat = new SimpleDateFormat(TestHistory.TEST_RESULT_FILE_DATE_PATTERN);
    String dateText = dateFormat.format(todaysDate);
    long dateStandard = new Long(dateText);
    long formatDays = 1000000;
    dateStandard = dateStandard - days * formatDays;
    return dateStandard;
  }

  private void deleteFileIfAppropriate(long dateStandard, File file) {
    if (file.isDirectory()) {
      File[] files = FileUtil.getDirectoryListing(file);
      for (File childFile : files)
        deleteFileIfAppropriate(dateStandard, childFile);
    } else
      deleteFileIfItIsTooOld(dateStandard, file);
  }

  private void deleteFileIfItIsTooOld(long dateStandard, File file) {
    String name = file.getName();
    String dateFromName = name.split("_")[0];
    long date = new Long(dateFromName);
    if (date < dateStandard)
      FileUtil.deleteFile(file);
  }


}
