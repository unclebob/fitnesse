package fitnesse.responders.testHistory;

import util.FileUtil;

import java.io.File;
import java.util.*;

public class TestHistory {
  Map<String, File> pageDirectoryMap = new HashMap<String, File>();

  public void readHistoryDirectory(File historyDirectory) {
    File [] pageDirectories = FileUtil.getDirectoryListing(historyDirectory);
    for (File file : pageDirectories)
      if (file.isDirectory())
        pageDirectoryMap.put(file.getName(), file);
  }

  public Set<String> getPageNames() {
    return pageDirectoryMap.keySet();
  }

  public PageHistory getPageHistory(String pageName) {
    PageHistory pageHistory = new PageHistory(pageDirectoryMap.get(pageName));

    return pageHistory;
  }
}
