package fitnesse.wiki;

import java.lang.String;

import fitnesse.ComponentFactory;
import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.WikiPage;

public interface WikiPageFactory {
  WikiPage makeRootPage(String rootPath, String rootPageName);

  WikiPage makeChildPage(String name, FileSystemPage parent);

}
