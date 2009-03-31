package fitnesse.wiki;

import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

public class NullVersionsController implements VersionsController {
  public NullVersionsController() {
    this(new Properties());
  }

  public NullVersionsController(final Properties properties) {
  }

  public PageData getRevisionData(final FileSystemPage page, final String label) {
    try {
      return page.getData();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Collection<VersionInfo> history(final FileSystemPage page) {
    return new HashSet<VersionInfo>();
  }

  public boolean isExternalRevisionControlEnabled() {
    return true;
  }

  public VersionInfo makeVersion(final FileSystemPage page, final PageData data) {
    return new VersionInfo(page.getFileSystemPath());
  }

  public void prune(final FileSystemPage page) {
  }

  public void removeVersion(final FileSystemPage page, final String versionName) {
  }
}
