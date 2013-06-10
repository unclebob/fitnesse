package fitnesse.wiki.fs;

import fitnesse.wiki.VersionInfo;
import org.junit.Test;

import java.io.File;
import java.util.Date;
import java.util.regex.Matcher;

import static fitnesse.wiki.fs.ZipFileVersionInfo.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ZipFileVersionInfoTest {

  @Test
  public void testFilePatternWithDate() {
    Matcher match = COMPEX_NAME_PATTERN.matcher("01234567890123");
    assertTrue(match.find());
    assertEquals("01234567890123", match.group(2));
  }

  @Test
  public void testFilePatternWithAuthorAndDate() {
    Matcher match = COMPEX_NAME_PATTERN.matcher("Joe-01234567890123");
    assertTrue(match.find());
    assertEquals("Joe", match.group(1));
    assertEquals("01234567890123", match.group(2));
  }

  @Test
  public void testParts() {
    VersionInfo version = makeVersionInfo(new File("joe-20030101010101.zip"));
    assertEquals("joe", version.getAuthor());
    assertEquals("joe-20030101010101", version.getName());
  }

  @Test
  public void testGetCreationTime() throws Exception {
    VersionInfo version = makeVersionInfo(new File("joe-20030101010101.zip"));
    Date date = version.getCreationTime();
    assertEquals("20030101010101", makeVersionTimeFormat().format(date));
  }

  @Test
  public void testGetAuthor() throws Exception {
    checkAuthor("01234567890123", "");
    checkAuthor("123-01234567890123", "");
    checkAuthor("-123-01234567890123", "");
    checkAuthor("user-01234567890123", "user");
    checkAuthor("user-123-01234567890123", "user");
    checkAuthor("abc123-123-01234567890123", "abc123");
    checkAuthor("abc123efg-123-01234567890123", "abc123efg");
    checkAuthor("joe <joe@blo.com>-123-01234567890123", "joe <joe@blo.com>");
  }

  private void checkAuthor(String versionName, String author) throws Exception {
    VersionInfo version = makeVersionInfo(new File(versionName + ".zip"));
    assertEquals(author, version.getAuthor());
  }


}
