package fitnesse.wiki.fs;

import fitnesse.util.Clock;
import util.FileUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DiskFileSystem implements FileSystem {
  public void makeFile(File file, String content) throws IOException {
    FileUtil.createFile(file, content);
  }

  @Override
  public void makeFile(File file, InputStream content) throws IOException {
    FileUtil.createFile(file, content);
  }

  @Override
  public void makeDirectory(File path) throws IOException {
    if (!path.mkdirs()) {
      throw new IOException("make directory failed: " + path.getAbsolutePath());
    }
  }

  @Override
  public boolean exists(File file) {
    return file.exists();
  }

  @Override
  public String[] list(File path) {
    return path.isDirectory() ? path.list() : new String[]{};
  }

  @Override
  public String getContent(File file) throws IOException {
    return FileUtil.getFileContent(file);
  }

  @Override
  public InputStream getInputStream(File file) throws IOException {
    return new BufferedInputStream(new FileInputStream(file));
  }

  @Override
  public void delete(File fileToBeDeleted) {
    if (fileToBeDeleted.isDirectory()) {
      FileUtil.deleteFileSystemDirectory(fileToBeDeleted);
    } else {
      FileUtil.deleteFile(fileToBeDeleted);
    }
  }

  @Override
  public long lastModified(File file) {
    return file.exists() ? file.lastModified() : Clock.currentTimeInMillis();
  }

  @Override
  public void rename(File file, File originalFile) throws IOException {
    if (!originalFile.renameTo(file)) {
      throw new IOException("file rename failed: " + originalFile.getAbsolutePath());
    }
  }

  @Override
  public boolean isDirectory(File file) {
    return file.isDirectory();
  }
}
