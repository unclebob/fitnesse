package fitnesse.wiki.fs;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import fitnesse.wiki.NoSuchVersionException;
import fitnesse.wiki.VersionInfo;

public class MemoryVersionsController implements VersionsController {


  private Map<String, FileVersions> versions = new HashMap<>();

  private VersionsController persistence;

  public MemoryVersionsController(FileSystem fileSystem) {
    this.persistence = new SimpleFileVersionsController(fileSystem);
  }

  @Override
  public FileVersion[] getRevisionData(String label, File... files) throws IOException {
    if (label == null) {
      return persistence.getRevisionData(null, files);
    }
    return getFileVersions(files[0]).getRevisionData(label);
  }

  @Override
  public Collection<VersionInfo> history(File... files) {
    FileVersions fileVersions = getFileVersions(files[0]);
    if (fileVersions == null) return null;
    return fileVersions.history();
  }

  @Override
  public VersionInfo makeVersion(FileVersion... fileVersions) throws IOException {
    // For FilePageFactory, it does some lookups in the file system.
    persistence.makeVersion(fileVersions);
    return getFileVersions(fileVersions[0].getFile()).makeVersion(fileVersions);
  }

  @Override
  public VersionInfo addDirectory(FileVersion filePath) throws IOException {
    return persistence.addDirectory(filePath);
  }

  @Override
  public void rename(FileVersion fileVersion, File originalFile) throws IOException {
    persistence.rename(fileVersion, originalFile);
  }

  @Override
  public void delete(File... files) throws IOException {
    persistence.delete(files);
  }

  private FileVersions getFileVersions(File file) {
    String key = file.getPath();
    FileVersions fileVersions = versions.get(key);
    if (fileVersions == null) {
      fileVersions = new FileVersions();
      versions.put(key, fileVersions);
    }
    return fileVersions;
  }

  private static class FileVersions {
    protected Map<String, FileVersion[]> versions = new TreeMap<>();

    protected VersionInfo makeVersion(FileVersion... current) {
      VersionInfo version = makeVersionInfo(current[0]);
      versions.put(version.getName(), current);
      return version;
    }

    private VersionInfo makeVersionInfo(FileVersion current) {
      String name = String.valueOf(versions.size());
      return makeVersionInfo(current, name);
    }

    public Collection<VersionInfo> history() {
      Collection<VersionInfo> set = new LinkedList<>();
      for (Map.Entry<String, FileVersion[]> entry : versions.entrySet()) {
        set.add(makeVersionInfo(entry.getValue()[0], entry.getKey()));
      }
      return set;
    }

    public FileVersion[] getRevisionData(String versionName) {
      FileVersion[] version = versions.get(versionName);
      if (version == null)
        throw new NoSuchVersionException("There is no version '" + versionName + "'");

      return version;
    }

    protected VersionInfo makeVersionInfo(FileVersion current, String name) {
      String author = current.getAuthor();
      if (author == null)
        author = "";
      Date date = current.getLastModificationTime();
      return new VersionInfo(name, author, date);
    }

  }
}
