package fitnesse.wiki;

import java.util.Collection;
import java.util.HashSet;

public class NullVersionsController implements VersionsController {
  public NullVersionsController() {
  }

  @Override
  public void setHistoryDepth(int historyDepth) {
  }

  @Override
  public PageData getRevisionData(final FileSystemPage page, final String label) {
    try {
      return page.getData();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Collection<VersionInfo> history(final FileSystemPage page) {
    return new HashSet<VersionInfo>();
  }

  @Override
  public VersionInfo makeVersion(final FileSystemPage page, final PageData data) {
    return new VersionInfo(page.getFileSystemPath());
  }

  @Override
  public void prune(final FileSystemPage page) {
  }

  @Override
  public void removeVersion(final FileSystemPage page, final String versionName) {
  }
}
