package fitnesse.wiki.fs;

import java.io.File;

import fitnesse.wiki.SystemVariableSource;
import fitnesse.wiki.mem.MemoryFileSystem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ExternalSuitePageTest {

  private FileSystemPage rootPage;
  private FileSystem fileSystem;

  @Before
  public void prepare() {
    fileSystem = new MemoryFileSystem();
    rootPage = new FileSystemPageFactory(fileSystem, new SimpleFileVersionsController(fileSystem), new SystemVariableSource()).makeRootPage("", "RooT");
  }

    @Test
    public void ContentIsTableOfContents() throws Exception {
        Assert.assertEquals("!contents", new ExternalSuitePage(new File("somewhere"), "MyTest", rootPage, fileSystem).getData().getContent());
    }

    @Test
    public void ChildrenAreLoaded() throws Exception {
        fileSystem.makeFile(new File("somewhere/MyTest/myfile.html"), "stuff");
        Assert.assertEquals(1, new ExternalSuitePage(new File("somewhere/MyTest"), "MyTest", rootPage, fileSystem).getChildren().size());
    }
}
