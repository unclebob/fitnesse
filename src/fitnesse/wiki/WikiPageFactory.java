package fitnesse.wiki;

public interface WikiPageFactory {
  WikiPage makeRootPage(String rootPath, String rootPageName);
}
