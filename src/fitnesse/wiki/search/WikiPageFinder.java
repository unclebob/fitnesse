package fitnesse.wiki.search;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.WikiPage;

public abstract class WikiPageFinder implements TraversalListener<WikiPage>, PageFinder {

  protected TraversalListener<? super WikiPage> observer;

  protected WikiPageFinder(TraversalListener<? super WikiPage> observer) {
    this.observer = observer;
  }

  protected abstract boolean pageMatches(WikiPage page);

  public void process(WikiPage page) {
    if (pageMatches(page)) {
      observer.process(page);
    }
  }

  public void search(WikiPage page) {
    page.getPageCrawler().traverse(this);
  }
}