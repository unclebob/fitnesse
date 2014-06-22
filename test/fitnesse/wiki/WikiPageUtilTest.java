package fitnesse.wiki;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class WikiPageUtilTest {

  @Test
  public void shouldResolveFileUris() {
    assertThat(WikiPageUtil.resolveFileUri("file:/tmp/someFile", new File(".")), equalTo(new File ("/tmp/someFile")));
    assertThat(WikiPageUtil.resolveFileUri("file:///tmp/someFile", new File(".")), equalTo(new File ("/tmp/someFile")));
    assertThat(WikiPageUtil.resolveFileUri("file:////tmp/someFile", new File(".")), equalTo(new File ("/tmp/someFile")));
  }

  @Test
  public void shouldSupportRelativeLinksWithDoubleSlash() throws IOException {
    assertThat(WikiPageUtil.resolveFileUri("file:tmp/someFile", new File(".")), equalTo(new File (new File(".").getCanonicalFile(), "/tmp/someFile")));
    assertThat(WikiPageUtil.resolveFileUri("file://tmp/someFile", new File(".")), equalTo(new File (new File(".").getCanonicalFile(), "/tmp/someFile")));
  }

  @Test( expected = IllegalArgumentException.class )
  public void shouldOnlyHandleFileUris() throws IOException {
    assertThat(WikiPageUtil.resolveFileUri("jiberish:/tmp/someFile", new File(".")), equalTo(new File (new File(".").getCanonicalFile(), "/tmp/someFile")));
  }

}