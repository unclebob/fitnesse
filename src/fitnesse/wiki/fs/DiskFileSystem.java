package fitnesse.wiki.fs;

import fitnesse.util.Clock;
import util.FileUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DiskFileSystem implements FileSystem {
  private static final Set<String> SKIPPED_FILE_NAMES = new HashSet<>(Arrays.asList("CVS", "RCS"));

  @Override
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
  public String[] list(File dir) {
    File[] files = FileUtil.listFiles(dir, path ->
      !Files.isHidden(path) && !SKIPPED_FILE_NAMES.contains(path.getFileName().toString()));
    List<String> fileList = new ArrayList<>(files.length);
    for (File f : files) {
      fileList.add(f.getName());
    }
    return fileList.toArray(new String[0]);
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
  public void delete(File fileToBeDeleted) throws IOException {
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
