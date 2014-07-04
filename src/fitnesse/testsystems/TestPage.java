package fitnesse.testsystems;

import fitnesse.wiki.ReadOnlyPageData;
import fitnesse.wiki.WikiPage;

public interface TestPage extends WikiPage {
  ReadOnlyPageData getDecoratedData();
}
