package fitnesse.wiki;

import java.util.Collection;

public interface VersionsController {

  void setHistoryDepth(int historyDepth);

  PageData getRevisionData(FileSystemPage page, String label);

  Collection<? extends VersionInfo> history(FileSystemPage page);

  VersionInfo makeVersion(FileSystemPage page, PageData data);

  VersionInfo getCurrentVersion(FileSystemPage page);
}