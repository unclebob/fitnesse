package fitnesse.components;

import fitnesse.wiki.WikiPage;

import java.util.ArrayList;
import java.util.List;

public abstract class WikiPageFinder implements TraversalListener<WikiPage>, PageFinder {

  protected List<WikiPage> hits;
  protected TraversalListener observer;

  protected WikiPageFinder(TraversalListener observer) {
    this.observer = observer;
  }

  protected abstract boolean pageMatches(WikiPage page);

  public void process(WikiPage page) {
    if (pageMatches(page)) {
      observer.process(page);
    }
  }

  public List<WikiPage> search(WikiPage page) {
    hits = new ArrayList<WikiPage>();
    page.getPageCrawler().traverse(page, this);
    return hits;
  }
}