package fitnesse.components;

import java.util.ArrayList;
import java.util.List;

import fitnesse.wiki.WikiPage;

public abstract class WikiPageFinder implements SearchObserver, TraversalListener, PageFinder {

  protected List<WikiPage> hits;
  protected SearchObserver observer;

  protected WikiPageFinder() {
    observer = this;
  }

  public String getSearchPattern() throws Exception {
    return "";
  }


  public void hit(WikiPage page) throws Exception {
    hits.add(page);
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