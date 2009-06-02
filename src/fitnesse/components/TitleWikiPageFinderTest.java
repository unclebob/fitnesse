package fitnesse.components;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;

public class TitleWikiPageFinderTest implements SearchObserver {
  WikiPage root;
  private WikiPage pageTwo;

  private List<WikiPage> hits = new ArrayList<WikiPage>();
  private PageCrawler crawler;
  private TitleWikiPageFinder searcher;

  public void hit(WikiPage page) throws Exception {
    hits.add(page);
  }

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    crawler.addPage(root, PathParser.parse("PageOne"), "has PageOne content");
    crawler.addPage(root, PathParser.parse("PageOne.PageOneChild"), "PageChild is a child of PageOne");
    pageTwo = crawler.addPage(root, PathParser.parse("PageTwo"), "PageTwo has a bit of content too\n^PageOneChild");
    PageData data = pageTwo.getData();
    data.setAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE, "http://localhost:" + FitNesseUtil.port + "/PageOne");
    pageTwo.commit(data);
    hits.clear();
  }

  @Test
  public void titleSearch() throws Exception {
    searcher = new TitleWikiPageFinder("one", this);
    hits.clear();
    searcher.search(root);
    assertPagesFound("PageOne", "PageOneChild");
  }

  private void assertPagesFound(String... pageNames) throws Exception {
    assertEquals(pageNames.length, hits.size());

    List<String> pageNameList = Arrays.asList(pageNames);
    for (WikiPage page: hits) {
      assertTrue(pageNameList.contains(page.getName()));
    }
  }

}
