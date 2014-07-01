package fitnesse.wiki;

import java.io.File;

public interface WikiPageFactory<T extends WikiPage> {
  @Deprecated
  WikiPage makeRootPage(String path, String pageName);

  WikiPage makePage(File path, String pageName, T parent);

  boolean supports(File path);
}
