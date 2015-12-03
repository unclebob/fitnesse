package fitnesse.testsystems.slim;

import fitnesse.testsystems.slim.HtmlDiffUtil;
import org.junit.Assert;
import org.junit.Test;

public class HtmlDiffUtilTest {

  @Test
  public void testCompletelyDifferentTexts() {
    String actual = "abc";
    String expected = "xyz";
    String htmlDiff = null;
    htmlDiff = HtmlDiffUtil.buildActual(actual, expected);
    Assert.assertEquals("<span class=\"diff\">abc</span>", htmlDiff);
    htmlDiff = HtmlDiffUtil.buildExpected(actual, expected);
    Assert.assertEquals("<span class=\"diff\">xyz</span>", htmlDiff);
  }

  @Test
  public void testOneDifferentCharInMiddleOfText() {
    String actual = "abc";
    String expected = "axc";
    String htmlDiff = null;
    htmlDiff = HtmlDiffUtil.buildActual(actual, expected);
    Assert.assertEquals("a<span class=\"diff\">b</span>c", htmlDiff);
    htmlDiff = HtmlDiffUtil.buildExpected(actual, expected);
    Assert.assertEquals("a<span class=\"diff\">x</span>c", htmlDiff);
  }

  @Test
  public void testOneDifferentCharAtTheBeginningOfText() {
    String actual = "abc";
    String expected = "xbc";
    String htmlDiff = null;
    htmlDiff = HtmlDiffUtil.buildActual(actual, expected);
    Assert.assertEquals("<span class=\"diff\">a</span>bc", htmlDiff);
    htmlDiff = HtmlDiffUtil.buildExpected(actual, expected);
    Assert.assertEquals("<span class=\"diff\">x</span>bc", htmlDiff);
  }

  @Test
  public void testOneDifferentCharAtTheEndOfText() {
    String actual = "abc";
    String expected = "abx";
    String htmlDiff = null;
    htmlDiff = HtmlDiffUtil.buildActual(actual, expected);
    Assert.assertEquals("ab<span class=\"diff\">c</span>", htmlDiff);
    htmlDiff = HtmlDiffUtil.buildExpected(actual, expected);
    Assert.assertEquals("ab<span class=\"diff\">x</span>", htmlDiff);
  }

  @Test
  public void testBuilderOpeningTag() {
    String actual = "abc";
    String expected = "axc";
    String htmlDiff = null;
    htmlDiff = new HtmlDiffUtil.ActualBuilder(actual, expected)
        .setOpeningTag("<span style=\"font-weight: bold;\">").build();
    Assert.assertEquals("a<span style=\"font-weight: bold;\">b</span>c", htmlDiff);
    htmlDiff = new HtmlDiffUtil.ExpectedBuilder(actual, expected)
        .setOpeningTag("<span style=\"font-weight: bold;\">").build();
    Assert.assertEquals("a<span style=\"font-weight: bold;\">x</span>c", htmlDiff);
  }

  @Test
  public void testBuilderClosingTag() {
    String actual = "abc";
    String expected = "axc";
    String htmlDiff = null;
    htmlDiff = new HtmlDiffUtil.ActualBuilder(actual, expected)
        .setClosingTag("</span><em>extra</em>").build();
    Assert.assertEquals("a<span class=\"diff\">b</span><em>extra</em>c", htmlDiff);
    htmlDiff = new HtmlDiffUtil.ExpectedBuilder(actual, expected)
        .setClosingTag("</span><em>extra</em>").build();
    Assert.assertEquals("a<span class=\"diff\">x</span><em>extra</em>c", htmlDiff);
  }

  @Test
  public void testEscapeHtmlCharacters() {
    String actual = "1 > 2";
    String expected = "1 < 2";
    String htmlDiff = null;
    htmlDiff = HtmlDiffUtil.buildActual(actual, expected);
    Assert.assertEquals("1 <span class=\"diff\">&gt;</span> 2", htmlDiff);  
    htmlDiff = HtmlDiffUtil.buildExpected(actual, expected);
    Assert.assertEquals("1 <span class=\"diff\">&lt;</span> 2", htmlDiff);  
  }
  
}
