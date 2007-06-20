// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.components;

import fitnesse.testutil.RegexTest;
import fitnesse.util.WildcardTest;
import fitnesse.wiki.*;

public class ClassPathBuilderTest extends RegexTest
{
	private WikiPage root;
	private ClassPathBuilder builder;
	String pathSeparator = System.getProperty("path.separator");
	private PageCrawler crawler;
	private WikiPagePath somePagePath;

	public void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		crawler = root.getPageCrawler();
		builder = new ClassPathBuilder();
		somePagePath = PathParser.parse("SomePage");
	}

	public void testGetClasspath() throws Exception
	{
		crawler.addPage(root, PathParser.parse("TestPage"),
		                "!path fitnesse.jar\n" +
			                "!path my.jar");
		String expected = "fitnesse.jar" + pathSeparator + "my.jar";
		assertEquals(expected, builder.getClasspath(root.getChildPage("TestPage")));
	}

	public void testGetPaths_OneLevel() throws Exception
	{
		String pageContent = "This is some content\n" +
			"!path aPath\n" +
			"end of conent\n";
		WikiPage root = InMemoryPage.makeRoot("RooT");
		WikiPage page = crawler.addPage(root, PathParser.parse("ClassPath"), pageContent);
		String path = builder.getClasspath(page);
		assertEquals("aPath", path);
	}

	public void testGetClassPathMultiLevel() throws Exception
	{
		WikiPage root = InMemoryPage.makeRoot("RooT");
		crawler.addPage(root, PathParser.parse("ProjectOne"),
		                "!path path2\n" +
			                "!path path 3");
		crawler.addPage(root, PathParser.parse("ProjectOne.TesT"), "!path path1");

		String cp = builder.getClasspath(crawler.getPage(root, PathParser.parse("ProjectOne.TesT")));
		assertSubString("path1", cp);
		assertSubString("path2", cp);
		assertSubString("\"path 3\"", cp);
	}

	public void testLinearClassPath() throws Exception
	{
		WikiPage root = InMemoryPage.makeRoot("RooT");
		WikiPage superPage = crawler.addPage(root, PathParser.parse("SuperPage"), "!path superPagePath");
		WikiPage subPage = crawler.addPage(superPage, PathParser.parse("SubPage"), "!path subPagePath");
		String cp = builder.getClasspath(subPage);
		assertEquals("subPagePath" + pathSeparator + "superPagePath", cp);

	}

	public void testGetClassPathFromPageThatDoesntExist() throws Exception
	{
		String classPath = makeClassPathFromSimpleStructure("somePath");

		assertEquals("somePath", classPath);
	}

	private String makeClassPathFromSimpleStructure(String path) throws Exception
	{
		PageData data = root.getData();
		data.setContent("!path " + path);
		root.commit(data);
		crawler = root.getPageCrawler();
		crawler.setDeadEndStrategy(new MockingPageCrawler());
		WikiPage page = crawler.getPage(root, somePagePath);
		String classPath = builder.getClasspath(page);
		return classPath;
	}

	public void testThatPathsWithSpacesGetQuoted() throws Exception
	{
		crawler.addPage(root, somePagePath, "!path Some File.jar");
		crawler = root.getPageCrawler();
		crawler.setDeadEndStrategy(new MockingPageCrawler());
		WikiPage page = crawler.getPage(root, somePagePath);

		assertEquals("\"Some File.jar\"", builder.getClasspath(page));

		crawler.addPage(root, somePagePath, "!path somefile.jar\n!path Some Dir/someFile.jar");
		assertEquals("somefile.jar" + pathSeparator + "\"Some Dir/someFile.jar\"", builder.getClasspath(page));
	}

	public void testWildCardExpansion() throws Exception
	{
		try
		{
			WildcardTest.makeSampleFiles();

			String classPath = makeClassPathFromSimpleStructure("testDir/*.jar");
			assertHasRegexp("one\\.jar", classPath);
			assertHasRegexp("two\\.jar", classPath);

			classPath = makeClassPathFromSimpleStructure("testDir/*.dll");
			assertHasRegexp("one\\.dll", classPath);
			assertHasRegexp("two\\.dll", classPath);

			classPath = makeClassPathFromSimpleStructure("testDir/one*");
			assertHasRegexp("one\\.dll", classPath);
			assertHasRegexp("one\\.jar", classPath);
			assertHasRegexp("oneA", classPath);
		}
		finally
		{
			WildcardTest.deleteSampleFiles();
		}
	}
}
