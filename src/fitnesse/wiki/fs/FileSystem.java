package fitnesse.wiki.fs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface FileSystem {
  void makeFile(File file, String content) throws IOException;

  void makeFile(File file, InputStream content) throws IOException;

  void makeDirectory(File path) throws IOException;

  boolean exists(File file);

  String[] list(File path);

  String getContent(File file) throws IOException;

  InputStream getInputStream(File file) throws IOException;

  void delete(File path) throws IOException;

  long lastModified(File file);

  void rename(File file, File originalFile) throws IOException;

  boolean isDirectory(File file);
}
