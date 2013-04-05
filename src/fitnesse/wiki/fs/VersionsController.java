package fitnesse.wiki.fs;

import java.util.Collection;

import fitnesse.wiki.PageData;
import fitnesse.wiki.VersionInfo;

public interface VersionsController {

  void setHistoryDepth(int historyDepth);

  PageData getRevisionData(FileSystemPage page, String label);

  Collection<? extends VersionInfo> history(FileSystemPage page);

  VersionInfo makeVersion(FileSystemPage page, PageData data);

  VersionInfo getCurrentVersion(FileSystemPage page);

  void delete(FileSystemPage page);
}