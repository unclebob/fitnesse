package fitnesse.components;

import java.util.ArrayList;
import java.util.List;

import fitnesse.wiki.WikiPage;

public abstract class WikiPageFinder implements TraversalListener, PageFinder {

  protected List<WikiPage> hits;
  protected SearchObserver observer;

  protected WikiPageFinder(SearchObserver observer) {
    this.observer = observer;
  }

  protected abstract boolean pageMatches(WikiPage page) throws Exception;

  public void processPage(WikiPage page) throws Exception {
    if (pageMatches(page)) {
      observer.hit(page);
    }
  }

  public List<WikiPage> search(WikiPage page) throws Exception {
    hits = new ArrayList<WikiPage>();
    page.getPageCrawler().traverse(page, this);
    return hits;
  }

}