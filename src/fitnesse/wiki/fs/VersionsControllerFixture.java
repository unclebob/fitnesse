package fitnesse.wiki.fs;

import fitnesse.ConfigurationParameter;
import fitnesse.wiki.*;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import util.FileUtil;

import java.io.File;
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
    rootPage = pageFactory.makeRootPage(TEST_DIR, "RooT");
  }

  public WikiPage getRootPage() {
    return rootPage;
  }

  public void cleanUp() {
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
    lastUsedPage.getParent().removeChildPage(lastUsedPage.getName());
  }

  public int historySize() {
    Collection<VersionInfo> versions = lastUsedPage.getVersions();
    return versions.size();
  }

  public String contentForRevision(int n) {
    List<VersionInfo> versions = new ArrayList<VersionInfo>(lastUsedPage.getVersions());
    PageData data = lastUsedPage.getDataVersion(versions.get(versions.size() - 1 - n).getName());
    return data.getContent();
  }

  public boolean initialiseGitRepository() throws GitAPIException {
    FileUtil.createDir(TEST_DIR);
    new InitCommand()
            .setDirectory(new File(TEST_DIR))
            .setBare(false)
            .call();
    return true;
  }
}
