package fitnesse.wiki.mem;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import fitnesse.wiki.NoSuchVersionException;
import fitnesse.wiki.PageData;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.fs.FileSystem;
import fitnesse.wiki.fs.FileSystemPage;
import fitnesse.wiki.fs.SimpleFileVersionsController;
import fitnesse.wiki.fs.VersionsController;

public class MemoryVersionsController implements VersionsController {


  private Map<String, FileVersions> versions = new HashMap<String, FileVersions>();

  private VersionsController persistence;

  MemoryVersionsController(FileSystem fileSystem) {
    this.persistence = new SimpleFileVersionsController(fileSystem);
  }

  @Override
  public void setHistoryDepth(int historyDepth) {
  }

  @Override
  public PageData getRevisionData(FileSystemPage page, String label) {
    if (label == null) {
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
    // For FilePageFactory, it does some lookups in the file system.
    persistence.makeVersion(page, data);
    return fileVersions.makeVersion(data);
  }

  @Override
  public VersionInfo getCurrentVersion(FileSystemPage page) {
    return persistence.getCurrentVersion(page);
  }

  @Override
  public void delete(FileSystemPage page) {
    persistence.delete(page);
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
    protected Map<String, PageData> versions = new TreeMap<String, PageData>();

    protected VersionInfo makeVersion(PageData current) {
      VersionInfo version = makeVersionInfo(current);
      versions.put(version.getName(), new PageData(current));
      return version;
    }

    private VersionInfo makeVersionInfo(PageData current) {
      String name = String.valueOf(versions.size());
      return makeVersionInfo(current, name);
    }

    public Collection<VersionInfo> history() {
      LinkedList<VersionInfo> set = new LinkedList<VersionInfo>();
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
