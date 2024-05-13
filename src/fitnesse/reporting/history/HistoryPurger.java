package fitnesse.reporting.history;

import fitnesse.util.Clock;
import fitnesse.wiki.WikiPagePath;
import util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import static java.lang.String.format;

public class HistoryPurger {
  private static final Logger LOG = Logger.getLogger(HistoryPurger.class.getName());

  private final File resultsDirectory;
  private final Date expirationDate;
  private Integer testhistoryCount;

  public HistoryPurger(File resultsDirectory, int days) {
    this.resultsDirectory = resultsDirectory;
    this.expirationDate = getDateDaysAgo(days);
  }

  public void deleteTestHistoryOlderThanDays() {
    File[] files = FileUtil.getDirectoryListing(resultsDirectory);
    deleteExpiredFiles(files);
  }

  public void deleteTestHistoryOlderThanDays(WikiPagePath path) {
    String pageName = path.toString();
    String subPagePrefix = pageName + ".";
    File[] files = FileUtil.getDirectoryListing(resultsDirectory);
    for (File file : files) {
      String fileName = file.getName();
      if (fileName.equals(pageName) || fileName.startsWith(subPagePrefix)) {
        delete(file);
      }
    }
  }
  
  public void deleteTestHistoryByCount(WikiPagePath path, String testhistoryMaxCount) {
    if (testhistoryMaxCount == null
        || !StringUtils.isNumeric(testhistoryMaxCount)) {
      LOG.fine(
          "The given testhistoryMaxCount must not be null and it has to be a valid number");
      return;
    }
    
    this.testhistoryCount = Integer.parseInt(testhistoryMaxCount);
    String pageName = path.toString();
    String subPagePrefix = pageName + ".";
    File[] files = FileUtil.getDirectoryListing(resultsDirectory);
    for (File file : files) {
      String fileName = file.getName();
      if (fileName.equals(pageName) || fileName.startsWith(subPagePrefix)) {
        delete(file);
      }
    }
  }

  private void deleteExpiredFiles(File[] files) {
    for (File file : files)
      delete(file);
  }

  public Date getDateDaysAgo(int days) {
    long now = Clock.currentTimeInMillis();
    long millisecondsPerDay = 1000L * 60L * 60L * 24L;
    Date daysEarlier = new Date(now - (millisecondsPerDay * days));
    return daysEarlier;
  }

  private void delete(File file) {
    try {
      if (file.isDirectory()) {
        deleteDirectory(file);
      } else
        deleteFileIfExpired(file);
    } catch (IOException e) {
      LOG.log(Level.INFO, format("Unable to remove test history file %s", file.getPath()));
    }
  }

  private void deleteDirectory(File file) throws IOException {
    File[] files = FileUtil.listFiles(file);
    if(testhistoryCount != null) {
      deleteFilesIfCountReached(files);
    } else {
      deleteExpiredFiles(files);
    }
    if (FileUtil.isEmpty(file)) {
      FileUtil.deleteFileSystemDirectory(file);
    }
  }

  private void deleteFileIfExpired(File file) throws IOException {
    String name = file.getName();
    Date date = getDateFromPageHistoryFileName(name);
    if (date.getTime() < expirationDate.getTime())
      FileUtil.deleteFile(file);
  }

  private void deleteFilesIfCountReached(File[] files) throws IOException {
    // Only delete histories when there are more histories than the count expects
    if((files.length - this.testhistoryCount) > 0) {
      // Sorting the files to have them in ascending order of creation
      Arrays.sort(files, Comparator.comparing(file -> getDateFromPageHistoryFileName(file.getName()), Date::compareTo));
      File[] filesToDelete = new File[files.length - this.testhistoryCount];

      System.arraycopy(files, 0, filesToDelete, 0, files.length - this.testhistoryCount);
      for (File fileToDelete : filesToDelete) {
        FileUtil.deleteFile(fileToDelete);
      }
    }
  }

  private Date getDateFromPageHistoryFileName(String name) {
    try {
      return tryExtractDateFromTestHistoryName(name);
    } catch (ParseException e) {
      LOG.log(Level.INFO, format("Can not determine date from test history file %s", name));
      return new Date();
    }
  }

  private Date tryExtractDateFromTestHistoryName(String testHistoryName) throws ParseException {
    SimpleDateFormat dateFormat = PageHistory.getDateFormat();
    String dateString = testHistoryName.split("_")[0];
    return dateFormat.parse(dateString);
  }
}
