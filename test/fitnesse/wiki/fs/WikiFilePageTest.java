package fitnesse.wiki.fs;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fitnesse.wiki.*;
import util.FileUtil;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

public class WikiFilePageTest {

  private FileSystem fileSystem;
  private VersionsController versionsController;
  private WikiPage root;

  @Before
  public void setUp() throws Exception {
    fileSystem = new MemoryFileSystem();
    versionsController = new SimpleFileVersionsController(fileSystem);
    root = new FileSystemPageFactory(fileSystem, versionsController).makePage(new File("root"), "root", null, new SystemVariableSource());
  }

  @Test
  public void shouldListChildren() {
    WikiPageUtil.addPage(root, PathParser.parse("AaAa"), "A content");
    WikiPageUtil.addPage(root, PathParser.parse("BbBb"), "B content");
    WikiPageUtil.addPage(root, PathParser.parse("c"), "C content");
    List<WikiPage> children = root.getChildren();
    assertEquals(3, children.size());
    for (WikiPage child : children) {
      String name = child.getName();
      boolean isOk = "AaAa".equals(name) || "BbBb".equals(name) || "c".equals(name);
      assertTrue("WikiPAge is not a valid one: " + name, isOk);
    }
  }

  @Test
  public void loadRootPageContent() throws IOException {
    fileSystem.makeFile(new File("root", "_root.wiki"), "root page content");
    root = new FileSystemPageFactory(fileSystem, versionsController).makePage(new File("root"), "root", null, new SystemVariableSource());
    assertThat(root.getData().getContent(), is("root page content"));
  }

  @Test
  public void loadPageContent() throws IOException {
    fileSystem.makeFile(new File("root", "testPage.wiki"), "page content");
    final WikiPage testPage = root.getChildPage("testPage");
    assertThat(testPage.getData().getContent(), is("page content"));
  }

  @Test
  public void loadPageProperties() throws IOException {
    fileSystem.makeFile(new File("root", "testPage.wiki"), "page content");
    final WikiPage testPage = root.getChildPage("testPage");
    final WikiPageProperty properties = testPage.getData().getProperties();
    assertThat(properties.getProperty(WikiPageProperty.EDIT), is(not(nullValue())));
  }
}
