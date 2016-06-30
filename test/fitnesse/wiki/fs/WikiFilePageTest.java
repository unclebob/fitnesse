package fitnesse.wiki.fs;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.hamcrest.Matcher;
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

  @Test
  public void loadPageWithFrontMatter() throws IOException {
    fileSystem.makeFile(new File("root", "testPage.wiki"),
        "---\n" +
        "Test\n" +
        "Help: text comes here\n" +
        "---\n" +
        "page content");
    final WikiPage testPage = root.getChildPage("testPage");
    String content = testPage.getData().getContent();
    final WikiPageProperty properties = testPage.getData().getProperties();
    assertThat(content, is("page content"));
    assertThat("Test", properties.get("Test"), isPresent());
    assertThat("Help", properties.get("Help"), is("text comes here"));
  }

  @Test
  public void loadPageWithFrontMatterCanUnsetProperties() throws IOException {
    fileSystem.makeFile(new File("root", "testPage.wiki"),
      "---\n" +
        "Files: no\n" +
        "---\n" +
        "page content");
    final WikiPage testPage = root.getChildPage("testPage");
    String content = testPage.getData().getContent();
    final WikiPageProperty properties = testPage.getData().getProperties();
    assertThat(content, is("page content"));
    assertThat("Files", properties.get("Files"), isNotPresent());
  }

  @Test
  public void loadPageWithFrontMatterWithSymbolicLinks() throws IOException {
    fileSystem.makeFile(new File("root", "testPage.wiki"),
      "---\n" +
        "SymbolicLinks:\n" +
        "  Linked: SomePage\n" +
        "---\n" +
        "page content");
    final WikiPage testPage = root.getChildPage("testPage");
    String content = testPage.getData().getContent();
    final WikiPageProperty properties = testPage.getData().getProperties();
    assertThat(content, is("page content"));
    assertThat("SymbolicLinks", properties.getProperty("SymbolicLinks").get("Linked"), is("SomePage"));
  }

  private Matcher<? super String> isPresent() {
    return is(not(nullValue()));
  }

  private Matcher<? super String> isNotPresent() {
    return is(nullValue());
  }

  // TODO: test page removal, also as child of FileSystemPage
}
