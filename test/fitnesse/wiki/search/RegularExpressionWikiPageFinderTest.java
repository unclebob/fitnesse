package fitnesse.wiki.search;

import fitnesse.wiki.HitCollector;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.fs.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

public class RegularExpressionWikiPageFinderTest {

  private WikiPage root;
  private WikiPage pageOne;
  private WikiPage childPage;
  private WikiPage virtualPage;

  HitCollector hits = new HitCollector();
  private WikiPageFinder pageFinder;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    pageOne = WikiPageUtil.addPage(root, PathParser.parse("PageOne"), "has PageOne content");
    childPage = WikiPageUtil.addPage(root, PathParser.parse("PageOne.PageOneChild"),
            "PageChild is a child of PageOne");
    virtualPage = WikiPageUtil.addPage(root, PathParser.parse("PageTwo"),
            "PageTwo has a bit of content too\n^PageOneChild");
    PageData data = virtualPage.getData();
    virtualPage.commit(data);
  }

  @Test
  public void searcher() throws Exception {
    pageFinder = pageFinder("has");
    pageFinder.search(root);
    hits.assertPagesFound(pageOne.getName(), virtualPage.getName());
  }

  @Test
  public void searcherAgain() throws Exception {
    pageFinder = pageFinder("a");
    pageFinder.search(root);
    hits.assertPagesFound(pageOne.getName(), childPage.getName(), virtualPage.getName());
  }

  @Test
  public void dontSearchProxyPages() throws Exception {
    pageFinder = pageFinder("a");
    pageFinder.search(virtualPage);
    hits.assertPagesFound(virtualPage.getName());
  }

  @Test
  public void observing() throws Exception {
    pageFinder = pageFinder("has");
    pageFinder.search(root);
    hits.assertPagesFound(pageOne.getName(), virtualPage.getName());
  }

  @Test
  public void pagesNotMatching() throws Exception {
    pageFinder = pageFinder(notMatchingSearchText());
    pageFinder.search(root);
    hits.assertPagesFound();
  }

  @Test
  public void singlePageMatches() throws Exception {
    pageFinder = pageFinder(matchTextForPageOne());
    pageFinder.search(root);
    hits.assertPagesFound(pageOne.getName());
  }

  @Test
  public void multiplePageMatch() throws Exception {
    pageFinder = pageFinder(matchAll());
    pageFinder.search(root);
    hits.assertPagesFound(root.getName(), pageOne.getName(), childPage.getName(), virtualPage.getName());
  }

  @Test
  public void matchesSublevels() throws Exception {
    pageFinder = pageFinder(matchAll());
    pageFinder.search(pageOne);
    hits.assertPagesFound(pageOne.getName(), childPage.getName());
  }

  private String matchAll() {
    return ".*";
  }

  private String matchTextForPageOne() {
    return "PageOne content";
  }

  private String notMatchingSearchText() {
    return "this search text does not match any page";
  }

  private WikiPageFinder pageFinder(String searchText) {
    return new RegularExpressionWikiPageFinder(searchText, hits);
  }

}
