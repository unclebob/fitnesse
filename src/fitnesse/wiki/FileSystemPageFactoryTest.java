package fitnesse.wiki;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import fitnesse.ComponentFactory;
import fitnesse.wiki.zip.ZipFileVersionsController;
import org.junit.Before;
import org.junit.Test;
import util.FileSystem;
import util.MemoryFileSystem;

public class FileSystemPageFactoryTest {
    private FileSystem fileSystem;
    private FileSystemPageFactory fileSystemPageFactory;
    private FileSystemPage rootPage;

    @Before
    public void SetUp() throws Exception {
        fileSystem = new MemoryFileSystem();
        fileSystemPageFactory = new FileSystemPageFactory(fileSystem);
        rootPage = (FileSystemPage) new FileSystemPageFactory(fileSystem).makeRootPage(".", "somepath") ;
    }

    @Test
    public void DirectoryOfHtmlFilesIsExternalSuitePage() throws Exception {
        fileSystem.makeFile("./somepath/ExternalSuite/myfile.html", "stuff");
        WikiPage page = fileSystemPageFactory.makeChildPage("ExternalSuite", rootPage);
        assertEquals(ExternalSuitePage.class, page.getClass());
    }

    @Test
    public void DirectoryOfDirectoryOfHtmlFilesIsExternalSuitePage() throws Exception {
        fileSystem.makeFile("./somepath/ExternalSuite/subsuite/myfile.html", "stuff");
        WikiPage page = fileSystemPageFactory.makeChildPage("ExternalSuite", rootPage);
        assertEquals(ExternalSuitePage.class, page.getClass());
    }

    @Test
    public void DirectoryWithoutHtmlFilesIsFileSystemPage() throws Exception {
        fileSystem.makeFile("./somepath/WikiPage/myfile.txt", "stuff");
        fileSystem.makeFile("./somepath/OtherPage/myfile.html", "stuff");
        WikiPage page = fileSystemPageFactory.makeChildPage("WikiPage", rootPage);
        assertEquals(FileSystemPage.class, page.getClass());
    }

    @Test
    public void DirectoryWithContentIsFileSystemPage() throws Exception {
        fileSystem.makeFile("./somepath/WikiPage/content.txt", "stuff");
        fileSystem.makeFile("./somepath/WikiPage/subsuite/myfile.html", "stuff");
        WikiPage page = fileSystemPageFactory.makeChildPage("WikiPage", rootPage);
        assertEquals(FileSystemPage.class, page.getClass());
    }

    @Test
    public void HtmlFileIsExternalSuitePageChild() throws Exception {
        fileSystem.makeFile("./somepath/ExternalSuite/myfile.html", "stuff");
        ExternalSuitePage page = (ExternalSuitePage) fileSystemPageFactory.makeChildPage("ExternalSuite", rootPage);
        WikiPage child = fileSystemPageFactory.findChildren(page).get(0);
        assertEquals(ExternalTestPage.class, child.getClass());
        assertEquals("MyfilE", child.getName());
    }

    @Test
    public void DirectoryOfHtmlFilesIsExternalSuitePageChild() throws Exception {
        fileSystem.makeFile("./somepath/ExternalSuite/subsuite/myfile.html", "stuff");
        ExternalSuitePage page = (ExternalSuitePage) fileSystemPageFactory.makeChildPage("ExternalSuite", rootPage);
        WikiPage child = fileSystemPageFactory.findChildren(page).get(0);
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
    testProperties.setProperty(ComponentFactory.VERSIONS_CONTROLLER, NullVersionsController.class.getName());
    fileSystemPageFactory = new FileSystemPageFactory(testProperties);

    VersionsController defaultRevisionController = fileSystemPageFactory.getVersionsController();
    assertEquals(NullVersionsController.class, defaultRevisionController.getClass());
  }


}
