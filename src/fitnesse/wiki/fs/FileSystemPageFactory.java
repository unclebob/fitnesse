package fitnesse.wiki.fs;

import fitnesse.components.ComponentFactory;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.SymbolicPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wiki.WikiPagePath;
import util.EnvironmentVariableTool;

import java.io.File;
import java.util.Properties;

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
            ComponentFactory.VERSIONS_CONTROLLER_CLASS, ZipFileVersionsController.class);
    versionsController.setHistoryDepth(Integer.parseInt(properties.getProperty(ComponentFactory.VERSIONS_CONTROLLER_DAYS, "14")));
  }

  public FileSystemPageFactory(FileSystem fileSystem, VersionsController versionsController) {
    this.fileSystem = fileSystem;
    this.versionsController = versionsController;
  }

  @Override
  // TODO: RootPath should be a File()?
  public FileSystemPage makeRootPage(String rootPath, String rootPageName) {
    return new FileSystemPage(rootPath, rootPageName, fileSystem, versionsController, new FileSystemSymbolicPageFactory());
  }

  VersionsController getVersionsController() {
    return versionsController;
  }

  protected class FileSystemSymbolicPageFactory implements SymbolicPageFactory {

    public WikiPage makePage(String linkPath, String linkName, WikiPage parent) {
      if (linkPath.startsWith("file://"))
        return createExternalSymbolicLink(linkPath, linkName, parent);
      else
        return createInternalSymbolicPage(linkPath, linkName, parent);
    }

    private WikiPage createExternalSymbolicLink(String linkPath, String linkName, WikiPage parent) {
      String fullPagePath = EnvironmentVariableTool.replace(linkPath.substring(7));
      File file = new File(fullPagePath);
      File parentDirectory = file.getParentFile();
      if (parentDirectory.exists()) {
        if (file.isDirectory()) {
          WikiPage externalRoot = FileSystemPageFactory.this.makeRootPage(parentDirectory.getPath(), file.getName());
          return new SymbolicPage(linkName, externalRoot, parent);
        }
      }
      return null;
    }

    protected WikiPage createInternalSymbolicPage(String linkPath, String linkName, WikiPage parent) {
      WikiPagePath path = PathParser.parse(linkPath);
      WikiPage start = (path.isRelativePath()) ? parent.getParent() : parent;  //TODO -AcD- a better way?
      WikiPage wikiPage = start.getPageCrawler().getPage(path);
      if (wikiPage != null)
        wikiPage = new SymbolicPage(linkName, wikiPage, parent);
      return wikiPage;
    }


  }

}
