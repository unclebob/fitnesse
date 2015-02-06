package fitnesse.wiki.fs;

import java.io.File;

import fitnesse.wiki.SystemVariableSource;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExternalSuitePageTest {

  private FileSystemPage rootPage;
  private FileSystem fileSystem;
  private SystemVariableSource variableSource;

  @Before
  public void prepare() {
    fileSystem = new MemoryFileSystem();
    variableSource = new SystemVariableSource();
    rootPage = new FileSystemPageFactory(fileSystem, new SimpleFileVersionsController(fileSystem)).makePage(null, "RooT", null, variableSource);
  }

  @Test
  public void contentIsTableOfContents() throws Exception {
      assertEquals("!contents", new ExternalSuitePage(new File("somewhere"), "MyTest", rootPage, fileSystem, variableSource).getData().getContent());
  }

  @Test
  public void ChildrenAreLoaded() throws Exception {
      fileSystem.makeFile(new File("somewhere/MyTest/myfile.html"), "stuff");
      assertEquals(1, new ExternalSuitePage(new File("somewhere"), "MyTest", rootPage, fileSystem, variableSource).getChildren().size());
  }
}
