// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import fitnesse.testutil.RegexTest;

import java.util.List;

public class FixtureListBuilderTest extends RegexTest
{
	private FixtureListBuilder builder;
	private PageCrawler crawler;
	private WikiPage root;

	protected void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		crawler = root.getPageCrawler();
		builder = new FixtureListBuilder();
	}

	public void testGetFixtureNames() throws Exception
	{
		WikiPagePath levelOnePath = PathParser.parse("LevelOne");
		WikiPagePath levelTwoPath = PathParser.parse("LevelTwo");
		WikiPagePath levelThreePath = PathParser.parse("LevelThree");
		WikiPagePath fullPath = PathParser.parse("LevelOne.LevelTwo.LevelThree");

		WikiPage level1 = crawler.addPage(root, levelOnePath, "!fixture Fixture.One\r\nNot.A.Fixture\r\n!fixture FixtureTwo");
		WikiPage level2 = crawler.addPage(level1, levelTwoPath, "!fixture FixtureThree");
		crawler.addPage(level2, levelThreePath, "Level three");

		List fixtureNames = builder.getFixtureNames(crawler.getPage(root, fullPath));
		assertEquals(3, fixtureNames.size());
		assertEquals("Fixture.One", fixtureNames.get(1));
		assertEquals("FixtureTwo", fixtureNames.get(2));
		assertEquals("FixtureThree", fixtureNames.get(0));
	}

	public void testThatFixtureNamesAreGatherFromLinearInheritance() throws Exception
	{
		WikiPage parent = crawler.addPage(root, PathParser.parse("ParenT"), "!fixture parent");
		WikiPage child = crawler.addPage(parent, PathParser.parse("ChilD"), "!fixture child");
		List fixtureNames = builder.getFixtureNames(child);
		assertEquals(2, fixtureNames.size());
		assertTrue(fixtureNames.contains("parent"));
		assertTrue(fixtureNames.contains("child"));
	}
}