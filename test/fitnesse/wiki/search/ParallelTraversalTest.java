package fitnesse.wiki.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import fitnesse.wiki.NoPruningStrategy;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PagePruningStrategy;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.PrunedPagePruningStrategy;
import fitnesse.wiki.SymbolicLinkPruningStrategy;
import fitnesse.wiki.SymbolicPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;
import fitnesse.wiki.WikiPageLoadException;
import fitnesse.wiki.WikiPageProperty;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.fs.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ParallelTraversalTest {
  private WikiPage root;

  @Before
  public void setUp() {
    root = InMemoryPage.makeRoot("RooT");
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        WikiPageUtil.addPage(root, PathParser.parse("SuitePage" + i + ".SubSuite" + j + ".TestPage"), "content");
      }
    }
    WikiPage prunedPage = root.getChildPage("SuitePage3");
    PageData data = prunedPage.getData();
    data.setAttribute(WikiPageProperty.PRUNE);
    prunedPage.commit(data);
    WikiPage linkingPage = root.getChildPage("SuitePage1");
    data = linkingPage.getData();
    data.getProperties().set(SymbolicPage.PROPERTY_NAME).set("SymLink", ".SuitePage2");
    linkingPage.commit(data);
  }

  @Test
  public void visitsPagesInSameOrderAsPageCrawler() {
    assertSameOrderAsPageCrawler(new NoPruningStrategy());
  }

  @Test
  public void honoursPruningStrategy() {
    List<String> paths = assertSameOrderAsPageCrawler(new PrunedPagePruningStrategy());
    assertFalse(paths.contains("SuitePage3"));
    assertTrue(paths.contains("SuitePage1.SymLink.SubSuite0.TestPage"));
  }

  @Test
  public void skipsSymbolicLinks() {
    List<String> paths = assertSameOrderAsPageCrawler(new SymbolicLinkPruningStrategy());
    assertFalse(paths.contains("SuitePage1.SymLink"));
  }

  @Test(expected = WikiPageLoadException.class)
  public void passesOnErrorsLoadingPages() {
    WikiPage brokenPage = new WikiPageDummy("BrokenPage", "", root) {
      @Override
      public List<WikiPage> getChildren() {
        throw new WikiPageLoadException(new IOException("broken"));
      }
    };
    new ParallelTraversal(page -> {}, new NoPruningStrategy(), true, 4).traverse(brokenPage);
  }

  @Test(expected = BrokenPageError.class)
  public void passesOnErrorsThrownLoadingPages() {
    WikiPage brokenPage = new WikiPageDummy("BrokenPage", "", root) {
      @Override
      public List<WikiPage> getChildren() {
        throw new BrokenPageError();
      }
    };
    new ParallelTraversal(page -> {}, new NoPruningStrategy(), true, 4).traverse(brokenPage);
  }

  @Test
  public void stopsWhenInterrupted() {
    CountDownLatch neverReleased = new CountDownLatch(1);
    WikiPage slowPage = new WikiPageDummy("SlowPage", "", root) {
      @Override
      public List<WikiPage> getChildren() {
        try {
          neverReleased.await();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
        return Collections.emptyList();
      }
    };
    Thread.currentThread().interrupt();
    try {
      new ParallelTraversal(page -> {}, new NoPruningStrategy(), true, 4).traverse(slowPage);
      fail("expected traversal to stop");
    } catch (IllegalStateException e) {
      assertTrue(Thread.interrupted());
    }
  }

  private static class BrokenPageError extends Error {
  }

  private List<String> assertSameOrderAsPageCrawler(PagePruningStrategy strategy) {
    List<String> expected = new ArrayList<>();
    root.getPageCrawler().traverse(page -> expected.add(page.getFullPath().toString()), strategy);
    List<String> actual = new ArrayList<>();
    new ParallelTraversal(page -> actual.add(page.getFullPath().toString()), strategy, true, 4).traverse(root);
    assertEquals(expected, actual);
    return actual;
  }
}
