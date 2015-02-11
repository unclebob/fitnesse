package fitnesse.reporting.history;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import fitnesse.util.Clock;
import util.FileUtil;

import static java.lang.String.format;

public class HistoryPurger {
  private static final Logger LOG = Logger.getLogger(HistoryPurger.class.getName());

  private final File resultsDirectory;

  public HistoryPurger(File resultsDirectory) {
    this.resultsDirectory = resultsDirectory;
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
    long now = Clock.currentTimeInMillis();
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
      LOG.log(Level.SEVERE, format("Can not determine date from test history file %s", name));
      return new Date();
    }
  }

  private Date tryExtractDateFromTestHistoryName(String testHistoryName) throws ParseException {
    SimpleDateFormat dateFormat = new SimpleDateFormat(PageHistory.TEST_RESULT_FILE_DATE_PATTERN);
    String dateString = testHistoryName.split("_")[0];
    return dateFormat.parse(dateString);
  }
}
