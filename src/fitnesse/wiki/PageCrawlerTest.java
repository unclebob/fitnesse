// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import fitnesse.components.FitNesseTraversalListener;

public class PageCrawlerTest extends TestCase implements FitNesseTraversalListener {
  private WikiPage root;
  private WikiPage page1;
  private WikiPage page2;
  private WikiPage child1;
  private WikiPage child2;
  private WikiPage grandChild1;
  private PageCrawler crawler;
  private WikiPagePath page1Path;
  private WikiPagePath child1FullPath;
  private WikiPagePath page2Path;
  private WikiPagePath grandChild1FullPath;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = new PageCrawlerImpl();

    page1Path = PathParser.parse("PageOne");
    page2Path = PathParser.parse("PageTwo");
    child1FullPath = PathParser.parse("PageOne.ChildOne");
    grandChild1FullPath = PathParser.parse("PageOne.ChildOne.GrandChildOne");
    page1 = crawler.addPage(root, page1Path);
    page2 = crawler.addPage(root, page2Path);
    child1 = crawler.addPage(page1, PathParser.parse("ChildOne"));
    child2 = crawler.addPage(page1, PathParser.parse("ChildTwo"));
    grandChild1 = crawler.addPage(child1, PathParser.parse("GrandChildOne"));
  }

  public void testPageExists() throws Exception {
    assertTrue(crawler.pageExists(page1, PathParser.parse("ChildOne")));
    assertFalse(crawler.pageExists(page1, PathParser.parse("BlahBlah")));
  }

  public void testPageExistsUsingPath() throws Exception {
    assertTrue(crawler.pageExists(page1, PathParser.parse("ChildOne")));
    assertTrue(crawler.pageExists(root, child1FullPath));
    assertTrue(crawler.pageExists(root, grandChild1FullPath));
    assertTrue(crawler.pageExists(root, PathParser.parse(".PageOne")));
    assertTrue(crawler.pageExists(root, PathParser.parse(".PageOne.ChildOne.GrandChildOne")));

    assertFalse(crawler.pageExists(page1, PathParser.parse("BlahBlah")));
    assertFalse(crawler.pageExists(page1, PathParser.parse("PageOne.BlahBlah")));
  }

  public void testGetPage() throws Exception {
    assertEquals(null, crawler.getPage(page1, page1Path));
    assertEquals(page1, crawler.getPage(root, page1Path));
    assertEquals(page2, crawler.getPage(root, page2Path));
    assertEquals(page1, crawler.getPage(page1, PathParser.parse(".PageOne")));
    assertEquals(page1, crawler.getPage(grandChild1, PathParser.parse(".PageOne")));
    assertEquals(grandChild1, crawler.getPage(page1, PathParser.parse("ChildOne.GrandChildOne")));
    assertEquals(root, crawler.getPage(root, PathParser.parse("root")));
    assertEquals(root, crawler.getPage(root, PathParser.parse(".")));
    assertEquals(root, crawler.getPage(root, PathParser.parse("")));
  }

  public void testGetSiblingPage() throws Exception {
    assertEquals(page2, crawler.getSiblingPage(page1, page2Path));
    assertEquals(child1, crawler.getSiblingPage(page1, PathParser.parse(">ChildOne")));
    assertEquals(child2, crawler.getSiblingPage(grandChild1, PathParser.parse("<PageOne.ChildTwo")));
  }

  public void testGetFullPath() throws Exception {
    assertEquals(page1Path, crawler.getFullPath(page1));
    assertEquals(page2Path, crawler.getFullPath(page2));
    assertEquals(child1FullPath, crawler.getFullPath(child1));
    assertEquals(grandChild1FullPath, crawler.getFullPath(grandChild1));
    assertEquals(PathParser.parse(""), crawler.getFullPath(root));
  }

  public void testGetAbsolutePathForChild() throws Exception {
    WikiPagePath somePagePath = PathParser.parse("SomePage");
    WikiPagePath somePageFullPath = crawler.getFullPathOfChild(root, somePagePath);
    assertEquals("SomePage", PathParser.render(somePageFullPath));

    WikiPagePath pageOnePath = page1Path;
    WikiPagePath pageOneFullPath = crawler.getFullPathOfChild(root, pageOnePath);
    assertEquals("PageOne", PathParser.render(pageOneFullPath));

    WikiPagePath SomePageChildFullPath = crawler.getFullPathOfChild(child1, somePagePath);
    assertEquals("PageOne.ChildOne.SomePage", PathParser.render(SomePageChildFullPath));

    WikiPagePath otherPagePath = PathParser.parse("SomePage.OtherPage");
    WikiPagePath otherPageFullPath = crawler.getFullPathOfChild(root, otherPagePath);
    assertEquals("SomePage.OtherPage", PathParser.render(otherPageFullPath));

    WikiPagePath somePageAbsolutePath = PathParser.parse(".SomePage");
    WikiPagePath somePageAbsoluteFullPath = crawler.getFullPathOfChild(child1, somePageAbsolutePath);
    assertEquals("SomePage", PathParser.render(somePageAbsoluteFullPath));
  }

  public void testAddPage() throws Exception {
    WikiPage page = crawler.addPage(page1, PathParser.parse("SomePage"));
    assertEquals(PathParser.parse("PageOne.SomePage"), crawler.getFullPath(page));
    assertEquals(page1, page.getParent());
  }

  public void testRecursiveAddbyName() throws Exception {
    crawler.addPage(root, PathParser.parse("AaAa"), "its content");
    assertTrue(root.hasChildPage("AaAa"));

    crawler.addPage(root, PathParser.parse("AaAa.BbBb"), "floop");
    assertTrue(crawler.pageExists(root, PathParser.parse("AaAa.BbBb")));
    assertEquals("floop", crawler.getPage(root, PathParser.parse("AaAa.BbBb")).getData().getContent());
  }

  public void testAddChildPageWithMissingParent() throws Exception {
    WikiPage page = crawler.addPage(root, PathParser.parse("WikiMail.BadSubject0123"), "");
    assertNotNull(page);
    assertEquals("BadSubject0123", page.getName());
    assertEquals(PathParser.parse("WikiMail.BadSubject0123"), crawler.getFullPath(page));
  }

  public void testGetRelativePageName() throws Exception {
    assertEquals("PageOne", crawler.getRelativeName(root, page1));
    assertEquals("PageOne.ChildOne", crawler.getRelativeName(root, child1));
    assertEquals("ChildOne", crawler.getRelativeName(page1, child1));
    assertEquals("GrandChildOne", crawler.getRelativeName(child1, grandChild1));
    assertEquals("ChildOne.GrandChildOne", crawler.getRelativeName(page1, grandChild1));
  }

  public void testIsRoot() throws Exception {
    assertTrue(crawler.isRoot(root));
    WikiPage page = crawler.addPage(root, page1Path);
    assertFalse(crawler.isRoot(page));
  }

  Set<String> traversedPages = new HashSet<String>();

  public void testTraversal() throws Exception {
    crawler.traverse(root, this);
    assertEquals(6, traversedPages.size());
    assertTrue(traversedPages.contains("PageOne"));
    assertTrue(traversedPages.contains("ChildOne"));
  }

  public void processPage(WikiPage page) throws Exception {
    traversedPages.add(page.getName());
  }

  public String getSearchPattern() throws Exception {
    return "blah";
  }

  public void testdoesntTraverseSymbolicPages() throws Exception {
    PageData data = page1.getData();
    data.getProperties().set(SymbolicPage.PROPERTY_NAME).set("SymLink", "PageTwo");
    page1.commit(data);

    crawler.traverse(root, this);
    assertEquals(6, traversedPages.size());

    assertFalse(traversedPages.contains("SymLink"));
  }
}
