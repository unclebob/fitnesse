// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import junit.framework.TestCase;

public class WikiPagePathTest extends TestCase {
  public WikiPagePath path;

  protected void setUp() throws Exception {
    path = new WikiPagePath();
  }

  public void testEmptyPath() throws Exception {
    assertTrue(path.isEmpty());
    assertNull(path.getFirst());
    assertTrue(path.getRest().isEmpty());
  }

  public void testAddOneName() throws Exception {
    path.addNameToEnd("bob");
    assertEquals("bob", path.getFirst());
    assertFalse(path.isEmpty());
    assertTrue(path.getRest().isEmpty());
  }

  public void testAddTwoNames() throws Exception {
    path.addNameToEnd("bob");
    path.addNameToEnd("martin");
    assertFalse(path.isEmpty());
    assertEquals("bob", path.getFirst());
    WikiPagePath rest = path.getRest();
    assertNotNull(rest);
    assertEquals("martin", rest.getFirst());
    assertTrue(rest.getRest().isEmpty());
  }

  public void testRenderEmptyPath() throws Exception {
    String renderedPath = PathParser.render(path);
    assertEquals("", renderedPath);
  }

  public void testRenderSimplePath() throws Exception {
    path.addNameToEnd("Bob");
    String renderedPath = PathParser.render(path);
    assertEquals("Bob", renderedPath);
  }

  public void testRenderComplexPaths() throws Exception {
    path.addNameToEnd("Bob");
    path.addNameToEnd("Martin");
    String renderedPath = PathParser.render(path);
    assertEquals("Bob.Martin", renderedPath);
  }

  public void testPop() throws Exception {
    path.addNameToEnd("Micah");
    path.removeNameFromEnd();
    assertEquals("", PathParser.render(path));

    path.addNameToEnd("Micah");
    path.addNameToEnd("Martin");
    path.removeNameFromEnd();
    assertEquals("Micah", PathParser.render(path));

    path.removeNameFromEnd();
    assertEquals("", PathParser.render(path));
  }

  public void testConstructorWithPage() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    PageCrawler crawler = root.getPageCrawler();
    WikiPagePath abcdPath = PathParser.parse("AaA.BbB.CcC.DdD");
    WikiPagePath grandchildPath = PathParser.parse("PageOne.ChildOne.GrandChildOne");

    WikiPage page1 = crawler.addPage(root, abcdPath);
    WikiPage page2 = crawler.addPage(root, grandchildPath);

