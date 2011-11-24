package fitnesse.wiki;

import fitnesse.wikitext.parser.WikiWordPath;
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
      String path = parent.getFileSystemPath() + "/" + name;
      if (hasContentChild(path)) {
          return new FileSystemPage(name, parent, fileSystem);
      }
      else if (hasHtmlChild(path)) {
          return new ExternalSuitePage(path, name, parent, fileSystem);
      }
      else {
          return new FileSystemPage(name, parent, fileSystem);
      }
    }

    private Boolean hasContentChild(String path) {
        for (String child: fileSystem.list(path)) {
            if (child.equals("content.txt")) return true;
        }
        return false;
    }

    private Boolean hasHtmlChild(String path) {
        if (path.endsWith(".html")) return true;
        for (String child: fileSystem.list(path)) {
            if (hasHtmlChild(path + "/" + child)) return true;
        }
        return false;
    }

    public List<WikiPage> findChildren(ExternalSuitePage parent) throws Exception {
        List<WikiPage> children = new ArrayList<WikiPage>();
        for (String child: fileSystem.list(parent.getFileSystemPath())) {
            String childPath = parent.getFileSystemPath() + "/" + child;
            if (child.endsWith(".html")) {
                children.add(new ExternalTestPage(childPath,
                        WikiWordPath.makeWikiWord(child.replace(".html", "")), parent, fileSystem));
            }
            else if (hasHtmlChild(childPath)) {
                children.add(new ExternalSuitePage(childPath,
                        WikiWordPath.makeWikiWord(child), parent, fileSystem));
            }
        }
        return children;
    }
}
