package fitnesse.wiki.fs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import fitnesse.wiki.VersionInfo;
import util.FileUtil;

public class SimpleFileVersionsController implements VersionsController, FileVersionsController {

  private final FileSystem fileSystem;

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
      createDirectory(fileVersion.getFile().getParentFile());
      fileSystem.makeFile(fileVersion.getFile(), fileVersion.getContent());
    }
    return VersionInfo.makeVersionInfo(fileVersions[0].getAuthor(), fileVersions[0].getLastModificationTime());
  }

  @Override
  public void delete(File... files) {
    for (File file : files) {
      fileSystem.delete(file);
    }
  }

  private void createDirectory(final File filePath) throws IOException {
    if (!fileSystem.exists(filePath)) {
      fileSystem.makeDirectory(filePath);
    }
  }

  @Override
  public void addFile(FileVersion... fileVersions) throws IOException {
    for (FileVersion fileVersion : fileVersions) {
      InputStream input = null;
      OutputStream output = null;
      try {
        input = fileVersion.getContent();
        output = new BufferedOutputStream(new FileOutputStream(fileVersion.getFile()));
        FileUtil.copyBytes(input, output);
      } finally {
        if (input != null)
          input.close();
        if (output != null)
          output.close();
      }
    }
  }

  @Override
  public void delete(File file) {
    if (file.isDirectory())
      FileUtil.deleteFileSystemDirectory(file);
    else
      file.delete();
  }

  @Override
  public void addDirectory(File dir) {
    dir.mkdirs();
  }

  @Override
  public void renameFile(File file, File oldFile) {
    oldFile.renameTo(file);
  }
}
