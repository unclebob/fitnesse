package fitnesse.wikitext;

import org.junit.Assert;
import org.junit.Test;

public class MarkupSystemsTest {
  @Test
  public void findsName() {
    assertName("lang on first line", "name", "#lang name\nstuff");
    assertName("skips leading blankspace", "name", "\n \n#lang name\nstuff");
    assertName("missing lang", "", "#langname\nstuff");
    assertName("alternate newline", "name", "#lang name\r\nstuff");
  }

  private void assertName(String message, String expected, String content) {
    Assert.assertEquals(message, expected, MarkUpSystems.findName(content));
  }
}
