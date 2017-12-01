package fitnesse.wiki.search;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.WikiPage;

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
