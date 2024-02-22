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
    SimpleDateFormat dateFormat = PageHistory.getDateFormat();
    String dateString = testHistoryName.split("_")[0];
    return dateFormat.parse(dateString);
  }

  public void deleteTestHistoryByCount(WikiPagePath path, int testHistoryCount) {
		String pageName = path.toString();
		String subPagePrefix = pageName + ".";
		File[] files = FileUtil.getDirectoryListing(resultsDirectory);
		for (File file : files) {
			String fileName = file.getName();
			if (fileName.equals(pageName) || fileName.startsWith(subPagePrefix)) {
				deleteIfCountReached(file, testHistoryCount);
			}
		}
	}
	
	private void deleteIfCountReached(File file, int testHistoryCount) {
		try {
			if (file.isDirectory()) {
				deleteDirectoryIfCountReached(file, testHistoryCount);
			} else {
				deleteFilesIfCountReached(new File[] {file}, testHistoryCount);
			}
		} catch (IOException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
			LOG.log(Level.INFO, format("Unable to remove test history file %s", file.getPath()));
		}
	}
	
	private void deleteDirectoryIfCountReached(File file, int testHistoryCount) throws IOException {
		File[] files = FileUtil.listFiles(file);
		if(testHistoryCount > 0) {
			deleteFilesIfCountReached(files, testHistoryCount);
		}
		// Deleting the folder if it is empty
		if (FileUtil.isEmpty(file)) {
			FileUtil.deleteFileSystemDirectory(file);
		}
	}
	
	private void deleteFilesIfCountReached(File[] files, int testHistoryCount) throws IOException {
		// Sorting the files to have them in ascending order of creation
    Arrays.sort(files, Comparator.comparingLong(file -> getDateFromPageHistoryFileName(file.getName()).getTime()));
		// Only delete histories when there are more histories than the count expects
		if((files.length - testHistoryCount) > 0) {
			File[] filesToDelete = new File[files.length - testHistoryCount];
			// Putting all files up to the count in a list for deletion
			System.arraycopy(files, 0, filesToDelete, 0, files.length - testHistoryCount);
			for (File fileToDelete : filesToDelete) {
				FileUtil.deleteFile(fileToDelete);
			}
		}
	}
}
