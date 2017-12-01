package fitnesse.wiki.search;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.PagePruningStrategy;
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

  public void search(WikiPage page, PagePruningStrategy strategy) {
    page.getPageCrawler(strategy).traverse(traverser);
  }
}


