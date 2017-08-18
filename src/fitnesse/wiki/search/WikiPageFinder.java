package fitnesse.wiki.search;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.WikiPage;

public abstract class WikiPageFinder implements PageFinder {

  private WikiPageTraverser traverser;

  protected WikiPageFinder(TraversalListener<? super WikiPage> observer) {
    this.traverser = new WikiPageTraverser(this, observer);
  }

  protected abstract boolean pageMatches(WikiPage page);

  @Override
  public void search(WikiPage page) {
    page.getPageCrawler().traverse(traverser);
  }
}


class WikiPageTraverser implements TraversalListener<WikiPage> {

  private final WikiPageFinder finder;
  private final TraversalListener<? super WikiPage> observer;

  WikiPageTraverser(WikiPageFinder finder, TraversalListener<? super WikiPage> observer){
    this.finder = finder;
    this.observer = observer;
  }

  @Override
  public void process(WikiPage page) {
    if (finder.pageMatches(page)) {
      observer.process(page);
    }
  }
}
