// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.util.HashSet;
import java.util.Set;
import java.util.List;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.mem.InMemoryPage;
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
  private PageBuilder pageBuider;
  private PageCrawlerImpl crawler;
  private WikiPagePath page1Path;
  private WikiPagePath child1FullPath;
  private WikiPagePath page2Path;
  private WikiPagePath grandChild1FullPath;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    pageBuider = new PageBuilder();
    crawler = new PageCrawlerImpl(root);

    page1Path = PathParser.parse("PageOne");
    page2Path = PathParser.parse("PageTwo");
    child1FullPath = PathParser.parse("PageOne.ChildOne");
    grandChild1FullPath = PathParser.parse("PageOne.ChildOne.GrandChildOne");
    page1 = pageBuider.addPage(root, page1Path);
    page2 = pageBuider.addPage(root, page2Path);
    child1 = pageBuider.addPage(page1, PathParser.parse("ChildOne"));
    child2 = pageBuider.addPage(page1, PathParser.parse("ChildTwo"));
    grandChild1 = pageBuider.addPage(child1, PathParser.parse("GrandChildOne"));
  }

  @Test
  public void testPageExists() throws Exception {
    assertTrue(page1.getPageCrawler().pageExists(page1, PathParser.parse("ChildOne")));
    assertFalse(page1.getPageCrawler().pageExists(page1, PathParser.parse("BlahBlah")));
  }

  @Test
  public void testPageExistsUsingPath() throws Exception {
    PageCrawler page1Crawler = new PageCrawlerImpl(page1);
    assertTrue(page1Crawler.pageExists(page1, PathParser.parse("ChildOne")));
    assertTrue(crawler.pageExists(root, child1FullPath));
    assertTrue(crawler.pageExists(root, grandChild1FullPath));
    assertTrue(crawler.pageExists(root, PathParser.parse(".PageOne")));
    assertTrue(crawler.pageExists(root, PathParser.parse(".PageOne.ChildOne.GrandChildOne")));

    assertFalse(page1Crawler.pageExists(page1, PathParser.parse("BlahBlah")));
    assertFalse(page1Crawler.pageExists(page1, PathParser.parse("PageOne.BlahBlah")));
  }

  @Test
  public void testGetPage() throws Exception {
    assertEquals(null, page1.getPageCrawler().getPage(page1, page1Path));
    assertEquals(page1, crawler.getPage(root, page1Path));
    assertEquals(page2, crawler.getPage(root, page2Path));
    assertEquals(page1, page1.getPageCrawler().getPage(page1, PathParser.parse(".PageOne")));
    assertEquals(page1, grandChild1.getPageCrawler().getPage(grandChild1, PathParser.parse(".PageOne")));
    assertEquals(grandChild1, page1.getPageCrawler().getPage(page1, PathParser.parse("ChildOne.GrandChildOne")));
    assertEquals(root, crawler.getPage(root, PathParser.parse("root")));
    assertEquals(root, crawler.getPage(root, PathParser.parse(".")));
    assertEquals(root, crawler.getPage(root, PathParser.parse("")));
  }

  @Test
  public void testGetSiblingPage() throws Exception {
    assertEquals(page2, page1.getPageCrawler().getSiblingPage(page1, page2Path));
    assertEquals(child1, page1.getPageCrawler().getSiblingPage(page1, PathParser.parse(">ChildOne")));
    assertEquals(child2, grandChild1.getPageCrawler().getSiblingPage(grandChild1, PathParser.parse("<PageOne.ChildTwo")));
  }

  @Test
  public void testGetFullPath() throws Exception {
    assertEquals(page1Path, page1.getPageCrawler().getFullPath(page1));
    assertEquals(page2Path, page2.getPageCrawler().getFullPath(page2));
    assertEquals(child1FullPath, child1.getPageCrawler().getFullPath(child1));
    assertEquals(grandChild1FullPath, grandChild1.getPageCrawler().getFullPath(grandChild1));
    assertEquals(PathParser.parse(""), crawler.getFullPath(root));
  }

  @Test
  public void testGetAbsolutePathForChild() throws Exception {
    WikiPagePath somePagePath = PathParser.parse("SomePage");
    WikiPagePath somePageFullPath = crawler.getFullPathOfChild(root, somePagePath);
    assertEquals("SomePage", PathParser.render(somePageFullPath));

    WikiPagePath pageOnePath = page1Path;
    WikiPagePath pageOneFullPath = crawler.getFullPathOfChild(root, pageOnePath);
    assertEquals("PageOne", PathParser.render(pageOneFullPath));

    WikiPagePath SomePageChildFullPath = child1.getPageCrawler().getFullPathOfChild(child1, somePagePath);
    assertEquals("PageOne.ChildOne.SomePage", PathParser.render(SomePageChildFullPath));

    WikiPagePath otherPagePath = PathParser.parse("SomePage.OtherPage");
    WikiPagePath otherPageFullPath = crawler.getFullPathOfChild(root, otherPagePath);
    assertEquals("SomePage.OtherPage", PathParser.render(otherPageFullPath));

    WikiPagePath somePageAbsolutePath = PathParser.parse(".SomePage");
    WikiPagePath somePageAbsoluteFullPath = child1.getPageCrawler().getFullPathOfChild(child1, somePageAbsolutePath);
    assertEquals("SomePage", PathParser.render(somePageAbsoluteFullPath));
  }

  @Test
  public void testAddPage() throws Exception {
    WikiPage page = pageBuider.addPage(page1, PathParser.parse("SomePage"));
    assertEquals(PathParser.parse("PageOne.SomePage"), page.getPageCrawler().getFullPath(page));
    assertEquals(page1, page.getParent());
  }

  @Test
  public void testRecursiveAddbyName() throws Exception {
    pageBuider.addPage(root, PathParser.parse("AaAa"), "its content");
    assertTrue(root.hasChildPage("AaAa"));

    pageBuider.addPage(root, PathParser.parse("AaAa.BbBb"), "floop");
    assertTrue(crawler.pageExists(root, PathParser.parse("AaAa.BbBb")));
    assertEquals("floop", crawler.getPage(root, PathParser.parse("AaAa.BbBb")).getData().getContent());
  }

  @Test
  public void testAddChildPageWithMissingParent() throws Exception {
    WikiPage page = pageBuider.addPage(root, PathParser.parse("WikiMail.BadSubject0123"), "");
    assertNotNull(page);
    assertEquals("BadSubject0123", page.getName());
    assertEquals(PathParser.parse("WikiMail.BadSubject0123"), page.getPageCrawler().getFullPath(page));
  }

  @Test
  public void testGetRelativePageName() throws Exception {
    assertEquals("PageOne", crawler.getRelativeName(root, page1));
    assertEquals("PageOne.ChildOne", crawler.getRelativeName(root, child1));
    assertEquals("ChildOne", page1.getPageCrawler().getRelativeName(page1, child1));
    assertEquals("GrandChildOne", child1.getPageCrawler().getRelativeName(child1, grandChild1));
    assertEquals("ChildOne.GrandChildOne", page1.getPageCrawler().getRelativeName(page1, grandChild1));
  }

  @Test
  public void testIsRoot() throws Exception {
    assertTrue(crawler._isRoot(root));
    WikiPage page = pageBuider.addPage(root, page1Path);
    assertFalse(crawler._isRoot(page));
  }

  Set<String> traversedPages = new HashSet<String>();

  @Test
  public void testTraversal() throws Exception {
    crawler.traverse(root, this);
    assertEquals(6, traversedPages.size());
    assertTrue(traversedPages.contains("PageOne"));
    assertTrue(traversedPages.contains("ChildOne"));
  }

  public void process(WikiPage page) {
    traversedPages.add(page.getName());
  }

  @Test
  public void testdoesntTraverseSymbolicPages() throws Exception {
    PageData data = page1.getData();
    data.getProperties().set(SymbolicPage.PROPERTY_NAME).set("SymLink", "PageTwo");
    page1.commit(data);

    crawler.traverse(root, this);
    assertEquals(6, traversedPages.size());

    assertFalse(traversedPages.contains("SymLink"));
  }

  @Test
  public void canFindAllUncles() throws Exception {
    WikiPage grandUnclePage = pageBuider.addPage(root, PathParser.parse("UnclePage"));
    WikiPage unclePage = pageBuider.addPage(root, PathParser.parse("PageOne.UnclePage"));
    WikiPage brotherPage = pageBuider.addPage(root, PathParser.parse("PageOne.ChildOne.UnclePage"));
    List<WikiPage> uncles = grandChild1.getPageCrawler().getAllUncles(grandChild1, "UnclePage");
    assertTrue(uncles.contains(grandUnclePage));
    assertTrue(uncles.contains(unclePage));
    assertTrue(uncles.contains(brotherPage));

  }
}
