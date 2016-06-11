package fitnesse.wiki.fs;

import java.io.File;
import java.util.Iterator;
import org.junit.Before;
import org.junit.Test;

import fitnesse.wiki.PageData;
import fitnesse.wiki.SystemVariableSource;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MemoryVersionsControllerTest {

  private FileSystemPageFactory wikiPageFactory;

  @Before
  public void setUp() {
    FileSystem fileSystem = new MemoryFileSystem();
    MemoryVersionsController memoryVersionsController = new MemoryVersionsController(fileSystem);
    wikiPageFactory = new FileSystemPageFactory(fileSystem, memoryVersionsController);
  }

  @Test
  public void shouldStoreFirstVersionAsZero() {
    WikiPage root = makeRoot();
    root.commit(root.getData());
    assertEquals(1, root.getVersions().size());
    assertEquals("0", root.getVersions().iterator().next().getName());
  }

  private WikiPage makeRoot() {
    return wikiPageFactory.makePage(new File(""), "RooT", null, new SystemVariableSource());
  }

  @Test
  public void shouldStoreSecondVersionAsOne() {
    WikiPage root = makeRoot();
    root.commit(root.getData());
    root.commit(root.getData());

    assertEquals(2, root.getVersions().size());
    Iterator<VersionInfo> iterator = root.getVersions().iterator();
    assertEquals("0", iterator.next().getName());
    assertEquals("1", iterator.next().getName());
  }

  @Test
  public void shouldStoreFirstVersionAsZeroForSecondPage() {
    WikiPage root = makeRoot();
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
    WikiPage root = makeRoot();
    VersionInfo version = root.commit(root.getData());

    WikiPage versionData = root.getVersion(version.getName());
    PageData newData = versionData.getData();

    assertEquals("0", version.getName());
    assertNotNull(newData);
  }
}
