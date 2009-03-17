// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import junit.framework.TestCase;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;

public class SearcherTest extends TestCase implements SearchObserver {
  WikiPage root;
  private WikiPage pageTwo;

  private List<WikiPage> hits = new ArrayList<WikiPage>();
  private PageCrawler crawler;
  private Searcher searcher;

  public void hit(WikiPage page) throws Exception {
    hits.add(page);
  }

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    crawler.addPage(root, PathParser.parse("PageOne"), "has PageOne content");
    crawler.addPage(root, PathParser.parse("PageOne.PageOneChild"), "PageChild is a child of PageOne");
    pageTwo = crawler.addPage(root, PathParser.parse("PageTwo"), "PageTwo has a bit of content too\n^PageOneChild");
    PageData data = pageTwo.getData();
    data.setAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE, "http://localhost:" + FitNesseUtil.port + "/PageOne");
    pageTwo.commit(data);
  }

  public void testSearcher() throws Exception {
    searcher = new Searcher("has", root);
    getResults();
    assertEquals(2, hits.size());
    hasNamedPageAtIndex(hits, "PageOne", 0);
    hasNamedPageAtIndex(hits, "PageTwo", 1);
  }

  public void testSearcherAgain() throws Exception {
    searcher = new Searcher("a", root);
    getResults();
    assertEquals(3, hits.size());
    hasNamedPageAtIndex(hits, "PageOne", 0);
    hasNamedPageAtIndex(hits, "PageOneChild", 1);
    hasNamedPageAtIndex(hits, "PageTwo", 2);
  }

  public void testDontSearchProxyPages() throws Exception {
    searcher = new Searcher("a", pageTwo);
    getResults();
    assertEquals(2, hits.size());
  }

  public void testObserving() throws Exception {
    Searcher searcher = new Searcher("has", root);
    searcher.searchContent(this);
    assertEquals(2, hits.size());
  }

  public void testTitleSearch() throws Exception {
    searcher = new Searcher("one", root);
    hits.clear();
    searcher.searchTitles(this);
    Collections.sort(hits, new Comparer());

    assertEquals(2, hits.size());
    assertEquals("PageOne", hits.get(0).getName());
    assertEquals("PageOneChild", hits.get(1).getName());
  }

  private void hasNamedPageAtIndex(List<WikiPage> results, String name, int index) throws Exception {
    WikiPage p = results.get(index);
    assertEquals(name, p.getName());
  }

  public void getResults() throws Exception {
    hits.clear();
    searcher.searchContent(this);
    Collections.sort(hits, new Comparer());
  }

  private class Comparer implements Comparator<Object> {
    public int compare(Object o1, Object o2) {
      try {
        WikiPage page1 = (WikiPage) o1;
        WikiPage page2 = (WikiPage) o2;
        return page1.getName().compareTo(page2.getName());
      }
      catch (Exception e) {
        return 0;
      }
    }
  }
}
