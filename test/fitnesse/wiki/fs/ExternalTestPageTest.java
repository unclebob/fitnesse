package fitnesse.wiki.fs;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import fitnesse.wiki.PageType;
import fitnesse.wiki.SystemVariableSource;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wikitext.parser.VariableSource;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;


public class ExternalTestPageTest {

  private MemoryFileSystem fileSystem;
  private WikiPage rootPage;
  private VariableSource variableSource;

  @Before
  public void prepare() {
    fileSystem = new MemoryFileSystem();
    variableSource = new SystemVariableSource();
    rootPage = new FileSystemPageFactory(fileSystem, new SimpleFileVersionsController(fileSystem)).makePage(new File("RooT"), "RooT", null, variableSource);
  }


  @Test
  public void PageDataIsFileContents() throws Exception {
    assertEquals("!-stuff-!", makePage("somewhere", "myfile.html", "stuff").getData().getContent());
  }

  @Test
  public void WhenHasNoTableThenIsNotTest() throws Exception {
    assertFalse(makePage("somewhere", "myfile.html", "stuff").getData().hasAttribute(PageType.TEST.toString()));
  }

  @Test
  public void WhenHasTableThenIsTest() throws Exception {
    assertTrue(makePage("somewhere", "myfile.html", "stuff and <table>").getData().hasAttribute(PageType.TEST.toString()));
  }

  @Test
  public void shouldNotHaveChildPages() throws IOException {
    assertThat(makePage("somewhere", "myfile.html", "stuff and <table>").getChildren(), is(Collections.<WikiPage>emptyList()));
  }

  @Test
  public void externalPageShouldBeAChildOfSuite() throws IOException {
    fileSystem.makeFile(new File("somewhere/MyTest/myfile.html"), "stuff");
    ExternalSuitePage suite = new ExternalSuitePage(new File("somewhere", "MyTest"), "MyTest", rootPage, fileSystem, variableSource);
    ExternalTestPage testPage = (ExternalTestPage) suite.getChildren().get(0);
    WikiPagePath path = testPage.getPageCrawler().getFullPath();
    assertEquals("Page path for external file", "MyTest.myfile", path.toString());
  }

  private ExternalTestPage makePage(String directory, String name, String content) throws IOException {
    FileSystem fileSystem = new MemoryFileSystem();
    File path = new File(directory, name);
    fileSystem.makeFile(path, content);
    WikiPage rootPage = new FileSystemPageFactory(fileSystem, new SimpleFileVersionsController(fileSystem)).makePage(new File("RooT"), "RooT", null, variableSource);
    return new ExternalTestPage(path, name, rootPage, fileSystem, variableSource);
  }
}
