package fitnesse.wiki.fs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Logger;

import fitnesse.util.Cache;
import fitnesse.wiki.VersionInfo;

public class SimpleFileVersionsController implements VersionsController {
  private static final Logger LOG = Logger.getLogger(SimpleFileVersionsController.class.getName());

  private final FileSystem fileSystem;
  private final Cache<File, String, IOException> fileCache = new Cache.Builder<File, String, IOException>()
          .withLoader(new Cache.Loader<File, String, IOException>() {
            @Override
            public String fetch(File fileName) throws IOException {
              return fileSystem.exists(fileName) ? fileSystem.getContent(fileName) : null;
            }
          })
          .withExpirationPolicy(new Cache.ExpirationPolicy<File, String>() {
            @Override
            public boolean isExpired(File key, String value, long lastModified) {
              return !fileSystem.exists(key) || fileSystem.lastModified(key) > lastModified;
            }
          })
          .build();

  public SimpleFileVersionsController(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
  }

  @Override
  public void setHistoryDepth(int historyDepth) {
    // Just one file, no history
  }

  @Override
  public FileVersion[] getRevisionData(String label, File... files) {
    FileVersion[] versions = new FileVersion[files.length];
    int counter = 0;
    for (File file : files) {
      if (fileSystem.exists(file))
        versions[counter++] = new RevisionFileVersion(file, "");
    }
    return versions;
  }

  private class RevisionFileVersion implements FileVersion {

    private final File file;
    private final String author;

    private RevisionFileVersion(File file, String author) {
      this.file = file;
      this.author = author;
    }

    @Override
    public File getFile() {
      return file;
    }

    @Override
    public InputStream getContent() throws IOException {
      return new ByteArrayInputStream(fileCache.get(file).getBytes("UTF-8"));
    }

    @Override
    public String getAuthor() {
      return author;
    }

    @Override
    public Date getLastModificationTime() {
      return new Date(fileSystem.lastModified(file));
    }
  }

  @Override
  public Collection<VersionInfo> history(File... files) {
    return Collections.emptyList();
  }

  @Override
  public VersionInfo makeVersion(FileVersion... fileVersions) throws IOException {
    for (FileVersion fileVersion : fileVersions) {
      addDirectory(fileVersion.getFile().getParentFile());
      InputStream content = fileVersion.getContent();
      fileCache.expire(fileVersion.getFile());
      try {
        fileSystem.makeFile(fileVersion.getFile(), content);
      } finally {
        content.close();
      }
    }
    return VersionInfo.makeVersionInfo(fileVersions[0].getAuthor(), fileVersions[0].getLastModificationTime());
  }

  @Override
  public void delete(FileVersion... files) {
    for (FileVersion fileVersion : files) {
      fileCache.expire(fileVersion.getFile());
      fileSystem.delete(fileVersion.getFile());
    }
  }

  @Override
  public VersionInfo addDirectory(final FileVersion dir) throws IOException {
    final File filePath = dir.getFile();
    addDirectory(filePath);
    return VersionInfo.makeVersionInfo(dir.getAuthor(), new Date(fileSystem.lastModified(filePath)));
  }

  private void addDirectory(final File filePath) throws IOException {
    if (!fileSystem.exists(filePath)) {
      fileSystem.makeDirectory(filePath);
    }

  }
  @Override
  public void rename(FileVersion fileVersion, File oldFile) throws IOException {
    fileCache.expire(oldFile);
    fileSystem.rename(fileVersion.getFile(), oldFile);
  }
}
