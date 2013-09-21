package fitnesse.wiki.fs;

import util.Clock;
import util.FileUtil;

import java.io.File;
import java.io.IOException;

public class DiskFileSystem implements FileSystem {
  public void makeFile(String path, String content) throws IOException {
    FileUtil.createFile(path, content);
  }

  public void makeDirectory(String path) throws IOException {
    if (!new File(path).mkdirs()) {
      throw new IOException("make directory failed: " + path);
    }
  }

  public boolean exists(String path) {
    return new File(path).exists();
  }

  public String[] list(String path) {
    File file = new File(path);
    return file.isDirectory() ? file.list() : new String[]{};
  }

  public String getContent(String path) throws IOException {
    return FileUtil.getFileContent(path);
  }

  @Override
  public void delete(String pathToDelete) {
    final File fileToBeDeleted = new File(pathToDelete);
    if (fileToBeDeleted.isDirectory()) {
      FileUtil.deleteFileSystemDirectory(fileToBeDeleted);
    } else {
      FileUtil.deleteFile(fileToBeDeleted);
    }
  }

  @Override
  public long lastModified(String path) {
    File file = new File(path);
    return file.exists() ? file.lastModified() : Clock.currentTimeInMillis();
  }
}
