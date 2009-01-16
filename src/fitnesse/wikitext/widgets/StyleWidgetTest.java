package fitnesse.wikitext.widgets;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.regex.Pattern;


public class StyleWidgetTest {
  @Test
  public void parenRegexp() throws Exception {
    String r = StyleWidget.ParenFormat.REGEXP;
    assertTrue(Pattern.matches(r, "!style_x(my text)"));
    assertTrue(Pattern.matches(r, "!style_style(my text)"));
    assertFalse(Pattern.matches(r, "!style(Hi)"));
    assertFalse(Pattern.matches(r, "!style_(Hi)"));
    assertFalse(Pattern.matches(r, "!style_myStyle(hi))"));
  }

  @Test
  public void braceRegexp() throws Exception {
    String r = StyleWidget.BraceFormat.REGEXP;
    assertTrue(Pattern.matches(r, "!style_x{my text}"));
    assertTrue(Pattern.matches(r, "!style_style{my text}"));
    assertFalse(Pattern.matches(r, "!style{Hi}"));
    assertFalse(Pattern.matches(r, "!style_{Hi}"));
    assertFalse(Pattern.matches(r, "!style_myStyle{hi}}"));
  }

  @Test
  public void bracketRegexp() throws Exception {
    String r = StyleWidget.BracketFormat.REGEXP;
    assertTrue(Pattern.matches(r, "!style_x[my text]"));
    assertTrue(Pattern.matches(r, "!style_style[my text]"));
    assertFalse(Pattern.matches(r, "!style[Hi]"));
    assertFalse(Pattern.matches(r, "!style_[Hi]"));
    assertFalse(Pattern.matches(r, "!style_myStyle[hi]]"));
  }

  @Test
  public void parenFormatHtml() throws Exception {
    StyleWidget widget = new StyleWidget.ParenFormat(new MockWidgetRoot(), "!style_myStyle(wow zap)");
    assertEquals("<span class=\"myStyle\">wow zap</span>", widget.render());
  }

  @Test
  public void BracketFormatHtml() throws Exception {
    StyleWidget widget = new StyleWidget.BracketFormat(new MockWidgetRoot(), "!style_myStyle[wow zap]");
    assertEquals("<span class=\"myStyle\">wow zap</span>", widget.render());
  }

  @Test
  public void bracketFormWithParen() throws Exception {
    StyleWidget widget = new StyleWidget.BracketFormat(new MockWidgetRoot(), "!style_myStyle[)]");
    assertEquals("<span class=\"myStyle\">)</span>", widget.render());
  }

  @Test
  public void BraceFormatHtml() throws Exception {
    StyleWidget widget = new StyleWidget.BraceFormat(new MockWidgetRoot(), "!style_myStyle{wow zap}");
    assertEquals("<span class=\"myStyle\">wow zap</span>", widget.render());
  }


}
