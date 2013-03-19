package fitnesse.wiki;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import fitnesse.wiki.storage.FileSystem;

public class MemoryVersionsController implements VersionsController {

  private static int counter = 0;

  public static int nextId() {
    return counter++;
  }


  private Map<String, FileVersions> versions = new HashMap<String, FileVersions>();
  private int historyDepth;

  private VersionsController persistence;

  MemoryVersionsController(FileSystem fileSystem) {
    this.persistence = new SimpleFileVersionsController(fileSystem);
  }

  @Override
  public void setHistoryDepth(int historyDepth) {
    this.historyDepth = historyDepth;
  }

  @Override
  public PageData getRevisionData(FileSystemPage page, String label) {
    if (label == null || label.startsWith("0-")) {
      return persistence.getRevisionData(page, null);
    }
    FileVersions fileVersions = getFileVersions(page);
    if (fileVersions == null) return null;
    return fileVersions.getRevisionData(label);
  }

  @Override
  public Collection<VersionInfo> history(FileSystemPage page) {
    FileVersions fileVersions = getFileVersions(page);
    if (fileVersions == null) return null;
    return fileVersions.history();
  }

  @Override
  public VersionInfo makeVersion(FileSystemPage page, PageData data) {
    FileVersions fileVersions = getFileVersions(page);
    fileVersions.makeVersion(data);
    return persistence.makeVersion(page, data);
  }

  @Override
  public VersionInfo getCurrentVersion(FileSystemPage page) {
    return persistence.getCurrentVersion(page);
  }

  private FileVersions getFileVersions(FileSystemPage page) {
    final String key = page.getFileSystemPath();
    FileVersions fileVersions = versions.get(key);
    if (fileVersions == null) {
      fileVersions = new FileVersions();
      versions.put(key, fileVersions);
    }
    return fileVersions;
  }

  private static class FileVersions {
    protected Map<String, PageData> versions = new ConcurrentHashMap<String, PageData>();

    protected VersionInfo makeVersion(PageData current) {
      String name = String.valueOf(nextId());
      VersionInfo version = makeVersionInfo(current, name);
      versions.put(version.getName(), current);
      return version;
    }

    public Collection<VersionInfo> history() {
      Set<VersionInfo> set = new HashSet<VersionInfo>();
      for (Map.Entry<String, PageData> entry : versions.entrySet()) {
        set.add(makeVersionInfo(entry.getValue(), entry.getKey()));
      }
      return set;
    }

    public PageData getRevisionData(String versionName) {
      PageData version = versions.get(versionName);
      if (version == null)
        throw new NoSuchVersionException("There is no version '" + versionName + "'");

      return new PageData(version);
    }

    protected VersionInfo makeVersionInfo(PageData current, String name) {
      String author = current.getAttribute(PageData.LAST_MODIFYING_USER);
      if (author == null)
        author = "";
      Date date = current.getProperties().getLastModificationTime();
      return new VersionInfo(name, author, date);
    }

  }
}
