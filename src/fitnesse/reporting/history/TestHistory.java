package fitnesse.reporting.history;

import fitnesse.reporting.history.PageHistory;
import fitnesse.testsystems.TestSummary;
import fitnesse.wikitext.parser.WikiWordPath;
import util.FileUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class TestHistory {
  Map<String, File> pageDirectoryMap = new HashMap<String, File>();

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

}
