package fitnesse.wiki.fs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import fitnesse.util.Clock;
import fitnesse.util.StringUtils;
import util.FileUtil;

public class MemoryFileSystem implements FileSystem {
  public static final String DIRECTORY_PLACEHOLDER = "*This is a directory*";
  private final Map<String, Payload> files = new LinkedHashMap<>();

  @Override
  public void makeFile(File file, String content) {
      files.put(file.getPath(), payload(content));
  }

  @Override
  public void makeFile(File file, InputStream content) throws IOException {
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    FileUtil.copyBytes(content, buf);
    makeFile(file, buf.toString(FileUtil.CHARENCODING));
  }

  @Override
  public void makeDirectory(File path) {
    files.put(path.getPath(), payload(DIRECTORY_PLACEHOLDER));
    if (path.getParentFile() != null && files.containsKey(path.getParent())) {
      makeDirectory(path.getParentFile());
    }
  }

  @Override
  public boolean exists(File file) {
    return getPayload(file) != null;
  }

  public Payload getPayload(File file) {
    String path = file.getPath();
    for (String filePath: files.keySet()) {
      if (filePath.equals(path)) return files.get(filePath);
      // Part is matching, assume a directory.
      if (filePath.startsWith(path)) return new Payload(DIRECTORY_PLACEHOLDER);
    }
    return null;
  }

  @Override
  public String[] list(File file) {
    String path = file.getPath();
    Collection<String> result = new ArrayList<>();
    for (String filePath: files.keySet()) {
      if (!filePath.startsWith(path)) continue;
      if (filePath.equals(path)) continue;
      String rest = filePath.substring(path.length() + 1);
      int size = rest.indexOf(File.separator);
      if (size < 0) size = rest.length();
      String newPath = rest.substring(0, size);
      if (!StringUtils.isBlank(newPath) && !result.contains(newPath)) result.add(newPath);
    }
    return result.toArray(new String[result.size()]);
  }

  @Override
  public String getContent(File file) {
      return files.get(file.getPath()).payload;
  }

  @Override
  public InputStream getInputStream(File file) throws IOException {
    return new ByteArrayInputStream(files.get(file.getPath()).payload.getBytes(FileUtil.CHARENCODING));
  }

  @Override
  public void delete(File fileToDelete) {
    String pathToDelete = fileToDelete.getPath();
    for (Iterator<String> iter = files.keySet().iterator(); iter.hasNext(); ) {
      String f = iter.next();
      if (f.startsWith(pathToDelete))
        iter.remove();
    }
  }

  @Override
  public long lastModified(File file) {
    Payload payload = files.get(file.getPath());
    return payload != null ? payload.lastModified : Clock.currentTimeInMillis();
  }

  @Override
  public void rename(File file, File originalFile) {
    throw new IllegalStateException("FileSystem.rename() has not been implemented for Memory file system.");
  }

  @Override
  public boolean isDirectory(File file) {
    Payload payload = getPayload(file);
    return (payload != null && DIRECTORY_PLACEHOLDER.equals(payload.payload));
  }

  private Payload payload(String payload) {
    return new Payload(payload);
  }

  private static class Payload {
    private final String payload;
    private final long lastModified;

    private Payload(String payload) {
      this.payload = payload;
      this.lastModified = new Date().getTime();
    }
  }
}
