package fitnesse.wiki;

import util.DiskFileSystem;
import util.FileSystem;

import java.util.ArrayList;
import java.util.List;

public class PageRepository {
    private FileSystem fileSystem;

    public PageRepository() {
        fileSystem = new DiskFileSystem();
    }

    public PageRepository(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    public WikiPage makeChildPage(String name, FileSystemPage parent) throws Exception {
      if (hasHtmlChild(parent.getFileSystemPath() + "/" + name)) {
          return new ExternalSuitePage(parent.getFileSystemPath() + "/" + name, name, parent, fileSystem);
      }
      else {
          return new FileSystemPage(name, parent, fileSystem);
      }
    }

    private Boolean hasHtmlChild(String path) {
        if (path.endsWith(".html")) return true;
        for (String child: fileSystem.list(path)) {
            if (hasHtmlChild(child)) return true;
        }
        return false;
    }

    public List<WikiPage> findChildren(ExternalSuitePage parent) throws Exception {
        List<WikiPage> children = new ArrayList<WikiPage>();
        for (String child: fileSystem.list(parent.getFileSystemPath())) {
            if (child.endsWith(".html")) {
                children.add(new ExternalTestPage(parent.getFileSystemPath() + "/" + child, child.replace(".html", ""), parent, fileSystem));
            }
        }
        return children;
    }
}
