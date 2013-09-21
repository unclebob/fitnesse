package fitnesse.testsystems;

import fitnesse.wiki.ReadOnlyPageData;

public interface TestPage {
  ReadOnlyPageData getDecoratedData();
}
