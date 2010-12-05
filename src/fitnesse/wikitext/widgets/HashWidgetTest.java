package fitnesse.wikitext.widgets;

import fitnesse.wikitext.WikiWidget;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class HashWidgetTest {
  private final String HTML_FOR_ABCD_HASH = "<table class=\"hash_table\">" +
    "\\s*<tr class=\"hash_row\">" +
    "\\s*<td class=\"hash_key\">" +
    "\\s*a" +
    "\\s*</td>" +
    "\\s*<td class=\"hash_value\">" +
    "\\s*b" +
    "\\s*</td>" +
    "\\s*</tr>" +
    "\\s*<tr class=\"hash_row\">" +
    "\\s*<td class=\"hash_key\">" +
    "\\s*c" +
    "\\s*</td>" +
    "\\s*<td class=\"hash_value\">" +
    "\\s*d" +
    "\\s*</td>" +
    "\\s*</tr>" +
    "\\s*</table>";
  private final String HTML_FOR_AB_HASH = "<table class=\"hash_table\">" +
    "\\s*<tr class=\"hash_row\">" +
    "\\s*<td class=\"hash_key\">" +
    "\\s*a" +
    "\\s*</td>" +
    "\\s*<td class=\"hash_value\">" +
    "\\s*b" +
    "\\s*</td>" +
    "\\s*</tr>" +
    "\\s*</table>";

  @Test
  public void testRegexp() throws Exception {
    assertTrue(Pattern.matches(HashWidget.REGEXP, "!{a:b}"));
    assertTrue(Pattern.matches(HashWidget.REGEXP, "!{a:b,c:d}"));
    assertTrue(Pattern.matches(HashWidget.REGEXP, "!{ a : b , c : d }"));
    assertTrue(Pattern.matches(HashWidget.REGEXP, "!{ a : b c : d }"));
    assertTrue(Pattern.matches(HashWidget.REGEXP, "!{a:b c:d}"));
    assertTrue(Pattern.matches(HashWidget.REGEXP, "!{}"));

    assertFalse(Pattern.matches(HashWidget.REGEXP, "!{]"));
  }

  @Test
  public void testConstruction() throws Exception {
    HashWidget widget = new HashWidget(new MockWidgetRoot(), "!{a:b,c:d}");
    assertEquals(2, widget.numberOfChildren());
    assertEquals(2, widget.numberOfKeys());
    List<String> keys = widget.getKeys();
    WikiWidget child = widget.nextChild();
    assertEquals("a", keys.get(0));
    assertEquals(TextWidget.class, child.getClass());
    assertEquals("b", ((TextWidget) child).getText());
    child = widget.nextChild();
    assertEquals("c", keys.get(1));
    assertEquals(TextWidget.class, child.getClass());
    assertEquals("d", ((TextWidget) child).getText());
  }

  @Test
  public void testHtml() throws Exception {
    HashWidget widget = new HashWidget(new MockWidgetRoot(), "!{a:b,c:d}");
    assertThat(widget.render(), matches(HTML_FOR_ABCD_HASH));
  }

  @Test
  public void commalessHtml() throws Exception {
    HashWidget widget = new HashWidget(new MockWidgetRoot(), "!{a:b c:d}");
    assertThat(widget.render(), matches(HTML_FOR_ABCD_HASH));
  }

  @Test
  public void onePairHash() throws Exception {
    HashWidget widget = new HashWidget(new MockWidgetRoot(), "!{a:b}");
    assertThat(widget.render(), matches(HTML_FOR_AB_HASH));
  }

  @Test
  public void withLotsOfSpaces() throws Exception {
    HashWidget widget = new HashWidget(new MockWidgetRoot(), "!{ a :  b ,    c :   d   }");
    assertThat(widget.render(), matches(HTML_FOR_ABCD_HASH));
  }

  @Test
  public void degenerateHash() throws Exception {
    HashWidget widget = new HashWidget(new MockWidgetRoot(), "!{ }");
    assertThat(widget.render(), matches(
      "<table class=\"hash_table\">" +
        "\\s*</table>"));

  }

  @Test
  public void multiLineHash() throws Exception {
    HashWidget widget = new HashWidget(new MockWidgetRoot(), "!{\n a :  b ,  \n  c :   d   \n}");
    assertThat(widget.render(), matches(HTML_FOR_ABCD_HASH));
  }

  private Matcher<String> matches(String s) {
    return new RegularExpressionMatcher(s);
  }

  private class RegularExpressionMatcher extends TypeSafeMatcher<String> {
    private Pattern pattern;

    public RegularExpressionMatcher(String pattern) {
      this.pattern = Pattern.compile(pattern);
    }

    public boolean matchesSafely(String s) {
      java.util.regex.Matcher matcher = pattern.matcher(s);
      return matcher.find();
    }

    public void describeTo(Description description) {
      description.appendText("a matching string");
    }

  }
}
