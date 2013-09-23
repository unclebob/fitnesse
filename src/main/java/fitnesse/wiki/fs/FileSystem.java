package fitnesse.wiki.fs;

import java.io.IOException;

public interface FileSystem {
    void makeFile(String path, String content) throws IOException;
    void makeDirectory(String path) throws IOException;
    boolean exists(String path);
    String[] list(String path);
    String getContent(String path) throws IOException;

    void delete(String path);

    long lastModified(String path);
}
