package fitnesse.fixtures;

import java.util.Properties;

import fitnesse.ComponentFactory;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.fs.FileSystemPageFactory;
import util.FileUtil;

public class VersionsControllerFixture {
  private FileSystemPageFactory pageFactory;
  private WikiPage rootPage;
  private WikiPage lastUsedPage;

  public VersionsControllerFixture() {
  }

  public VersionsControllerFixture(String versionsControllerClassName) {
    Properties properties = new Properties();
    properties.setProperty(ComponentFactory.VERSIONS_CONTROLLER, versionsControllerClassName);

    pageFactory = new FileSystemPageFactory(properties);
  }

  public void createWikiRoot() {
    rootPage = pageFactory.makeRootPage("TestDir", "RooT");
  }

  public void cleanUp() {
    FileUtil.deleteFileSystemDirectory("TestDir");
  }

  public Object savePageWithContent(String pageName, String content) {
    final PageCrawler pageCrawler = rootPage.getPageCrawler();
    lastUsedPage = pageCrawler.addPage(rootPage, PathParser.parse(pageName));
    final PageData data = lastUsedPage.getData();
    data.setContent(content);
    return lastUsedPage.commit(data);
  }

  public void deletePage(String pageName) {
    final PageCrawler pageCrawler = rootPage.getPageCrawler();
    lastUsedPage = pageCrawler.getPage(rootPage, PathParser.parse(pageName));
    lastUsedPage.getParent().removeChildPage(lastUsedPage.getName());
  }

  public int historySize() {
    return lastUsedPage.getVersions().size();
  }
}
