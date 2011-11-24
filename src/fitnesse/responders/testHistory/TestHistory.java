package fitnesse.responders.testHistory;

import fitnesse.responders.run.TestSummary;
import fitnesse.wikitext.parser.WikiWordPath;
import util.FileUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class TestHistory {
  Map<String, File> pageDirectoryMap = new HashMap<String, File>();
  public static final String TEST_RESULT_FILE_DATE_PATTERN = "yyyyMMddHHmmss";

  public void readHistoryDirectory(File historyDirectory) {
    File[] pageDirectories = FileUtil.getDirectoryListing(historyDirectory);
    for (File file : pageDirectories)
      if (isValidFile(file))
        pageDirectoryMap.put(file.getName(), file);
  }

  private boolean isValidFile(File file) {
    return file.isDirectory() && file.list().length > 0 && WikiWordPath.isWikiWord(file.getName());
  }

  public Set<String> getPageNames() {
    return new TreeSet<String>(pageDirectoryMap.keySet());
  }

  public PageHistory getPageHistory(String pageName) {
    File pageHistoryDirectory = pageDirectoryMap.get(pageName);
    if (pageHistoryDirectory == null)
      return null;
    else {
      PageHistory pageHistory = new PageHistory(pageHistoryDirectory);
      if (pageHistory.size() == 0)
        return null;
      else
        return pageHistory;
    }
  }

  public void readPageHistoryDirectory(File historyDirectory, String pageName) {
    File[] pageDirectories = FileUtil.getDirectoryListing(historyDirectory);
    for (File file : pageDirectories)
      if ((isValidFile(file)) && file.getName().startsWith(pageName))
        pageDirectoryMap.put(file.getName(), file);
  }

  public static String makeResultFileName(TestSummary summary, long time) {
    SimpleDateFormat format = new SimpleDateFormat(TEST_RESULT_FILE_DATE_PATTERN);
    String datePart = format.format(new Date(time));
    return String.format("%s_%d_%d_%d_%d.xml", datePart, summary.getRight(), summary.getWrong(), summary.getIgnores(), summary.getExceptions());
  }
}
