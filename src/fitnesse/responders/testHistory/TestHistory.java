package fitnesse.responders.testHistory;

import util.FileUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TestHistory {
  Map<String, File> pageDirectoryMap = new HashMap<String, File>();

  public void readHistoryDirectory(File historyDirectory) {
    File[] pageDirectories = FileUtil.getDirectoryListing(historyDirectory);
    for (File file : pageDirectories)
      if (file.isDirectory())
        pageDirectoryMap.put(file.getName(), file);
  }

  public Set<String> getPageNames() {
    return pageDirectoryMap.keySet();
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
}
