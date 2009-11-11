package util;

import java.io.File;
import java.io.IOException;

public class DiskFileSystem implements FileSystem{
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

    public String getContent(String path) throws Exception {
        return FileUtil.getFileContent(path);
    }
}
