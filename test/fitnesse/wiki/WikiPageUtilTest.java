package fitnesse.wiki;

import java.io.File;
import java.io.IOException;
import java.util.List;

import fitnesse.wiki.fs.InMemoryPage;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class WikiPageUtilTest {

  @Test
  public void shouldResolveFileUris() {
    assertThat(WikiPageUtil.resolveFileUri("file:/tmp/someFile", new File(".")), equalTo(new File("/tmp/someFile")));
    assertThat(WikiPageUtil.resolveFileUri("file:///tmp/someFile", new File(".")), equalTo(new File("/tmp/someFile")));
    //assertThat(WikiPageUtil.resolveFileUri("file:////tmp/someFile", new File(".")), equalTo(new File ("/tmp/someFile")));
  }

  @Test
  public void shouldSupportRelativeLinksWithDoubleSlash() throws IOException {
    assertThat(WikiPageUtil.resolveFileUri("file:tmp/someFile", new File(".")), equalTo(new File(new File(".").getCanonicalFile(), "/tmp/someFile")));
    assertThat(WikiPageUtil.resolveFileUri("file://tmp/someFile", new File(".")), equalTo(new File(new File(".").getCanonicalFile(), "/tmp/someFile")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldOnlyHandleFileUris() throws IOException {
    assertThat(WikiPageUtil.resolveFileUri("jiberish:/tmp/someFile", new File(".")), equalTo(new File(new File(".").getCanonicalFile(), "/tmp/someFile")));
  }

  @Test
  public void testGetCrossReferences() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("PageName"), "!see XrefPage\r\n");
    List<?> xrefs = WikiPageUtil.getXrefPages(page);
    assertEquals("XrefPage", xrefs.get(0));
  }

  @Test
  public void testGetCrossReferencesWithAlias() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("PageName"), "!see [[xref page][XrefPage]]\r\n");
    List<?> xrefs = WikiPageUtil.getXrefPages(page);
    assertEquals("XrefPage", xrefs.get(0));
  }

  @Test
  public void testGetCrossReferencesWithMalformedAlias() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("PageName"), "!see [[starts like alias but is not\r\n");
    List<?> xrefs = WikiPageUtil.getXrefPages(page);
    assertEquals(0, xrefs.size());
  }

}
