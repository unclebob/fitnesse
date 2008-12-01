package fitnesse.wikitext.widgets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StyleWidget extends ParentWidget {
  public static final String REGEXP = "!style_\\w+\\([^\r\n\\)]*\\)";
  private static final Pattern pattern = Pattern.compile("!style_(\\w+)\\(([^\\)]*)\\)");
  private String style = "TILT";

  public StyleWidget(ParentWidget parent, String text) throws Exception {
    super(parent);
    Matcher match = pattern.matcher(text);
    if (match.find()) {
      style = match.group(1);
      addChildWidgets(match.group(2));
    }
  }

  public String render() throws Exception {
    return String.format("<span class=\"%s\">%s</span>", style, childHtml());
  }
}
