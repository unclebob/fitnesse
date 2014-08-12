package fitnesse.testsystems;

import fitnesse.wiki.ReadOnlyPageData;

public interface TestPage {
  ReadOnlyPageData getDecoratedData();

  String getHtml();

  String getVariable(String name);

  String getFullPath();
}
