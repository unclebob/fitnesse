package fitnesse.wiki.mem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

import fitnesse.wiki.fs.FileSystem;
import util.Clock;
import util.FileUtil;

public class MemoryFileSystem implements FileSystem {
    private final Hashtable<String, Payload> files = new Hashtable<String, Payload>();

    @Override
    public void makeFile(File file, String content) {
        files.put(file.getPath(), payload(content));
    }

    @Override
    public void makeFile(File file, InputStream content) throws IOException {
      ByteArrayOutputStream buf = new ByteArrayOutputStream();
      FileUtil.copyBytes(content, buf);
      makeFile(file, buf.toString("UTF-8"));
    }

    @Override
    public void makeDirectory(File path) {
        files.put(path.getPath(), payload(""));
    }

    @Override
    public boolean exists(File file) {
        String path = file.getPath();
        for (String filePath: files.keySet()) {
            if (filePath.startsWith(path)) return true;
        }
        return false;
    }

    @Override
    public String[] list(File file) {
        String path = file.getPath();
        ArrayList<String> result = new ArrayList<String>();
        for (String filePath: files.keySet()) {
            if (!filePath.startsWith(path)) continue;
            if (filePath.equals(path)) continue;
            String rest = filePath.substring(path.length() + 1);
            int size = rest.indexOf(File.separator);
            if (size < 0) size = rest.length();
            String newPath = rest.substring(0, size);
            if (!result.contains(newPath)) result.add(newPath);
        }
        return result.toArray(new String[result.size()]);
    }

    @Override
    public String getContent(File file) {
        return files.get(file.getPath()).payload;
    }

  @Override
  public InputStream getInputStream(File file) throws IOException {
    return new ByteArrayInputStream(files.get(file.getPath()).payload.getBytes("UTF-8"));
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
    throw new RuntimeException("FileSystem.rename() has not been implemented for Memory file system.");
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
