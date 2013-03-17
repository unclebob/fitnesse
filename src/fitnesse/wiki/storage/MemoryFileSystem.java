package fitnesse.wiki.storage;

import java.util.ArrayList;
import java.util.Hashtable;

import util.Clock;

public class MemoryFileSystem implements FileSystem{
    private final Hashtable<String, String> files = new Hashtable<String, String>();

    public void makeFile(String path, String content) {
        files.put(path, content);
    }

    public void makeDirectory(String path) {
        files.put(path, "");
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

    public String getContent(String path) {
        return files.get(path);
    }

  @Override
  public void delete(String pathToDelete) {
    for (String f : files.keySet()) {
      if (f.startsWith(pathToDelete))
        files.remove(f);
    }
  }

  @Override
  public long lastModified(String path) {
    return Clock.currentTimeInMillis();
  }
}
