package fitnesse.wiki;

import java.io.File;

public interface WikiPageFactory<T extends WikiPage> {

  WikiPage makePage(File path, String pageName, T parent);

  boolean supports(File path);
}
