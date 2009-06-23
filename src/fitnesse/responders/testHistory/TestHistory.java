package fitnesse.responders.testHistory;

import fitnesse.wikitext.widgets.WikiWordWidget;
import fitnesse.responders.run.TestSummary;
import util.FileUtil;

import java.io.File;
import java.util.*;
import java.text.SimpleDateFormat;

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
    return file.isDirectory() && file.list().length > 0 && WikiWordWidget.isWikiWord(file.getName());
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

  public static String makeResultFileName(TestSummary summary, long time) {
    SimpleDateFormat format = new SimpleDateFormat(TEST_RESULT_FILE_DATE_PATTERN);
    String datePart = format.format(new Date(time));
    return String.format("%s_%d_%d_%d_%d.xml", datePart, summary.getRight(), summary.getWrong(), summary.getIgnores(), summary.getExceptions());
  }
}
