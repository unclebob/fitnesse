package fitnesse.wiki.search;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.NoPruningStrategy;
import fitnesse.wiki.PagePruningStrategy;
import fitnesse.wiki.WikiPage;

public abstract class WikiPageFinder implements PageFinder {
  public static final int MAX_THREADS = 4;

  private WikiPageTraverser traverser;

  protected WikiPageFinder(TraversalListener<? super WikiPage> observer) {
    this.traverser = new WikiPageTraverser(this, observer);
  }

  protected abstract boolean pageMatches(WikiPage page);

  /**
   * @return false if {@link #pageMatches(WikiPage)} does not look at the page data, so it need not be loaded ahead.
   */
  protected boolean readsPageData() {
    return true;
  }

  @Override
  public void search(WikiPage page) {
    search(page, new NoPruningStrategy());
  }

  public void search(WikiPage page, PagePruningStrategy strategy) {
    search(page, strategy, 1);
  }

  /**
   * @param page page to start searching at
   * @param strategy pages to leave out
   * @param threads number of threads to load pages with: 1 searches on the calling thread only,
   *                more load pages ahead on that many worker threads (at most {@link #MAX_THREADS})
   */
  public void search(WikiPage page, PagePruningStrategy strategy, int threads) {
    if (threads <= 1) {
      page.getPageCrawler().traverse(traverser, strategy);
    } else {
      new ParallelTraversal(traverser, strategy, readsPageData(), Math.min(threads, MAX_THREADS)).traverse(page);
    }
  }
}


