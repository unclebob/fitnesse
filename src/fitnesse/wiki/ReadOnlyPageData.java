package fitnesse.wiki;

import java.util.Date;

public interface ReadOnlyPageData {
  String getContent();
  String getAttribute(String attribute);
  boolean hasAttribute(String attribute);
  WikiPageProperty getProperties();
}
