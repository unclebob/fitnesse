package fitnesse.wikitext.widgets;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.regex.Pattern;


public class StyleWidgetTest {
  @Test
  public void regexp() throws Exception {
    String r = StyleWidget.REGEXP;
    assertTrue(Pattern.matches(r, "!style_x(my text)"));
    assertTrue(Pattern.matches(r, "!style_style(my text)"));
    assertFalse(Pattern.matches(r, "!style(Hi)"));
    assertFalse(Pattern.matches(r, "!style_(Hi)"));
    assertFalse(Pattern.matches(r, "!style_myStyle(hi))"));
  }

  @Test
  public void html() throws Exception {
    StyleWidget widget = new StyleWidget(new MockWidgetRoot(), "!style_myStyle(wow zap)");
    assertEquals("<span class=\"myStyle\">wow zap</span>", widget.render());
  }


}
