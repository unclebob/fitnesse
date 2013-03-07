package fitnesse.wiki;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import fitnesse.ComponentFactory;
import fitnesse.wiki.zip.ZipFileVersionsController;
import fitnesse.wikitext.parser.WikiWordPath;
import util.DiskFileSystem;
import util.FileSystem;

// TODO: Merge with WikiPageFactory
public class FileSystemPageFactory implements WikiPageFactory {
  private FileSystem fileSystem;
  private VersionsController versionsController;

  public FileSystemPageFactory() {
    fileSystem = new DiskFileSystem();
    versionsController = new ZipFileVersionsController();
  }

  public FileSystemPageFactory(Properties properties) {
    fileSystem = new DiskFileSystem();
    versionsController = (VersionsController) new ComponentFactory(properties).createComponent(
            ComponentFactory.VERSIONS_CONTROLLER, ZipFileVersionsController.class);
  }

  public FileSystemPageFactory(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
    this.versionsController = new ZipFileVersionsController();
  }

  @Override
  // RootPath should be a File()
  public WikiPage makeRootPage(String rootPath, String rootPageName) {
    return new FileSystemPage(rootPath, rootPageName, this, fileSystem, versionsController);
  }

  @Override
  public WikiPage makeChildPage(String name, FileSystemPage parent) {
    String path = parent.getFileSystemPath() + "/" + name;
    if (hasContentChild(path)) {
      return new FileSystemPage(name, parent, this, fileSystem);
    } else if (hasHtmlChild(path)) {
      return new ExternalSuitePage(path, name, parent, fileSystem);
    } else {
      return new FileSystemPage(name, parent, this, fileSystem);
    }
  }

  private Boolean hasContentChild(String path) {
    for (String child : fileSystem.list(path)) {
      if (child.equals("content.txt")) return true;
    }
    return false;
  }

  private Boolean hasHtmlChild(String path) {
    if (path.endsWith(".html")) return true;
    for (String child : fileSystem.list(path)) {
      if (hasHtmlChild(path + "/" + child)) return true;
    }
    return false;
  }

  public List<WikiPage> findChildren(ExternalSuitePage parent) {
    List<WikiPage> children = new ArrayList<WikiPage>();
    for (String child : fileSystem.list(parent.getFileSystemPath())) {
      String childPath = parent.getFileSystemPath() + "/" + child;
      if (child.endsWith(".html")) {
        children.add(new ExternalTestPage(childPath,
                WikiWordPath.makeWikiWord(child.replace(".html", "")), parent, fileSystem));
      } else if (hasHtmlChild(childPath)) {
        children.add(new ExternalSuitePage(childPath,
                WikiWordPath.makeWikiWord(child), parent, fileSystem));
      }
    }
    return children;
  }

  VersionsController getVersionsController() {
    return versionsController;
  }
}
