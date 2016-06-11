package fitnesse.wiki.fs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import fitnesse.wiki.VersionInfo;

public class SimpleFileVersionsController implements VersionsController {

  private final FileSystem fileSystem;

  public SimpleFileVersionsController(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
  }

  public SimpleFileVersionsController() {
    this(new DiskFileSystem());
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
      return new BufferedInputStream(fileSystem.getInputStream(file));
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
      try (InputStream content = fileVersion.getContent()) {
        fileSystem.makeFile(fileVersion.getFile(), content);
      }
    }
    return VersionInfo.makeVersionInfo(fileVersions[0].getAuthor(), fileVersions[0].getLastModificationTime());
  }

  @Override
  public void delete(File... files) throws IOException {
    for (File file : files) {
      fileSystem.delete(file);
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
    fileSystem.rename(fileVersion.getFile(), oldFile);
  }
}
