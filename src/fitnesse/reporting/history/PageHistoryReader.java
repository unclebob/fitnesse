package fitnesse.reporting.history;

import util.FileUtil;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.String.format;

public class PageHistoryReader {

  private SimpleDateFormat dateFormat = PageHistory.getDateFormat();
  public static final String TEST_FILE_FORMAT = "\\A\\d{14}_\\d+_\\d+_\\d+_\\d+(.xml)*\\Z";

  void readHistoryFromPageDirectory(File pageDirectory) {
    File[] resultDir = FileUtil.getDirectoryListing(pageDirectory);

    for (File file : resultDir) {
      if (fileIsNotADirectoryAndIsValid(file)) {
        compileResultFileIntoHistory(file);
      }
    }
  }

  private boolean fileIsNotADirectoryAndIsValid(File file) {
    return !file.isDirectory() && matchesPageHistoryFileFormat(file.getName());
  }

  public static boolean matchesPageHistoryFileFormat(String pageHistoryFileName) {
    return pageHistoryFileName.matches(TEST_FILE_FORMAT);
  }

  private void compileResultFileIntoHistory(File file) {
    TestResultRecord record = buildTestResultRecord(file);
    processTestFile(record);
  }

  void processTestFile(TestResultRecord record) {
    // for subclasses.
  }

  private TestResultRecord buildTestResultRecord(File file) {
    String[] parts = file.getName().split("_|\\.");
    Date date;
    try {
      date = dateFormat.parse(parts[0]);
    } catch (ParseException e) {
      throw new IllegalStateException(format("'%s' can not be parsed to a valid date", parts[0]), e);
    }
    return new TestResultRecord(
      file,
      date,
      Integer.parseInt(parts[1]),
      Integer.parseInt(parts[2]),
      Integer.parseInt(parts[3]),
      Integer.parseInt(parts[4]));
  }

}
