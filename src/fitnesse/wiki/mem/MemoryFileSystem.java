package fitnesse.wiki.mem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

import fitnesse.wiki.fs.FileSystem;
import util.Clock;
import util.FileUtil;

public class MemoryFileSystem implements FileSystem {
    private final Hashtable<String, Payload> files = new Hashtable<String, Payload>();

    public void makeFile(String path, String content) {
        files.put(path, payload(content));
    }

    @Override
    public void makeFile(String path, InputStream content) throws IOException {
      ByteArrayOutputStream buf = new ByteArrayOutputStream();
      FileUtil.copyBytes(content, buf);
      makeFile(path, buf.toString());
    }

    public void makeDirectory(String path) {
        files.put(path, payload(""));
    }

    public boolean exists(String path) {
        for (String filePath: files.keySet()) {
            if (filePath.startsWith(path)) return true;
        }
        return false;
    }

    public String[] list(String path) {
        ArrayList<String> result = new ArrayList<String>();
        for (String filePath: files.keySet()) {
            if (!filePath.startsWith(path)) continue;
            if (filePath.equals(path)) continue;
            String rest = filePath.substring(path.length() + 1);
            int size = rest.indexOf("/");
            if (size < 0) size = rest.length();
            String newPath = rest.substring(0, size);
            if (!result.contains(newPath)) result.add(newPath);
        }
        return result.toArray(new String[result.size()]);
    }

    @Override
    public String getContent(String path) {
        return files.get(path).payload;
    }

  @Override
  public InputStream getInputStream(String path) throws IOException {
    return new ByteArrayInputStream(files.get(path).payload.getBytes());
  }

  @Override
  public void delete(String pathToDelete) {
    for (Iterator<String> iter = files.keySet().iterator(); iter.hasNext(); ) {
      String f = iter.next();
      if (f.startsWith(pathToDelete))
        iter.remove();
    }
  }

  @Override
  public long lastModified(String path) {
    return files.get(path).lastModified;
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
