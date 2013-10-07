package fitnesse.wiki.fs;

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
        Assert.assertEquals("!contents", new ExternalSuitePage("somewhere", "MyTest", rootPage, fileSystem).getData().getContent());
    }

    @Test
    public void ChildrenAreLoaded() throws Exception {
        fileSystem.makeFile("somewhere/MyTest/myfile.html", "stuff");
        Assert.assertEquals(1, new ExternalSuitePage("somewhere/MyTest", "MyTest", rootPage, fileSystem).getChildren().size());
    }
}
