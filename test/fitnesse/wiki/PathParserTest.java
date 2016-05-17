// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import org.junit.Test;

import static org.junit.Assert.*;

public class PathParserTest {
  public WikiPagePath path;

  private WikiPagePath makePath(String pathName) {
    WikiPagePath path = PathParser.parse(pathName);
    return path;
  }

  @Test
  public void testSimpleName() throws Exception {
    path = makePath("ParentPage");
    assertEquals("ParentPage", path.getFirst());
    assertTrue(path.getRest().isEmpty());
  }

  @Test
  public void testSimpleLowercaseName() throws Exception {
    path = makePath("parent_page");
    assertEquals("parent_page", path.getFirst());
    assertTrue(path.getRest().isEmpty());
  }

  @Test
  public void testTwoComponentName() throws Exception {
    path = makePath("ParentPage.ChildPage");
    assertEquals("ParentPage", path.getFirst());
    assertEquals("ChildPage", path.getRest().getFirst());
    assertTrue(path.getRest().getRest().isEmpty());
  }

  @Test
  public void testAbsolutePath() throws Exception {
    path = makePath(".ParentPage.ChildPage");
    assertTrue(path.isAbsolute());
    assertEquals("ParentPage", path.getFirst());
    assertEquals("ChildPage", path.getRest().getFirst());
    assertTrue(path.getRest().getRest().isEmpty());
  }

  @Test
  public void testRoot() throws Exception {
    path = makePath("root");
    String name = PathParser.render(path);
    assertTrue(path.isAbsolute());
    assertTrue(path.isEmpty());
    assertEquals("root", name);
  }

  @Test
  public void testDot() throws Exception {
    path = makePath(".");
    assertTrue(path.isAbsolute());
    assertTrue(path.isEmpty());
  }

  @Test
  public void testEmptyString() throws Exception {
    path = makePath("");
    assertTrue(path.isEmpty());
  }

  @Test
  public void testInvalidNames() throws Exception {
    assertNull(makePath("&bob"));
    assertNull(makePath("+bobMartin"));
  }

  @Test
  public void testSubPagePath() throws Exception {
    path = makePath(">MySubPagePath.ChildPage");
    assertTrue(path.isSubPagePath());
    assertEquals("MySubPagePath", path.getFirst());
    assertEquals("ChildPage", path.getRest().getFirst());
    assertTrue(path.getRest().getRest().isEmpty());
  }

  @Test
  public void testBackwardSearchPath() throws Exception {
    path = makePath("<MySubPagePath.ChildPage");
    assertTrue(path.isBackwardSearchPath());
    assertEquals("MySubPagePath", path.getFirst());
    assertEquals("ChildPage", path.getRest().getFirst());
    assertTrue(path.getRest().getRest().isEmpty());
  }

  @Test
  public void testRender() throws Exception {
    assertEquals("MyPage", PathParser.render(makePath("MyPage")));
    assertEquals(".MyPage", PathParser.render(makePath(".MyPage")));

    WikiPagePath p = PathParser.parse(".MyPage");
    p.makeAbsolute();
    assertEquals(".MyPage", PathParser.render(p));

    assertEquals("root", PathParser.render(PathParser.parse(".")));
    assertEquals("root", PathParser.render(PathParser.parse("root")));

    assertEquals("<MyPage", PathParser.render(makePath("<MyPage")));
    assertEquals(">MyPage", PathParser.render(makePath(">MyPage")));
  }

  @Test
  public void testIsSingleWikiWord() throws Exception {
    assertTrue(PathParser.isSingleWikiWord("WikiWord"));
    assertTrue(PathParser.isSingleWikiWord("anotherWikiWord"));
    assertFalse(PathParser.isSingleWikiWord("NotSingle.WikiWord"));
    assertFalse(PathParser.isSingleWikiWord("WikiW\u00F0rd"));
    assertFalse(PathParser.isSingleWikiWord("files"));
    assertFalse(PathParser.isSingleWikiWord("root"));
  }


  @Test
  public void isValidWikiPath() {
    assertWikiPath("SomePage");
    assertWikiPath("SomePage.AnotherPage");
    assertWikiPath("SomePage.someotherpage");
    assertWikiPath(".SomePage.someotherpage");
    assertWikiPath("<SomePage.someotherpage");
    assertWikiPath(">SomePage.someotherpage");
  }

  @Test
  public void absoluteWikiPathWithNonWikiWords() {
    assertWikiPath(".FrontPage.Environments.Env1.TestSuites.SuiteSetUp");
    path = PathParser.parse(".FrontPage.Environments.Env1.TestSuites.SuiteSetUp");

    assertFalse(path == null);
  }

  private void assertWikiPath(String path) {
    assertTrue(path, PathParser.isWikiPath(path));
  }

}
