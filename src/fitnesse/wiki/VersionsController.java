package fitnesse.wiki;

import java.util.Collection;

public interface VersionsController {
  public PageData getRevisionData(FileSystemPage page, String label);

  public Collection<VersionInfo> history(FileSystemPage page);

  public VersionInfo makeVersion(FileSystemPage page, PageData data);

  public void removeVersion(FileSystemPage page, String versionName);

  public void prune(FileSystemPage page);

  public boolean isExternalRevisionControlEnabled();
}