    WikiPagePath page1Path = new WikiPagePath(page1);
    assertEquals(abcdPath, page1Path);
    WikiPagePath page2Path = new WikiPagePath(page2);
    assertEquals(grandchildPath, page2Path);
  }

  public void testAppend() throws Exception {
    WikiPagePath path = new WikiPagePath();
    WikiPagePath a = PathParser.parse("PageA");
    WikiPagePath pageAPath = path.append(a);
    assertEquals("PageA", PathParser.render(pageAPath));

    WikiPagePath pageBPath = PathParser.parse("PageB");
    WikiPagePath pageABPath = pageAPath.append(pageBPath);
    assertEquals("PageA.PageB", PathParser.render(pageABPath));
  }

  public void testIsAbsolute() throws Exception {
    assertTrue(PathParser.parse(".AbsolutePage").isAbsolute());
    assertFalse(PathParser.parse("RelativePage").isAbsolute());
  }

  public void testGetRelativePath() throws Exception {
    WikiPagePath somePageAbsolutePath = PathParser.parse(".SomePage");
    assertEquals("SomePage", PathParser.render(somePageAbsolutePath.relativePath()));

    WikiPagePath somePagePath = PathParser.parse("SomePage");
    assertEquals("SomePage", PathParser.render(somePagePath.relativePath()));
  }

  public void testEquals() throws Exception {
    assertEquals(PathParser.parse("PageOne"), PathParser.parse("PageOne"));
    assertFalse(PathParser.parse("PageOne").equals(PathParser.parse("PageTwo")));
    assertFalse(PathParser.parse("PageOne").equals("a string"));
    assertEquals(PathParser.parse("PageOne.PageTwo"), PathParser.parse("PageOne.PageTwo"));
    assertFalse(PathParser.parse("PageOne.PageTwo").equals(PathParser.parse("PageOne.PageThree")));
    assertFalse(PathParser.parse("PageOne").equals(PathParser.parse(".PageOne")));
  }

  public void testCompareTo() throws Exception {
    WikiPagePath a = PathParser.parse("PageA");
    WikiPagePath ab = PathParser.parse("PageA.PageB");
    WikiPagePath b = PathParser.parse("PageB");
    WikiPagePath aa = PathParser.parse("PageA.PageA");
    WikiPagePath bb = PathParser.parse("PageB.PageB");
    WikiPagePath ba = PathParser.parse("PageB.PageA");

    assertTrue(a.compareTo(a) == 0);    // a == a
    assertTrue(a.compareTo(b) != 0);    // a != b
    assertTrue(ab.compareTo(ab) == 0);  // ab == ab
    assertTrue(a.compareTo(b) == -1);   // a < b
    assertTrue(aa.compareTo(ab) == -1); // aa < ab
    assertTrue(ba.compareTo(bb) == -1); // ba < bb
    assertTrue(b.compareTo(a) == 1);    // b > a
    assertTrue(ab.compareTo(aa) == 1);  // ab > aa
    assertTrue(bb.compareTo(ba) == 1);  // bb > ba
  }

  public void testMakeAbsolute() throws Exception {
    WikiPagePath p = PathParser.parse("PathOne");
    p.makeAbsolute();
    assertTrue(p.isAbsolute());

    WikiPagePath empty = new WikiPagePath();
    empty.makeAbsolute();
    assertTrue(empty.isAbsolute());
  }

  public void testAbsoluteModeIsMutuallyExclusive() throws Exception {
    path = PathParser.parse("<MyPage");
    assertTrue(path.isBackwardSearchPath());
    path.makeAbsolute();
    assertFalse(path.isBackwardSearchPath());
  }

  public void testParentPath() throws Exception {
    WikiPagePath path2 = new WikiPagePath();
    assertEquals(path2, path.parentPath());

    path.addNameToEnd("AbC");
    assertEquals(path2, path.parentPath());

    path.addNameToEnd("XyZ");
    path2.addNameToEnd("AbC");
    assertEquals(path2, path.parentPath());
  }

  public void testEquality() throws Exception {
    WikiPagePath path1 = new WikiPagePath();
    WikiPagePath path2 = new WikiPagePath();

    assertEquals(path1, path2);
    assertEquals(path2, path1);

    path1.addNameToEnd("AbC");
    assertFalse(path1.equals(path2));
    assertFalse(path2.equals(path1));

    path2.addNameToEnd("AbC");
    assertEquals(path1, path2);

    path1.addNameToEnd("XyZ");
    path2.addNameToEnd("XyZ");
    assertEquals(path1, path2);

    path1.removeNameFromEnd();
    assertFalse(path1.equals(path2));
    path2.removeNameFromEnd();
    assertEquals(path1, path2);
  }

  public void testClone() throws Exception {
    WikiPagePath abs = PathParser.parse(".MyPage");
    WikiPagePath rel = PathParser.parse("MyPage");
    WikiPagePath sub = PathParser.parse(">MyPage");
    WikiPagePath back = PathParser.parse("<MyPage");
    assertEquals(abs, abs.clone());
    assertEquals(rel, rel.clone());
    assertEquals(sub, sub.clone());
    assertEquals(back, back.clone());
  }

  public void testStartsWith() throws Exception {
    WikiPagePath path2 = new WikiPagePath();
    assertTrue(path2.startsWith(path));

    path.addNameToEnd("AbC");
    assertTrue(path.startsWith(path2));

    path2.addNameToEnd("AbC");
    assertTrue(path.startsWith(path2));

    path.addNameToEnd("DeF");
    assertTrue(path.startsWith(path2));

    path2.addNameToEnd("XyZ");
    assertFalse(path.startsWith(path2));

    path2.removeNameFromEnd();
    path2.addNameToEnd("DeF");
    assertTrue(path.startsWith(path2));

    path2.addNameToEnd("XyZ");
    assertFalse(path.startsWith(path2));
  }

  public void testWithNameAdded() throws Exception {
    WikiPagePath path2 = new WikiPagePath();
    path2.addNameToEnd("AbC");
    WikiPagePath path3 = path.withNameAdded("AbC");
    assertEquals(path2, path3);
    assertNotSame(path3, path2);
    assertNotSame(path3, path);
  }

  public void testSubstract() throws Exception {
    WikiPagePath path123 = new WikiPagePath(new String[]{"OnE", "TwO", "ThreE"});
    WikiPagePath path12 = new WikiPagePath(new String[]{"OnE", "TwO"});
    WikiPagePath path1 = new WikiPagePath(new String[]{"OnE"});
    WikiPagePath blah = new WikiPagePath(new String[]{"BlaH"});

    assertEquals(new WikiPagePath(new String[]{"ThreE"}), path123.subtractFromFront(path12));
    assertEquals(new WikiPagePath(new String[]{"TwO", "ThreE"}), path123.subtractFromFront(path1));
    assertEquals(new WikiPagePath(new String[]{"TwO"}), path12.subtractFromFront(path1));
    assertEquals(path123, path123.subtractFromFront(blah));
    assertEquals(new WikiPagePath(), path123.subtractFromFront(path123));
  }
}
