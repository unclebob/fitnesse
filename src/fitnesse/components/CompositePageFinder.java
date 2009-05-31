package fitnesse.components;

import java.util.ArrayList;
import java.util.List;

import fitnesse.wiki.WikiPage;

public class CompositePageFinder implements PageFinder {

  List<PageFinder> delegates;

  public CompositePageFinder() {
    delegates = new ArrayList<PageFinder>();
  }

  public void add(PageFinder finder) {
    delegates.add(finder);
  }

  public List<WikiPage> search(WikiPage page) throws Exception {
    List<WikiPage> results = null;
    for (PageFinder pageFinder: delegates) {
      if (results == null) {
        results = pageFinder.search(page);
      } else {
        results.retainAll(pageFinder.search(page));
      }
    }
    return results;
  }

}
