package fitnesse.fixtures;

import fitnesse.html.HtmlUtil;

public class StringComparator {
  public boolean stringStartsWith(String it, String starts) {
    return it.startsWith(HtmlUtil.escapeHTML(starts));
  }

  public boolean stringEndsWith(String it, String ending) {
    return it.endsWith(HtmlUtil.escapeHTML(ending));
  }

  public boolean stringContains(String it, String part) {
    return it.contains(HtmlUtil.escapeHTML(part));
  }
}
