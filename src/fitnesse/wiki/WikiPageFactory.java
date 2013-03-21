package fitnesse.wiki;

public interface WikiPageFactory {
  WikiPage makeRootPage(String path, String pageName);
}
