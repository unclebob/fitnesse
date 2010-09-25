package fitnesse.wikitext.widgets;

import org.junit.Before;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class PlainTextTableWidgetTest {
  private Pattern pattern;

  @Before
  public void setup() {
    pattern = Pattern.compile(PlainTextTableWidget.REGEXP, Pattern.DOTALL);
  }

  @Test
  public void widgetsThatShouldMatchThePattern() throws Exception {
    String matchingStrings[] = {
      "![\nsingle row\n]!\n",
      "![\nrow1\nrow2\n]!\n",
      "![:\nrow\n]!\n",     // cell seperation punctuation
      "![: firstRow\nsecondRow\n]!\n",
      "![ firstRow\nsecondRow\n]!\n",
      "![\na\nb\nc\nd\n]!\n",
    };
    for (String matchingString : matchingStrings)
      assertTrue(matchingString + " should match", pattern.matcher(matchingString).matches());
  }

  @Test
  public void widgetsThatShouldNotMatchThePattern() throws Exception {
    String[] matchingStrings = {
      "![single row\n]!\n", //no line break after ![
      "![\nsingle row]!\n", // no line end before closing ]!
      " ![\nrow\n]!\n",   // ![ not at start of line.
      "![a\nrow\n]!\n",   // cell seperation should be punctuation
    };
    for (String matchingString : matchingStrings)
      assertFalse(matchingString + " should not match", pattern.matcher(matchingString).matches());
  }

  @Test
  public void parseWidget() throws Exception {
    parse("![\nbody\n]!", "", "", "body");
    parse("![:\nbody\n]!", ":", "", "body");
    parse("![: first\nbody\n]!", ":", "first", "body");
    parse("![ first\nbody\n]!", "", "first", "body");
    parse("![: first\nsecond\nthird\nfourth\n]!", ":", "first", "second\nthird\nfourth");
  }

  private void parse(String widgetText, String delimiter, String firstRow, String body) throws Exception {
    PlainTextTableWidget widget = new PlainTextTableWidget(null, widgetText);
    assertEquals(delimiter, widget.getDelimiter());
    assertEquals(firstRow, widget.getHiddenFirstRow());
    assertEquals(body, widget.getBody());
  }

  @Test
  public void renderWidget() throws Exception {
    render("![\nrow\n]!", "<table class=\"plain_text_table\"><tr><td>row</td></tr></table>");
    render("![\nr1\nr2\n]!", "<table class=\"plain_text_table\"><tr><td>r1</td></tr><tr><td>r2</td></tr></table>");
    render("![:\nc1:c2\n]!", "<table class=\"plain_text_table\"><tr><td>c1</td><td>c2</td></tr></table>");
    render("![: hidden\nr2\n]!", "<table class=\"plain_text_table\"><tr class=\"hidden\"><td>hidden</td></tr><tr><td>r2</td></tr></table>");
    render("![ hidden\nr2\n]!", "<table class=\"plain_text_table\"><tr class=\"hidden\"><td>hidden</td></tr><tr><td>r2</td></tr></table>");
    render("![, h1,h2\nr21,r22\nr31,r32,r33\n]!",
      "<table class=\"plain_text_table\">" +
        "<tr class=\"hidden\"><td>h1</td><td colspan=\"2\">h2</td></tr>" +
        "<tr><td>r21</td><td colspan=\"2\">r22</td></tr>" +
        "<tr><td>r31</td><td>r32</td><td>r33</td></tr>" +
        "</table>");
  }

  private void render(String widgetText , String html) throws Exception {
    PlainTextTableWidget widget = new PlainTextTableWidget(new MockWidgetRoot(), widgetText);
    assertEquals(html, widget.render().replaceAll("\n", "").replaceAll("\r", ""));
  }
}

