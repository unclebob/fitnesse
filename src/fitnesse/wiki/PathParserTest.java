// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import junit.framework.TestCase;

public class PathParserTest extends TestCase
{
	public WikiPagePath path;

	private WikiPagePath makePath(String pathName)
	{
		WikiPagePath path = PathParser.parse(pathName);
		return path;
	}

	public void testSimpleName() throws Exception
	{
		path = makePath("ParentPage");
		assertEquals("ParentPage", path.getFirst());
		assertTrue(path.getRest().isEmpty());
	}

	public void testTwoComponentName() throws Exception
	{
		path = makePath("ParentPage.ChildPage");
		assertEquals("ParentPage", path.getFirst());
		assertEquals("ChildPage", path.getRest().getFirst());
		assertTrue(path.getRest().getRest().isEmpty());
	}

	public void testAbsolutePath() throws Exception
	{
		path = makePath(".ParentPage.ChildPage");
		assertEquals(WikiPagePath.ROOT, path.getFirst());
		assertEquals("ParentPage", path.getRest().getFirst());
		assertEquals("ChildPage", path.getRest().getRest().getFirst());
		assertTrue(path.getRest().getRest().getRest().isEmpty());
	}

	public void testRoot() throws Exception
	{
		path = makePath("root");
		assertEquals(WikiPagePath.ROOT, path.getFirst());
		assertTrue(path.getRest().isEmpty());
	}

	public void testDot() throws Exception
	{
		path = makePath(".");
		assertEquals(WikiPagePath.ROOT, path.getFirst());
		assertTrue(path.getRest().isEmpty());
	}

	public void testEmptyString() throws Exception
	{
		path = makePath("");
		assertTrue(path.isEmpty());
	}

	public void testInvalidNames() throws Exception
	{
		assertNull(makePath("bob"));
		assertNull(makePath("bobMartin"));
		assertNull(makePath("_root"));
	}

	public void testRender() throws Exception
	{
		assertEquals("MyPage", PathParser.render(makePath("MyPage")));
		assertEquals(".MyPage", PathParser.render(makePath(".MyPage")));

		WikiPagePath p = PathParser.parse(".MyPage");
		p.makeAbsolute();
		assertEquals(".MyPage", PathParser.render(p));

		assertEquals(".", PathParser.render(PathParser.parse(".")));
		//todo RCM 2/7/05 More tests here.
	}
}
