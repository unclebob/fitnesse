package fitnesse.fixtures;

import fitnesse.wikitext.Utils;

public class StringComparator {
  public boolean stringStartsWith(String it, String starts) {
    return it.startsWith(Utils.escapeHTML(starts));
  }

  public boolean stringEndsWith(String it, String ending) {
    return it.endsWith(Utils.escapeHTML(ending));
  }

  public boolean stringContains(String it, String part) {
    return it.indexOf(Utils.escapeHTML(part)) != -1;
  }
}
