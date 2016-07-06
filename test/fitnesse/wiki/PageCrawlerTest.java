// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.fs.InMemoryPage;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class PageCrawlerTest implements TraversalListener<WikiPage> {
  private WikiPage root;
  private WikiPage page1;
  private WikiPage page2;
  private WikiPage child1;
  private WikiPage child2;
  private WikiPage grandChild1;
  private PageCrawlerImpl crawler;
  private WikiPagePath page1Path;
  private WikiPagePath child1FullPath;
  private WikiPagePath page2Path;
  private WikiPagePath grandChild1FullPath;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = new PageCrawlerImpl(root);

    page1Path = PathParser.parse("PageOne");
    page2Path = PathParser.parse("PageTwo");
    child1FullPath = PathParser.parse("PageOne.ChildOne");
    grandChild1FullPath = PathParser.parse("PageOne.ChildOne.GrandChildOne");
    page1 = WikiPageUtil.addPage(root, page1Path, "");
    page2 = WikiPageUtil.addPage(root, page2Path, "");
    child1 = WikiPageUtil.addPage(page1, PathParser.parse("ChildOne"), "");
    child2 = WikiPageUtil.addPage(page1, PathParser.parse("ChildTwo"), "");
    grandChild1 = WikiPageUtil.addPage(child1, PathParser.parse("GrandChildOne"), "");
  }

  @Test
  public void testPageExists() throws Exception {
    assertTrue(page1.getPageCrawler().pageExists(PathParser.parse("ChildOne")));
    assertFalse(page1.getPageCrawler().pageExists(PathParser.parse("BlahBlah")));
  }

  @Test
  public void testPageExistsUsingPath() throws Exception {
    PageCrawler page1Crawler = new PageCrawlerImpl(page1);
    assertTrue(page1Crawler.pageExists(PathParser.parse("ChildOne")));
    assertTrue(crawler.pageExists(child1FullPath));
    assertTrue(crawler.pageExists(grandChild1FullPath));
    assertTrue(crawler.pageExists(PathParser.parse(".PageOne")));
    assertTrue(crawler.pageExists(PathParser.parse(".PageOne.ChildOne.GrandChildOne")));

    assertFalse(page1Crawler.pageExists(PathParser.parse("BlahBlah")));
    assertFalse(page1Crawler.pageExists(PathParser.parse("PageOne.BlahBlah")));
  }

  @Test
  public void testGetPage() throws Exception {
    assertEquals(null, page1.getPageCrawler().getPage(page1Path));
    assertEquals(page1, crawler.getPage(page1Path));
    assertEquals(page2, crawler.getPage(page2Path));
    assertEquals(page1, page1.getPageCrawler().getPage(PathParser.parse(".PageOne")));
    assertEquals(page1, grandChild1.getPageCrawler().getPage(PathParser.parse(".PageOne")));
    assertEquals(grandChild1, page1.getPageCrawler().getPage(PathParser.parse("ChildOne.GrandChildOne")));
    assertEquals(root, crawler.getPage(PathParser.parse("root")));
    assertEquals(root, crawler.getPage(PathParser.parse(".")));
    assertEquals(root, crawler.getPage(PathParser.parse("")));
  }

  @Test
  public void testGetSiblingPage() throws Exception {
    assertEquals(page2, page1.getPageCrawler().getSiblingPage(page2Path));
    assertEquals(child1, page1.getPageCrawler().getSiblingPage(PathParser.parse(">ChildOne")));
    assertEquals(child2, grandChild1.getPageCrawler().getSiblingPage(PathParser.parse("<PageOne.ChildTwo")));
  }

  @Test
  public void testGetFullPath() throws Exception {
    assertEquals(page1Path, page1.getPageCrawler().getFullPath());
    assertEquals(page2Path, page2.getPageCrawler().getFullPath());
    assertEquals(child1FullPath, child1.getPageCrawler().getFullPath());
    assertEquals(grandChild1FullPath, grandChild1.getPageCrawler().getFullPath());
    assertEquals(PathParser.parse(""), crawler.getFullPath());
  }

  @Test
  public void testGetAbsolutePathForChild() throws Exception {
    WikiPagePath somePagePath = PathParser.parse("SomePage");
    WikiPagePath somePageFullPath = crawler.getFullPathOfChild(somePagePath);
    assertEquals("SomePage", PathParser.render(somePageFullPath));

    WikiPagePath pageOnePath = page1Path;
    WikiPagePath pageOneFullPath = crawler.getFullPathOfChild(pageOnePath);
    assertEquals("PageOne", PathParser.render(pageOneFullPath));

    WikiPagePath SomePageChildFullPath = child1.getPageCrawler().getFullPathOfChild(somePagePath);
    assertEquals("PageOne.ChildOne.SomePage", PathParser.render(SomePageChildFullPath));

    WikiPagePath otherPagePath = PathParser.parse("SomePage.OtherPage");
    WikiPagePath otherPageFullPath = crawler.getFullPathOfChild(otherPagePath);
    assertEquals("SomePage.OtherPage", PathParser.render(otherPageFullPath));

    WikiPagePath somePageAbsolutePath = PathParser.parse(".SomePage");
    WikiPagePath somePageAbsoluteFullPath = child1.getPageCrawler().getFullPathOfChild(somePageAbsolutePath);
    assertEquals("SomePage", PathParser.render(somePageAbsoluteFullPath));
  }

  @Test
  public void testAddPage() throws Exception {
    WikiPage page = WikiPageUtil.addPage(page1, PathParser.parse("SomePage"));
    assertEquals(PathParser.parse("PageOne.SomePage"), page.getPageCrawler().getFullPath());
    assertEquals(page1, page.getParent());
  }

  @Test
  public void testRecursiveAddbyName() throws Exception {
    WikiPageUtil.addPage(root, PathParser.parse("AaAa"), "its content");
    assertTrue(root.hasChildPage("AaAa"));

    WikiPageUtil.addPage(root, PathParser.parse("AaAa.BbBb"), "floop");
    assertTrue(crawler.pageExists(PathParser.parse("AaAa.BbBb")));
    assertEquals("floop", crawler.getPage(PathParser.parse("AaAa.BbBb")).getData().getContent());
  }

  @Test
  public void testAddChildPageWithMissingParent() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("WikiMail.BadSubject0123"), "");
    assertNotNull(page);
    assertEquals("BadSubject0123", page.getName());
    assertEquals(PathParser.parse("WikiMail.BadSubject0123"), page.getPageCrawler().getFullPath());
  }

  @Test
  public void testGetRelativePageName() throws Exception {
    assertEquals("PageOne", crawler.getRelativeName(page1));
    assertEquals("PageOne.ChildOne", crawler.getRelativeName(child1));
    assertEquals("ChildOne", page1.getPageCrawler().getRelativeName(child1));
    assertEquals("GrandChildOne", child1.getPageCrawler().getRelativeName(grandChild1));
    assertEquals("ChildOne.GrandChildOne", page1.getPageCrawler().getRelativeName(grandChild1));
  }

  Set<String> traversedPages = new HashSet<>();

  @Test
  public void testTraversal() throws Exception {
    crawler.traverse(this);
    assertEquals(6, traversedPages.size());
    assertTrue(traversedPages.contains("PageOne"));
    assertTrue(traversedPages.contains("ChildOne"));
  }

  @Test
  public void doesTraverseSymbolicPages() throws Exception {
    PageData data = page1.getData();
    data.getProperties().set(SymbolicPage.PROPERTY_NAME).set("SymLink", page2.getName());
    page1.commit(data);

    crawler.traverse(this);
    assertEquals(7, traversedPages.size());

    assertTrue(traversedPages.contains("SymLink"));
  }

  @Test
  public void doesNotTraverseCyclicPageReferences() {
    PageData data = child1.getData();
    data.getProperties().set(SymbolicPage.PROPERTY_NAME).set("SymLink", "." + page1.getName());
    child1.commit(data);

    crawler = new PageCrawlerImpl(page1);
    crawler.traverse(this);

    assertEquals(traversedPages.toString(), 5, traversedPages.size());

    assertTrue(traversedPages.contains("SymLink"));
  }

  @Test
  public void canFindAllUncles() throws Exception {
    WikiPage grandUnclePage = WikiPageUtil.addPage(root, PathParser.parse("UnclePage"), "");
    WikiPage unclePage = WikiPageUtil.addPage(root, PathParser.parse("PageOne.UnclePage"), "");
    WikiPage brotherPage = WikiPageUtil.addPage(root, PathParser.parse("PageOne.ChildOne.UnclePage"), "");
    final List<WikiPage> uncles = new ArrayList<>();
    grandChild1.getPageCrawler().traverseUncles("UnclePage", new TraversalListener<WikiPage>() {
      @Override
      public void process(WikiPage page) {
        uncles.add(page);
      }
    });
    assertTrue(uncles.contains(grandUnclePage));
    assertTrue(uncles.contains(unclePage));
    assertTrue(uncles.contains(brotherPage));

  }

  @Override
  public void process(WikiPage page) {
    traversedPages.add(page.getName());
  }
}
