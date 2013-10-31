package fitnesse.wiki.fs;

import fitnesse.components.ComponentFactory;
import fitnesse.wiki.SystemVariableSource;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.mem.MemoryFileSystem;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class FileSystemPageFactoryTest {
    private FileSystem fileSystem;
    private FileSystemPageFactory fileSystemPageFactory;
    private FileSystemPage rootPage;

    @Before
    public void SetUp() throws Exception {
        fileSystem = new MemoryFileSystem();
        fileSystemPageFactory = new FileSystemPageFactory(fileSystem, new ZipFileVersionsController(), new SystemVariableSource());
        rootPage = fileSystemPageFactory.makeRootPage(".", "somepath") ;
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
    public void DirectoryWithoutHtmlFilesIsFileSystemPage() throws Exception {
        fileSystem.makeFile(new File("./somepath/WikiPage/myfile.txt"), "stuff");
        fileSystem.makeFile(new File("./somepath/OtherPage/myfile.html"), "stuff");
        WikiPage page = rootPage.addChildPage("WikiPage");
        assertEquals(FileSystemPage.class, page.getClass());
    }

    @Test
    public void DirectoryWithContentIsFileSystemPage() throws Exception {
        fileSystem.makeFile(new File("./somepath/WikiPage/content.txt"), "stuff");
        fileSystem.makeFile(new File("./somepath/WikiPage/subsuite/myfile.html"), "stuff");
        WikiPage page = rootPage.addChildPage("WikiPage");
        assertEquals(FileSystemPage.class, page.getClass());
    }

    @Test
    public void HtmlFileIsExternalSuitePageChild() throws Exception {
        fileSystem.makeFile(new File("./somepath/ExternalSuite/myfile.html"), "stuff");
        ExternalSuitePage page = (ExternalSuitePage) rootPage.addChildPage("ExternalSuite");
        WikiPage child = page.getNormalChildren().get(0);
        assertEquals(ExternalTestPage.class, child.getClass());
        assertEquals("MyfilE", child.getName());
    }

    @Test
    public void DirectoryOfHtmlFilesIsExternalSuitePageChild() throws Exception {
        fileSystem.makeFile(new File("./somepath/ExternalSuite/subsuite/myfile.html"), "stuff");
        ExternalSuitePage page = (ExternalSuitePage) rootPage.addChildPage("ExternalSuite");
        WikiPage child = page.getNormalChildren().get(0);
        assertEquals(ExternalSuitePage.class, child.getClass());
        assertEquals("SubsuitE", child.getName());
    }

  @Test
  public void testShouldUseZipFileRevisionControllerAsDefault() throws Exception {
    VersionsController defaultRevisionController = fileSystemPageFactory.getVersionsController();
    assertEquals(ZipFileVersionsController.class, defaultRevisionController.getClass());
  }

  @Test
  public void testShouldUseSpecifiedRevisionController() throws Exception {
    Properties testProperties = new Properties();
    testProperties.setProperty(ComponentFactory.VERSIONS_CONTROLLER_CLASS, NullVersionsController.class.getName());
    fileSystemPageFactory = new FileSystemPageFactory(testProperties);

    VersionsController defaultRevisionController = fileSystemPageFactory.getVersionsController();
    assertEquals(NullVersionsController.class, defaultRevisionController.getClass());
    assertEquals(14, ((NullVersionsController) defaultRevisionController).getHistoryDepth());
  }

  @Test
  public void testShouldUseSpecifiedRevisionControllerWithHistoryDepth() throws Exception {
    Properties testProperties = new Properties();
    testProperties.setProperty(ComponentFactory.VERSIONS_CONTROLLER_CLASS, NullVersionsController.class.getName());
    testProperties.setProperty(ComponentFactory.VERSIONS_CONTROLLER_DAYS, "42");
    fileSystemPageFactory = new FileSystemPageFactory(testProperties);

    VersionsController defaultRevisionController = fileSystemPageFactory.getVersionsController();
    assertEquals(NullVersionsController.class, defaultRevisionController.getClass());
    assertEquals(42, ((NullVersionsController) defaultRevisionController).getHistoryDepth());
  }

  public static class NullVersionsController implements VersionsController {
    private int historyDepth;

    public NullVersionsController() {
    }

    @Override
    public void setHistoryDepth(int historyDepth) {
      this.historyDepth = historyDepth;
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
      return new HashSet<VersionInfo>();
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
    public void delete(FileVersion... files) {
    }
  }

}
