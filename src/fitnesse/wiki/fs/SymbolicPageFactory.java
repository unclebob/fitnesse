package fitnesse.wiki.fs;

import fitnesse.wiki.WikiPage;

public interface SymbolicPageFactory {

  WikiPage makePage(String linkPath, String linkName, WikiPage parent);

}
