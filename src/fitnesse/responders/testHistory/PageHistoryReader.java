package fitnesse.responders.testHistory;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import fitnesse.FitNesseContext;
import fitnesse.responders.run.TestSummary;
import fitnesse.wiki.WikiPage;

import util.FileUtil;

public class PageHistoryReader {

  private SimpleDateFormat dateFormat = new SimpleDateFormat(TestHistory.TEST_RESULT_FILE_DATE_PATTERN);
  public static final String TEST_FILE_FORMAT = "\\A\\d{14}_\\d+_\\d+_\\d+_\\d+(.xml)*\\Z";
  
  void readHistoryFromPageDirectory(File pageDirectory) throws ParseException {
    File[] resultDir = FileUtil.getDirectoryListing(pageDirectory);

    for (File file : resultDir) {
      if (fileIsNotADirectoryAndIsValid(file)) {
        compileResultFileIntoHistory(file);
      }
    }
  }

  private boolean fileIsNotADirectoryAndIsValid(File file) {
    if(file.isDirectory())
      return false;
    if(!matchesPageHistoryFileFormat(file.getName()))
      return false;
    return true;

  }

  public static boolean matchesPageHistoryFileFormat(String pageHistoryFileName) {
    return pageHistoryFileName.matches(TEST_FILE_FORMAT);
  }
  
  private void compileResultFileIntoHistory(File file) throws ParseException {
    TestResultRecord record = buildTestResultRecord(file);
    processTestFile(record);
  }

  void processTestFile(TestResultRecord record) throws ParseException {
    // for subclasses.
  }
  
  private TestResultRecord buildTestResultRecord(File file) throws ParseException {
    String parts[] = file.getName().split("_|\\.");
    Date date = dateFormat.parse(parts[0]);
    TestResultRecord testResultRecord = new TestResultRecord(
      file,
      date,
      Integer.parseInt(parts[1]),
      Integer.parseInt(parts[2]),
      Integer.parseInt(parts[3]),
      Integer.parseInt(parts[4]));
    return testResultRecord;
  }
 
  public static String makePageHistoryFileName(FitNesseContext context, WikiPage page, TestSummary counts, long time) throws Exception {
    return String.format("%s/%s/%s",
      context.getTestHistoryDirectory(),
      page.getPageCrawler().getFullPath(page).toString(),
      TestHistory.makeResultFileName(counts, time));
  }
}