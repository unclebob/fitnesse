package fitnesse.reporting.history;

import fitnesse.wiki.PathParser;
import util.FileUtil;

import java.io.File;
import java.util.*;

public class TestHistory {
  final Map<String, File> pageDirectoryMap = new HashMap<>();

  public TestHistory(File historyDirectory) {
    readHistoryDirectory(historyDirectory);
  }

  public TestHistory(File historyDirectory, String pageName) {
    if ("".equals(pageName)) {
      // Top Level Request
      readHistoryDirectory(historyDirectory);
    } else {
      readPageHistoryDirectory(historyDirectory, pageName);
    }
  }

  private void readHistoryDirectory(File historyDirectory) {
    File[] pageDirectories = FileUtil.getDirectoryListing(historyDirectory);
    for (File file : pageDirectories)
      if (isValidFile(file))
        pageDirectoryMap.put(file.getName(), file);
  }

  private boolean isValidFile(File file) {
    return file.isDirectory() && file.list().length > 0 && PathParser.isWikiPath(file.getName());
  }

  public Set<String> getPageNames() {
    return new TreeSet<>(pageDirectoryMap.keySet());
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

  /*
   * Includes result pages from the page itself and all child pages
   */
  private void readPageHistoryDirectory(File historyDirectory, String pageName) {
    File[] pageDirectories = FileUtil.getDirectoryListing(historyDirectory);
    String childPageNames = pageName + ".";
    for (File file : pageDirectories)
      if ((isValidFile(file)) && (file.getName().equals(pageName) || file.getName().startsWith(childPageNames)))
        pageDirectoryMap.put(file.getName(), file);
  }

}
