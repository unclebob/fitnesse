package fitnesse.testrunner;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.WikiPage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

class SuiteSpecificationTraverser implements TraversalListener<WikiPage> {

  private LinkedList<WikiPage> testPageList = new LinkedList<>();

  @Override
  public void process(WikiPage page) {
    for (WikiPage hit : testPageList) {
      if (hit.equals(page))
        return;
    }
    if (page.getData().hasAttribute("Test"))
      testPageList.add(page);
  }

  public List<WikiPage> testPages() {
    return new ArrayList<>(testPageList);
  }
}
