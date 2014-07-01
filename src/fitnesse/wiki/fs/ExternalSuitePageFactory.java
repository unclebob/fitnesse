package fitnesse.wiki.fs;

import java.io.File;

import fitnesse.wiki.BaseWikiPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageFactory;

public class ExternalSuitePageFactory implements WikiPageFactory<BaseWikiPage> {

  private final FileSystem fileSystem;

  public ExternalSuitePageFactory() {
    this(new DiskFileSystem());
  }

  public ExternalSuitePageFactory(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
  }

  @Override
  public WikiPage makeRootPage(String path, String pageName) {
    throw new RuntimeException("Can not make ExternalSuitePage root pages");
  }

  @Override
  public WikiPage makePage(File path, String pageName, BaseWikiPage parent) {
    return new ExternalSuitePage(path, pageName, parent, fileSystem);
  }

  @Override
  public boolean supports(File path) {
    if (path.getName().endsWith(".html")) return true;
    for (String child : fileSystem.list(path)) {
      if (hasHtmlChild(new File(path, child))) return true;
    }
    return false;
  }

  private boolean hasHtmlChild(File path) {
    if (path.getName().endsWith(".html")) return true;
    for (String child : fileSystem.list(path)) {
      if (hasHtmlChild(new File(path, child))) return true;
    }
    return false;
  }

}
