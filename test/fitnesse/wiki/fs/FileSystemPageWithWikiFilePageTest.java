package fitnesse.wiki.fs;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import fitnesse.wiki.PathParser;
import fitnesse.wiki.SystemVariableSource;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FileSystemPageWithWikiFilePageTest {
  private WikiPage root;

  @Before
  public void setUp() throws Exception {
    FileSystem fileSystem = new MemoryFileSystem();
    fileSystem.makeFile(new File("RooT", "content.txt"), "");
    root = new FileSystemPageFactory(fileSystem, new SimpleFileVersionsController(fileSystem)).makePage(new File("RooT"), "RooT", null, new SystemVariableSource());
  }

  @Test
  public void createChildPage() {
    WikiPageUtil.addPage(root, PathParser.parse("child"), "new content");

    WikiPage child = root.getChildPage("child");

    assertThat(root, is(instanceOf(FileSystemPage.class)));
    assertThat(child, is(instanceOf(WikiFilePage.class)));
    assertThat(child.getName(), is("child"));
    assertThat(child.getData().getContent(), is("new content"));
  }


}
