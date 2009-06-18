package fitnesse.responders.testHistory;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.run.XmlFormatter;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import util.FileUtil;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PurgeHistoryResponder implements Responder {
  private File resultsDirectory;
  private WikiPagePath currentPagePath;
  private PageCrawler crawler;
  private WikiPage currentPage;
  private Date todaysDate;


  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    if (resultsDirectory == null)
      resultsDirectory = context.getTestHistoryDirectory();
    todaysDate = new Date();
    
    if (request.getInput("days") != null) {
      String daysInput = request.getInput("days").toString();
      Integer days = new Integer(daysInput);
      deleteTestHistoryOlderThan(days);
    }
    currentPagePath = PathParser.parse(request.getResource());
    crawler = context.root.getPageCrawler();
    currentPage = crawler.getPage(context.root, currentPagePath);
    SimpleResponse response = new SimpleResponse();
    WikiPagePath fullPathOfCurrentPage = crawler.getFullPath(currentPage);
    response.redirect(fullPathOfCurrentPage.toString());
    return response;
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
    SimpleDateFormat dateFormat = new SimpleDateFormat(XmlFormatter.TEST_RESULT_FILE_DATE_PATTERN);
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
