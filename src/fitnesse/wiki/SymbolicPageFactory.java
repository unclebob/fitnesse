package fitnesse.wiki;

import java.io.File;

import fitnesse.wiki.fs.DiskFileSystem;
import fitnesse.wiki.fs.FileSystem;
import fitnesse.wiki.fs.FileSystemPageFactory;
import util.EnvironmentVariableTool;

public class SymbolicPageFactory implements WikiPageFactory {

  private final WikiPageFactory wikiPageFactory;
  private final WikiPage page;

  public SymbolicPageFactory(WikiPage page) {
    this.page = page;
    FileSystem fileSystem = new DiskFileSystem();
    wikiPageFactory = new FileSystemPageFactory(fileSystem, new SimpleFileVersionsController(fileSystem));
  }

  public WikiPage makeRootPage(String linkPath, String linkName) {
    if (linkPath.startsWith("file://"))
      return createExternalSymbolicLink(linkPath, linkName);
    else
      return createInternalSymbolicPage(linkPath, linkName);
  }

  private WikiPage createExternalSymbolicLink(String linkPath, String linkName) {
    String fullPagePath = EnvironmentVariableTool.replace(linkPath.substring(7));
    File file = new File(fullPagePath);
    File parentDirectory = file.getParentFile();
    if (parentDirectory.exists()) {
      if (file.isDirectory()) {
        WikiPage externalRoot = wikiPageFactory.makeRootPage(parentDirectory.getPath(), file.getName());
        return new SymbolicPage(linkName, externalRoot, page);
      }
    }
    return null;
  }

  protected WikiPage createInternalSymbolicPage(String linkPath, String linkName) {
    WikiPagePath path = PathParser.parse(linkPath);
    WikiPage start = (path.isRelativePath()) ? page.getParent() : page;  //TODO -AcD- a better way?
    WikiPage wikiPage = page.getPageCrawler().getPage(start, path);
    if (wikiPage != null)
      wikiPage = new SymbolicPage(linkName, wikiPage, page);
    return wikiPage;
  }


}
