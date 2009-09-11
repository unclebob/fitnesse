package fitnesse.wikitext.widgets;

import fitnesse.wikitext.WikiWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HashWidget extends ParentWidget {
  public static final String REGEXP = "^!\\{\\s*(?:[^,:\\s]+\\s*:\\s*[^,:\\s]+\\s*,?\\s*)*\\s*\\}";
  private static final Pattern pattern = Pattern.compile("^!\\{\\s*((?:[^,:\\s]+\\s*:\\s*[^,:\\s]+\\s*,?\\s*)*)\\s*\\}");
  private List<String> keys = new ArrayList<String>();
  private static final Pattern pair = Pattern.compile("([^,:\\s]+)\\s*:\\s*([^,:\\s]+)\\s*,?\\s*");


  public HashWidget(ParentWidget parent, String text) throws Exception {
    super(parent);
    Matcher match = pattern.matcher(text);
    match.find();
    String pairs = match.group(1);
    Matcher pairMatcher = pair.matcher(pairs);
    while (pairMatcher.find()) {
      addKey(pairMatcher.group(1));
      addChildWidgets(pairMatcher.group(2));
    }

  }

  private void addKey(String key) {
    keys.add(key);
  }

  public String render() throws Exception {
    StringBuffer html = new StringBuffer("<table class=\"hash_table\">");
    for (int i=0; i<keys.size(); i++) {
      String key = keys.get(i);
      WikiWidget widget = children.get(i);
      html.append(String.format(
        "<tr class=\"hash_row\">" +
          "<td class=\"hash_key\">" +
          "%s" +
          "</td>" +
          "<td class=\"hash_value\">" +
          "%s" +
          "</td>" +
          "</tr>", key, widget.render()));
    }
    html.append("</table>");
    return html.toString();
  }

  public int numberOfKeys() {
    return keys.size();
  }

  public List<String> getKeys() {
    return keys;
  }
}
