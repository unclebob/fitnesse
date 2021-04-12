package fitnesse.reporting.history;

import fitnesse.util.Clock;
import fitnesse.wiki.WikiPagePath;
import util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

public class HistoryPurger {
  private static final Logger LOG = Logger.getLogger(HistoryPurger.class.getName());

  private final File resultsDirectory;
  private final Date expirationDate;

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
        deleteIfExpired(file);
      }
    }
  }

  private void deleteExpiredFiles(File[] files) {
    for (File file : files)
      deleteIfExpired(file);
  }

  public Date getDateDaysAgo(int days) {
    long now = Clock.currentTimeInMillis();
    long millisecondsPerDay = 1000L * 60L * 60L * 24L;
    Date daysEarlier = new Date(now - (millisecondsPerDay * days));
    return daysEarlier;
  }

  private void deleteIfExpired(File file) {
    try {
      if (file.isDirectory()) {
        deleteDirectoryIfExpired(file);
      } else
        deleteFileIfExpired(file);
    } catch (IOException e) {
      LOG.log(Level.INFO, format("Unable to remove test history file %s", file.getPath()));
    }
  }

  private void deleteDirectoryIfExpired(File file) throws IOException {
    File[] files = FileUtil.listFiles(file);
    deleteExpiredFiles(files);
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

  private Date getDateFromPageHistoryFileName(String name) {
    try {
      return tryExtractDateFromTestHistoryName(name);
    } catch (ParseException e) {
      LOG.log(Level.INFO, format("Can not determine date from test history file %s", name));
      return new Date();
    }
  }

  private Date tryExtractDateFromTestHistoryName(String testHistoryName) throws ParseException {
    SimpleDateFormat dateFormat = new SimpleDateFormat(PageHistory.TEST_RESULT_FILE_DATE_PATTERN);
    String dateString = testHistoryName.split("_")[0];
    return dateFormat.parse(dateString);
  }
}
