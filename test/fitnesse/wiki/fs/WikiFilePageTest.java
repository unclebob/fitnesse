package fitnesse.wiki.fs;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import fitnesse.wiki.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
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
  public void pagesShouldBeListedByOldStyleParentPage() throws IOException {
    File wikiPageFile = new File("root", "testPage.wiki");
    fileSystem.makeFile(wikiPageFile, "page content");

    final List<WikiPage> children = root.getChildren();

    assertThat(children, hasSize(1));
    assertThat(((WikiFilePage) children.get(0)).getFileSystemPath().getPath(),
        is("root" + File.separator + "testPage"));
    assertThat(children.get(0).getName(), is("testPage"));
  }

  @Test
  public void pagesWithSubPagesShouldNotBeListedTwice() throws IOException {
    File wikiPageFile = new File("root", "testPage.wiki");
    File subWikiPageFile = new File("root", "testPage/subPage.wiki");
    fileSystem.makeFile(wikiPageFile, "page content");
    fileSystem.makeFile(subWikiPageFile, "page content");

    final List<WikiPage> children = root.getChildren();

    assertThat(children, hasSize(1));
    assertThat(((WikiFilePage) children.get(0)).getFileSystemPath().getPath(),
        is("root" + File.separator + "testPage"));
    assertThat(children.get(0).getName(), is("testPage"));
  }

  @Test
  public void removePageWithoutSubPages() throws IOException {
    File wikiPageFile = new File("root", "testPage.wiki");
    fileSystem.makeFile(wikiPageFile, "page content");
    final WikiPage testPage = this.root.getChildPage("testPage");
    testPage.remove();

    assertFalse(fileSystem.exists(wikiPageFile));
  }

  @Test
  public void removePageWithSubPages() throws IOException {
    File wikiPageFile = new File("root", "testPage.wiki");
    File subWikiPageFile1 = new File("root", "testPage/sub1.wiki");
    File subWikiPageFile2 = new File("root", "testPage/sub2.wiki");
    fileSystem.makeFile(wikiPageFile, "page content");
    fileSystem.makeFile(subWikiPageFile1, "page content");
    fileSystem.makeFile(subWikiPageFile2, "page content");
    final WikiPage testPage = this.root.getChildPage("testPage");
    testPage.remove();

    assertFalse(fileSystem.exists(wikiPageFile));
    assertFalse(fileSystem.exists(subWikiPageFile1));
    assertFalse(fileSystem.exists(subWikiPageFile2));
  }

  @Test
  public void loadRootPageContent() throws IOException {
    fileSystem.makeDirectory(new File("root"));
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

  @Test
  public void loadPageWithEmptyLineInFrontMatter() throws IOException {
    fileSystem.makeFile(new File("root", "testPage.wiki"),
      "---\n" +
        "\n" +
        "---\n" +
        "page content");
    final WikiPage testPage = root.getChildPage("testPage");
    String content = testPage.getData().getContent();
    assertThat(content, is("page content"));
  }

  @Test
  public void loadPageWithFrontMatterAndDashesInContent() throws IOException {
    fileSystem.makeFile(new File("root", "testPage.wiki"),
      "---\n" +
        "\n" +
        "---\n" +
        "\n" +
        "---\n");
    final WikiPage testPage = root.getChildPage("testPage");
    String content = testPage.getData().getContent();
    assertThat(content, is("\n---\n"));
  }

  @Test
  public void updateWikiFile() throws IOException {
    File wikiPageFile = new File("root", "testPage.wiki");
    fileSystem.makeFile(wikiPageFile, "page content");
    final WikiPage testPage = root.getChildPage("testPage");
    PageData data = testPage.getData();
    data.setContent("updated!");
    testPage.commit(data);
    final String content = fileSystem.getContent(wikiPageFile);
    assertThat(content, is("updated!"));
  }

  @Test
  public void updateWikiFileWithPropertiesChanged() throws IOException {
    File wikiPageFile = new File("root", "testPage.wiki");
    fileSystem.makeFile(wikiPageFile, "page content");
    final WikiPage testPage = root.getChildPage("testPage");
    PageData data = testPage.getData();
    data.setContent("updated!");
    data.getProperties().set("Test");
    data.getProperties().set("Help", "foo");
    data.getProperties().remove("Edit");
    testPage.commit(data);
    final String content = fileSystem.getContent(wikiPageFile);
    assertThat(content, is(
      "---\n" +
      "Edit: no\n" +
      "Help: foo\n" +
      "Test\n" +
      "---\n" +
      "updated!"));
  }

  @Test
  public void updateWikiFileWithSymLinks() throws IOException {
    File wikiPageFile = new File("root", "testPage.wiki");
    fileSystem.makeFile(wikiPageFile, "page content");
    final WikiPage testPage = root.getChildPage("testPage");
    PageData data = testPage.getData();
    final WikiPageProperty symlinks = data.getProperties().set(SymbolicPage.PROPERTY_NAME);
    symlinks.set("PageOne", "RemotePage");
    symlinks.set("PageTwo", "AnotherRemotePage");
    testPage.commit(data);
    final String content = fileSystem.getContent(wikiPageFile);
    assertThat(content, is(
      "---\n" +
      "SymbolicLinks\n" +
      "  PageOne: RemotePage\n" +
      "  PageTwo: AnotherRemotePage\n" +
      "---\n" +
      "page content"));
  }

  @Test
  public void readWikiFileWithFrontMatterButNoContent() throws IOException {
    File wikiPageFile = new File("root", "testPage.wiki");
    fileSystem.makeFile(wikiPageFile, "---\n" +
      "Test\n" +
      "---\n");
    final WikiPage testPage = root.getChildPage("testPage");
    PageData data = testPage.getData();
    assertThat(data.getContent(), is(""));
  }

  @Test
  public void pageLoadShouldNotLoadRootPage() throws IOException {
    File rootWikiPageFile = new File("root", "_root.wiki");
    File wikiPageFile = new File("root", "testPage.wiki");
    fileSystem.makeFile(rootWikiPageFile, "test");
    fileSystem.makeFile(wikiPageFile, "test");
    final List<WikiPage> children = root.getChildren();

    assertThat(children, hasSize(1));
    assertThat(children.get(0).getName(), is("testPage"));
  }

  private Matcher<? super String> isPresent() {
    return is(not(nullValue()));
  }

  private Matcher<? super String> isNotPresent() {
    return is(nullValue());
  }

}
