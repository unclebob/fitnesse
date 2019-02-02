package fitnesse.wiki.fs;

import fitnesse.ConfigurationParameter;
import fitnesse.components.ComponentFactory;
import fitnesse.wiki.*;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import static fitnesse.ConfigurationParameter.VERSIONS_CONTROLLER_CLASS;
import static fitnesse.ConfigurationParameter.WIKI_PAGE_FACTORY_CLASS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class FileSystemPageFactoryTest {
  private FileSystem fileSystem;
  private FileSystemPageFactory fileSystemPageFactory;
  private WikiPage rootPage;

  @Before
  public void SetUp() throws Exception {
    fileSystem = new MemoryFileSystem();
    fileSystemPageFactory = new FileSystemPageFactory(fileSystem, new ZipFileVersionsController());
    fileSystem.makeFile(new File("./somepath/content.txt"), "");
    rootPage = fileSystemPageFactory.makePage(new File("./somepath"), "somepath", null, new SystemVariableSource());
  }

  @Test
  public void getVersionControllerFromComponentFactoryWhenCreatedByComponentFactory() {
    ComponentFactory c = new ComponentFactory(new Properties());

    VersionsController vc = c.createComponent(VERSIONS_CONTROLLER_CLASS, ZipFileVersionsController.class);
    FileSystemPageFactory f = c.createComponent(WIKI_PAGE_FACTORY_CLASS, FileSystemPageFactory.class);

    assertSame("Did not use the version controller instance present in component factory",
      vc, f.getVersionsController());
  }

  @Test
  public void DirectoryOfHtmlFilesIsExternalSuitePage() throws Exception {
    fileSystem.makeFile(new File("./somepath/ExternalSuite/myfile.html"), "stuff");
    WikiPage page = rootPage.addChildPage("ExternalSuite");
    assertEquals(ExternalSuitePage.class, page.getClass());
  }

  @Test
  public void DirectoryOfDirectoryOfHtmlFilesIsExternalSuitePage() throws Exception {
    fileSystem.makeFile(new File("./somepath/ExternalSuite/subsuite/myfile.html"), "stuff");
    WikiPage page = rootPage.addChildPage("ExternalSuite");
    assertEquals(ExternalSuitePage.class, page.getClass());
  }

  @Test
  public void DirectoryWithoutHtmlFilesIsWikiFilePage() throws Exception {
    fileSystem.makeFile(new File("./somepath/WikiPage/myfile.txt"), "stuff");
    fileSystem.makeFile(new File("./somepath/OtherPage/myfile.html"), "stuff");
    WikiPage page = rootPage.addChildPage("WikiPage");
    assertEquals(WikiFilePage.class, page.getClass());
  }

  @Test
  public void DirectoryWithContentIsFileSystemPage() throws Exception {
    fileSystem.makeFile(new File("./somepath/WikiPage/content.txt"), "stuff");
    fileSystem.makeFile(new File("./somepath/WikiPage/subsuite/myfile.html"), "stuff");
    WikiPage page = rootPage.addChildPage("WikiPage");
    assertEquals(FileSystemPage.class, page.getClass());
  }

  @Test
  public void FileWithWikiExtensionIsNewFileSystemPage() throws Exception {
    fileSystem.makeFile(new File("./somepath/WikiPage.wiki"), "stuff");
    fileSystem.makeFile(new File("./somepath/WikiPage/subsuite/myfile.html"), "stuff");
    WikiPage page = rootPage.addChildPage("WikiPage");
    assertEquals(WikiFilePage.class, page.getClass());
  }

  @Test
  public void NestedWikiFileIsNewFileSystemPage() throws Exception {
    fileSystem.makeFile(new File("./somepath/WikiPage/content.txt"), "stuff");
    fileSystem.makeFile(new File("./somepath/WikiPage/subsuite.wiki"), "stuff");
    fileSystem.makeFile(new File("./somepath/WikiPage/subsuite/myfile.html"), "stuff");
    WikiPage page = rootPage.getPageCrawler().getPage(PathParser.parse("WikiPage.subsuite"));
    assertEquals(WikiFilePage.class, page.getClass());
  }

  @Test
  public void NestedWikiFileInOldStyleDirectoryIsNewFileSystemPage() throws Exception {
    fileSystem.makeFile(new File("./somepath/WikiPage.wiki"), "stuff");
    fileSystem.makeFile(new File("./somepath/WikiPage/subsuite.wiki"), "stuff");
    fileSystem.makeFile(new File("./somepath/WikiPage/subsuite/myfile.html"), "stuff");
    WikiPage page = rootPage.getPageCrawler().getPage(PathParser.parse("WikiPage.subsuite"));
    assertEquals(WikiFilePage.class, page.getClass());
  }

  @Test
  public void OldStyleDirectoryInNestedWikiFileIsFileSystemPage() throws Exception {
    fileSystem.makeFile(new File("./somepath/WikiPage.wiki"), "stuff");
    fileSystem.makeFile(new File("./somepath/WikiPage/SubPage/content.txt"), "stuff");
    fileSystem.makeFile(new File("./somepath/WikiPage/SubPage/subsuite/myfile.html"), "stuff");
    WikiPage page = rootPage.getPageCrawler().getPage(PathParser.parse("WikiPage.SubPage"));
    assertEquals(FileSystemPage.class, page.getClass());
  }

  @Test
  public void HtmlFileIsExternalSuitePageChild() throws Exception {
    fileSystem.makeFile(new File("./somepath/ExternalSuite/myfile.html"), "stuff");
    ExternalSuitePage page = (ExternalSuitePage) rootPage.addChildPage("ExternalSuite");
    WikiPage child = page.getChildren().get(0);
    assertEquals(ExternalTestPage.class, child.getClass());
    assertEquals("myfile", child.getName());
  }

  @Test
  public void DirectoryOfHtmlFilesIsExternalSuitePageChild() throws Exception {
    fileSystem.makeFile(new File("./somepath/ExternalSuite/subsuite/myfile.html"), "stuff");
    ExternalSuitePage page = (ExternalSuitePage) rootPage.addChildPage("ExternalSuite");
    WikiPage child = page.getChildren().get(0);
    assertEquals(ExternalSuitePage.class, child.getClass());
    assertEquals("subsuite", child.getName());
  }

  @Test
  public void testShouldUseZipFileRevisionControllerAsDefault() throws Exception {
    VersionsController defaultRevisionController = fileSystemPageFactory.getVersionsController();
    assertEquals(ZipFileVersionsController.class, defaultRevisionController.getClass());
  }

  @Test
  public void testShouldUseSpecifiedRevisionController() throws Exception {
    Properties testProperties = new Properties();
    testProperties.setProperty(ConfigurationParameter.VERSIONS_CONTROLLER_CLASS.getKey(), NullVersionsController.class.getName());
    fileSystemPageFactory = new FileSystemPageFactory(testProperties);

    VersionsController defaultRevisionController = fileSystemPageFactory.getVersionsController();
    assertEquals(NullVersionsController.class, defaultRevisionController.getClass());
  }

  @Test
  public void testShouldUseSpecifiedRevisionControllerWithHistoryDepth() throws Exception {
    Properties testProperties = new Properties();
    testProperties.setProperty(ConfigurationParameter.VERSIONS_CONTROLLER_CLASS.getKey(), NullVersionsController.class.getName());
    testProperties.setProperty(ConfigurationParameter.VERSIONS_CONTROLLER_DAYS.getKey(), "42");
    fileSystemPageFactory = new FileSystemPageFactory(testProperties);

    VersionsController defaultRevisionController = fileSystemPageFactory.getVersionsController();
    assertEquals(NullVersionsController.class, defaultRevisionController.getClass());
    assertEquals(42, ((NullVersionsController) defaultRevisionController).getHistoryDepth());
  }

  public static class NullVersionsController implements VersionsController {

    private final int historyDepth;

    public NullVersionsController(Properties properties) {
      historyDepth = Integer.valueOf(properties.getProperty(ConfigurationParameter.VERSIONS_CONTROLLER_DAYS.getKey(), "0"));
    }

    public int getHistoryDepth() {
      return historyDepth;
    }

    @Override
    public FileVersion[] getRevisionData(final String label, final File... files) {
      return null;
    }

    @Override
    public Collection<VersionInfo> history(final File... files) {
      return new HashSet<>();
    }

    @Override
    public VersionInfo makeVersion(final FileVersion... fileVersions) {
      return null;
    }

    @Override
    public VersionInfo addDirectory(FileVersion filePath) {
      return null;
    }

    @Override
    public void rename(FileVersion fileVersion, File originalFile) {
    }

    @Override
    public void delete(File... files) {
    }
  }

}
