package fitnesse.wiki;

public interface ReadOnlyPageData {
  String getContent();
  String getAttribute(String attribute);
  boolean hasAttribute(String attribute);
  WikiPageProperty getProperties();
}
