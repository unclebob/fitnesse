package fitnesse.wiki.mem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;

import fitnesse.wiki.PageData;
import fitnesse.wiki.SystemVariableSource;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.fs.FileSystem;
import fitnesse.wiki.fs.FileSystemPageFactory;
import fitnesse.wiki.fs.SimpleFileVersionsController;
import org.junit.Before;
import org.junit.Test;

public class MemoryVersionsControllerTest {

  private FileSystemPageFactory wikiPageFactory;

  @Before
  public void setUp() {
    FileSystem fileSystem = new MemoryFileSystem();
    MemoryVersionsController memoryVersionsController = new MemoryVersionsController(fileSystem);
    wikiPageFactory = new FileSystemPageFactory(fileSystem, memoryVersionsController, new SystemVariableSource());
  }

  @Test
  public void shouldStoreFirstVersionAsZero() {
    WikiPage root = wikiPageFactory.makeRootPage(null, "RooT");
    root.commit(root.getData());
    assertEquals(1, root.getVersions().size());
    assertEquals("0", root.getVersions().iterator().next().getName());
  }

  @Test
  public void shouldStoreSecondVersionAsOne() {
    WikiPage root = wikiPageFactory.makeRootPage(null, "RooT");
    root.commit(root.getData());
    root.commit(root.getData());

    assertEquals(2, root.getVersions().size());
    Iterator<VersionInfo> iterator = root.getVersions().iterator();
    assertEquals("0", iterator.next().getName());
    assertEquals("1", iterator.next().getName());
  }

  @Test
  public void shouldStoreFirstVersionAsZeroForSecondPage() {
    WikiPage root = wikiPageFactory.makeRootPage(null, "RooT");
    root.commit(root.getData());
    WikiPage page = root.addChildPage("PageOne");
    page.commit(root.getData());

    assertEquals(1, root.getVersions().size());
    assertEquals("0", root.getVersions().iterator().next().getName());

    assertEquals(1, page.getVersions().size());
    assertEquals("0", page.getVersions().iterator().next().getName());
  }

  @Test
  public void shouldLoadMostRecentVersion() {
    WikiPage root = wikiPageFactory.makeRootPage(null, "RooT");
    VersionInfo version = root.commit(root.getData());

    PageData newData = root.getDataVersion(version.getName());

    assertEquals("0", version.getName());
    assertNotNull(newData);
  }
}
