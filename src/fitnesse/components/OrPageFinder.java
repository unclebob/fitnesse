package fitnesse.components;

import java.util.ArrayList;
import java.util.List;

import fitnesse.wiki.WikiPage;

public class OrPageFinder implements CompositePageFinder {

  List<PageFinder> delegates;

  public OrPageFinder() {
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
        appendFinderResultsToResultList(pageFinder.search(page), results);
      }
    }
    return results;
  }

  private void appendFinderResultsToResultList(List<WikiPage> search,
      List<WikiPage> results) {
    for (WikiPage result: search) {
      if (!results.contains(result)) {
        results.add(result);
      }
    }
  }

}
