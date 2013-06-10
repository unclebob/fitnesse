package fitnesse.wiki.fs;

import java.io.File;

import fitnesse.wiki.*;
import fitnesse.wiki.fs.DiskFileSystem;
import fitnesse.wiki.fs.FileSystem;
import fitnesse.wiki.fs.FileSystemPageFactory;
import fitnesse.wiki.fs.SimpleFileVersionsController;
import util.EnvironmentVariableTool;

public class SymbolicPageFactory {

  private final WikiPageFactory wikiPageFactory;

  public SymbolicPageFactory() {
    this(new DiskFileSystem());
  }

  public SymbolicPageFactory(FileSystem fileSystem) {
    // FixMe: -AJM- this is a cyclic dependency: FileSystemPageFactory - FileSystemPage - SymbolicPageFactory
    wikiPageFactory = new FileSystemPageFactory(fileSystem, new SimpleFileVersionsController(fileSystem));
  }

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
        WikiPage externalRoot = wikiPageFactory.makeRootPage(parentDirectory.getPath(), file.getName());
        return new SymbolicPage(linkName, externalRoot, parent, this);
      }
    }
    return null;
  }

  protected WikiPage createInternalSymbolicPage(String linkPath, String linkName, WikiPage parent) {
    WikiPagePath path = PathParser.parse(linkPath);
    WikiPage start = (path.isRelativePath()) ? parent.getParent() : parent;  //TODO -AcD- a better way?
    WikiPage wikiPage = parent.getPageCrawler().getPage(start, path);
    if (wikiPage != null)
      wikiPage = new SymbolicPage(linkName, wikiPage, parent, this);
    return wikiPage;
  }


}
