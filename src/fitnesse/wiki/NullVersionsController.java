package fitnesse.wiki;

import java.util.Collection;
import java.util.HashSet;

public class NullVersionsController implements VersionsController {
  private int historyDepth;

  public NullVersionsController() {
  }

  @Override
  public void setHistoryDepth(int historyDepth) {
    this.historyDepth = historyDepth;
  }

  public int getHistoryDepth() {
    return historyDepth;
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
}
