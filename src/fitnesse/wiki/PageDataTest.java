// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import fitnesse.testutil.RegexTest;
import fitnesse.responders.run.*;

import java.util.*;
import java.text.SimpleDateFormat;

public class PageDataTest extends RegexTest
{
	public WikiPage page;
	private WikiPage root;
	private PageCrawler crawler;

	public void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		crawler = root.getPageCrawler();
		page = crawler.addPage(root, PathParser.parse("PagE"), "some content");
	}

	public void tearDown() throws Exception
	{
	}

	public void testVariablePreprocessing() throws Exception
	{
		PageData d = new PageData(InMemoryPage.makeRoot("RooT"), "!define x {''italic''}\n${x}\n");
		String preprocessedText = d.getContent();
		assertHasRegexp("''italic''", preprocessedText);
	}

	public void testVariablesRenderedFirst() throws Exception
	{
		String text = "!define x {''italics''}\n${x}";
		WikiPage root = InMemoryPage.makeRoot("RooT");
		WikiPage page = crawler.addPage(root, PathParser.parse("SomePage"), text);
		String html = page.getData().getHtml();
		assertHasRegexp("''italics''", html);
		assertHasRegexp("<i>italics</i>", html);
	}

	public void testThatSpecialCharsAreNotEscapedTwice() throws Exception
	{
		PageData d = new PageData(new MockWikiPage(), "<b>");
		String html = d.getHtml();
		assertEquals("&lt;b&gt;", html);
	}
//todo delete comment
// Disabled by Micah
//	public void testNestedVariables() throws Exception
//	{
//		String content = "!define x (!define y {123})\n" +
//		  "!define z (${y})\n" +
//		  "${x}\n" +
//		  "-${z}-";
//		PageData d = new PageData(page, content);
//		String text = d.preProcess();
//		assertSubString("-123-", text);
//	}
//
//	public void testInfiniteNestedVariables() throws Exception
//	{
//		String content = "!define x (${x})\n${x}";
//		PageData d = new PageData(page, content);
//		try
//		{
//			System.err.println(d.preProcess());
//			fail("Should throw recursive variables Exception");
//		}
//		catch(Exception e)
//		{
//		}
//	}

	public void testLiteral() throws Exception
	{
		WikiPage root = InMemoryPage.makeRoot("RooT");
		WikiPage page = crawler.addPage(root, PathParser.parse("LiteralPage"), "!-literal-!");
		String renderedContent = page.getData().getHtml();
		assertHasRegexp("literal", renderedContent);
		assertDoesntHaveRegexp("!-literal-!", renderedContent);
	}

	public void testClasspath() throws Exception
	{
		WikiPage root = InMemoryPage.makeRoot("RooT");
		WikiPage page = crawler.addPage(root, PathParser.parse("ClassPath"), "!path 123\n!path abc\n");
		List paths = page.getData().getClasspaths();
		assertTrue(paths.contains("123"));
		assertTrue(paths.contains("abc"));
	}

	public void testGetFixtureNames() throws Exception
	{
		WikiPage root = InMemoryPage.makeRoot("RooT");
		WikiPage page = crawler.addPage(root, PathParser.parse("PageName"), "!fixture FixtureOne\r\nNot.A.Fixture\r\n!fixture FixtureTwo\n\n!fixture FixtureThree");
		List fixtureNames = page.getData().getFixtureNames();
		assertEquals(3, fixtureNames.size());
		assertEquals("FixtureOne", fixtureNames.get(0));
		assertEquals("FixtureTwo", fixtureNames.get(1));
		assertEquals("FixtureThree", fixtureNames.get(2));
	}

	public void testGetCrossReferences() throws Exception
	{
		WikiPage root = InMemoryPage.makeRoot("RooT");
		WikiPage page = crawler.addPage(root, PathParser.parse("PageName"), "!see XrefPage\r\n");
		List xrefs = page.getData().getXrefPages();
		assertEquals("XrefPage", xrefs.get(0));
	}

	public void testDefaultAttributes() throws Exception
	{
		WikiPage normalPage = crawler.addPage(root, PathParser.parse("NormalPage"));
		WikiPage testPage = crawler.addPage(root, PathParser.parse("TestPage"));
		WikiPage suitePage = crawler.addPage(root, PathParser.parse("SuitePage"));
		WikiPage suiteSetupPage = crawler.addPage(root, PathParser.parse(SuiteResponder.SUITE_SETUP_NAME));
		WikiPage suiteTearDownPage = crawler.addPage(root, PathParser.parse(SuiteResponder.SUITE_TEARDOWN_NAME));

		PageData data = new PageData(normalPage);
		assertTrue(data.hasAttribute("Edit"));
		assertTrue(data.hasAttribute("Search"));
		assertTrue(data.hasAttribute("Versions"));
		assertTrue(data.hasAttribute("Files"));
		assertFalse(data.hasAttribute("Test"));
		assertFalse(data.hasAttribute("Suite"));

		data = new PageData(testPage);
		assertTrue(data.hasAttribute("Test"));
		assertFalse(data.hasAttribute("Suite"));

		data = new PageData(suitePage);
		assertFalse(data.hasAttribute("Test"));
		assertTrue(data.hasAttribute("Suite"));

		data = new PageData(suiteSetupPage);
		assertFalse(data.hasAttribute("Suite"));

		data = new PageData(suiteTearDownPage);
		assertFalse(data.hasAttribute("Suite"));
	}

	public void testAttributesAreTruelyCopiedInCopyConstructor() throws Exception
	{
		PageData data = root.getData();
		data.setAttribute(WikiPage.LAST_MODIFYING_USER, "Joe");
		PageData newData = new PageData(data);
		newData.setAttribute(WikiPage.LAST_MODIFYING_USER, "Jane");

		assertEquals("Joe", data.getAttribute(WikiPage.LAST_MODIFYING_USER));
	}

	public void testLastModificationTime() throws Exception
	{
		SimpleDateFormat format = PageData.makeVersionTimeFormat();
		PageData data = new PageData(root);
		assertEquals(format.format(new Date()), format.format(data.getLastModificationTime()));
		Date date = format.parse("20040101000001");
		data.setLastModificationTime(date);
		assertEquals("20040101000001", data.getProperties().get("LastModified"));
		assertEquals(date, data.getLastModificationTime());
	}
}
