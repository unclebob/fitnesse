// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
    assertTrue(path.isAbsolute());
    assertTrue(path.isEmpty());
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
    assertNull(makePath("bob"));
    assertNull(makePath("bobMartin"));
    assertNull(makePath("_root"));
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

    assertEquals(".", PathParser.render(PathParser.parse(".")));

    assertEquals("<MyPage", PathParser.render(makePath("<MyPage")));
    assertEquals(">MyPage", PathParser.render(makePath(">MyPage")));
  }
}
