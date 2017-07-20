package fitnesse.wiki.fs;

import fitnesse.ConfigurationParameter;
import fitnesse.wiki.*;
import util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class VersionsControllerFixture {
  public static final String TEST_DIR = "TestDir";

  private FileSystemPageFactory pageFactory;
  private WikiPage rootPage;
  private WikiPage lastUsedPage;

  public VersionsControllerFixture() {
  }

  public VersionsControllerFixture(String versionsControllerClassName) {
    Properties properties = new Properties();
    properties.setProperty(ConfigurationParameter.VERSIONS_CONTROLLER_CLASS.getKey(), versionsControllerClassName);

    pageFactory = new FileSystemPageFactory(properties);
  }

  public void createWikiRoot() {
    rootPage = pageFactory.makePage(new File(TEST_DIR, "RooT"), "RooT", null, new SystemVariableSource());
  }

  public WikiPage getRootPage() {
    return rootPage;
  }

  public void cleanUp() throws IOException {
    FileUtil.deleteFileSystemDirectory(TEST_DIR);
  }

  public Object savePageWithContent(String pageName, String content) {
    lastUsedPage = WikiPageUtil.addPage(rootPage, PathParser.parse(pageName));
    final PageData data = lastUsedPage.getData();
    data.setContent(content);
    return lastUsedPage.commit(data);
  }

  public void deletePage(String pageName) {
    final PageCrawler pageCrawler = rootPage.getPageCrawler();
    lastUsedPage = pageCrawler.getPage(PathParser.parse(pageName));
    lastUsedPage.remove();
  }

  public int historySize() {
    Collection<VersionInfo> versions = lastUsedPage.getVersions();
    return versions.size();
  }

  public String getVersionInfos() {
    String result = "";
    Collection<VersionInfo> versions = lastUsedPage.getVersions();
    for (VersionInfo version : versions){
      result = result  + version.getName() +"-" + version.getAuthor() + "-" + version.getCreationTime() + "\n";
    }
    return result;
  }

  public String contentForRevision(int n) {
    List<VersionInfo> versions = new ArrayList<>(lastUsedPage.getVersions());
    WikiPage page = lastUsedPage.getVersion(versions.get(versions.size() - 1 - n).getName());
    return page.getData().getContent();
  }

  public String contentForRevisionFromPage(int n, String pageName) {
    final PageCrawler pageCrawler = rootPage.getPageCrawler();
    lastUsedPage = pageCrawler.getPage(PathParser.parse(pageName));
    if (lastUsedPage == null) return "[Error: Page doesn't exists]";
    else return contentForRevision(n);
  }

  public String contentFromPage(String pageName) {
    final PageCrawler pageCrawler = rootPage.getPageCrawler();
    lastUsedPage = pageCrawler.getPage(PathParser.parse(pageName));
    if (lastUsedPage == null) return "[Error: Page doesn't exists]";
    else return lastUsedPage.getData().getContent();
  }
}
