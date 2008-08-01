// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.

package fitnesse.wiki;

import fitnesse.util.FileUtil;
import junit.framework.TestCase;

import java.io.File;
import java.util.*;

public class FileSystemPageTest extends TestCase
{
	private static final String defaultPath = "./teststorage";
	private static final File base = new File(defaultPath);
	private FileSystemPage root;
	private PageCrawler crawler;

	public void setUp() throws Exception
	{
		FileUtil.deleteFileSystemDirectory(base);
		createFileSystemDirectory(base);
		root = (FileSystemPage) FileSystemPage.makeRoot(defaultPath, "RooT");
		crawler = root.getPageCrawler();
	}

	public void tearDown() throws Exception
	{
		FileUtil.deleteFileSystemDirectory(base);
		FileUtil.deleteFileSystemDirectory("RooT");
	}

	public static void createFileSystemDirectory(File current)
	{
		current.mkdir();
	}

	public void testCreateBase() throws Exception
	{
		FileSystemPage levelA = (FileSystemPage) crawler.addPage(root, PathParser.parse("PageA"), "");
		assertEquals("./teststorage/RooT/PageA", levelA.getFileSystemPath());
		assertTrue(new File(defaultPath + "/RooT/PageA").exists());
	}

	public void testTwoLevel() throws Exception
	{
		WikiPage levelA = crawler.addPage(root, PathParser.parse("PageA"));
		crawler.addPage(levelA, PathParser.parse("PageB"));
		assertTrue(new File(defaultPath + "/RooT/PageA/PageB").exists());
	}

	public void testContent() throws Exception
	{
		WikiPagePath rootPath = PathParser.parse("root");
		assertEquals("", crawler.getPage(root, rootPath).getData().getContent());
		crawler.addPage(root, PathParser.parse("AaAa"), "A content");
		assertEquals("A content", root.getChildPage("AaAa").getData().getContent());
		WikiPagePath bPath = PathParser.parse("AaAa.BbBb");
		crawler.addPage(root, bPath, "B content");
		assertEquals("B content", crawler.getPage(root, bPath).getData().getContent());
	}

	public void testBigContent() throws Exception
	{
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < 1000; i++) buffer.append("abcdefghijklmnopqrstuvwxyz");
		crawler.addPage(root, PathParser.parse("BigPage"), buffer.toString());
		String content = root.getChildPage("BigPage").getData().getContent();
		assertTrue(buffer.toString().equals(content));
	}

	public void testPageExists() throws Exception
	{
		crawler.addPage(root, PathParser.parse("AaAa"), "A content");
		assertTrue(root.hasChildPage("AaAa"));
	}

	public void testGetChidren() throws Exception
	{
		crawler.addPage(root, PathParser.parse("AaAa"), "A content");
		crawler.addPage(root, PathParser.parse("BbBb"), "B content");
		crawler.addPage(root, PathParser.parse("CcCc"), "C content");
		new File(defaultPath + "/root/someOtherDir").mkdir();
		List children = root.getChildren();
		assertEquals(3, children.size());
		for(Iterator iterator = children.iterator(); iterator.hasNext();)
		{
			WikiPage child = (WikiPage) iterator.next();
			String name = child.getName();
			boolean isOk = "AaAa".equals(name) || "BbBb".equals(name) || "CcCc".equals(name);
			assertTrue("WikiPAge is not a valid one: " + name, isOk);
		}
	}

	public void testRemovePage() throws Exception
	{
		WikiPage levelOne = crawler.addPage(root, PathParser.parse("LevelOne"));
		crawler.addPage(levelOne, PathParser.parse("LevelTwo"));
		levelOne.removeChildPage("LevelTwo");
		File fileOne = new File(defaultPath + "/RooT/LevelOne");
		File fileTwo = new File(defaultPath + "/RooT/LevelOne/LevelTwo");
		assertTrue(fileOne.exists());
		assertFalse(fileTwo.exists());
	}

	public void testDelTree() throws Exception
	{
		FileSystemPage fsRoot = (FileSystemPage) FileSystemPage.makeRoot(".", "RooT");
		WikiPage levelOne = crawler.addPage(fsRoot, PathParser.parse("LevelOne"));
		crawler.addPage(levelOne, PathParser.parse("LevelTwo"));
		File childOne = new File("RooT/LevelOne");
		File childTwo = new File("RooT/LevelOne/LevelTwo");
		assertTrue(childOne.exists());
		FileUtil.deleteFileSystemDirectory(childOne);
		assertFalse(childTwo.exists());
		assertFalse(childOne.exists());
	}

	public void testDefaultAttributes() throws Exception
	{
		WikiPage page = crawler.addPage(root, PathParser.parse("PageOne"), "something");
		assertTrue(page.getData().hasAttribute("Edit"));
		assertTrue(page.getData().hasAttribute("Search"));
		assertFalse(page.getData().hasAttribute("Test"));
		assertFalse(page.getData().hasAttribute("TestSuite"));
	}

	public void testPersistentAttributes() throws Exception
	{
		crawler.addPage(root, PathParser.parse("FrontPage"));
		WikiPage createdPage = root.getChildPage("FrontPage");
		PageData data = createdPage.getData();
		data.setAttribute("Test", "true");
		data.setAttribute("Search", "true");
		createdPage.commit(data);
		assertTrue(data.hasAttribute("Test"));
		assertTrue(data.hasAttribute("Search"));
		WikiPage page = root.getChildPage("FrontPage");
		assertTrue(page.getData().hasAttribute("Test"));
		assertTrue(page.getData().hasAttribute("Search"));
	}

	public void testCachedInfo() throws Exception
	{
		WikiPage page1 = crawler.addPage(root, PathParser.parse("PageOne"), "page one");
		WikiPage child1 = crawler.addPage(page1, PathParser.parse("ChildOne"), "child one");
		WikiPage child = page1.getChildPage("ChildOne");
		assertSame(child1, child);
	}

	public void testCanFindExistingPages() throws Exception
	{
		crawler.addPage(root, PathParser.parse("FrontPage"), "front page");
		WikiPage newRoot = FileSystemPage.makeRoot(defaultPath, "RooT");
		assertNotNull(newRoot.getChildPage("FrontPage"));
	}

	public void testGetPath() throws Exception
	{
		assertEquals(defaultPath + "/RooT", root.getFileSystemPath());
	}

	public void testLastModifiedTime() throws Exception
	{
		WikiPage page = crawler.addPage(root, PathParser.parse("SomePage"), "some text");
		page.commit(page.getData());
		Date now = new Date();
		Date lastModified = page.getData().getProperties().getLastModificationTime();
		assertTrue(now.getTime() - lastModified.getTime() <= 1000);
	}

	public void testUnicodeCharacters() throws Exception
	{
		WikiPage page = crawler.addPage(root, PathParser.parse("SomePage"), "\uba80\uba81\uba82\uba83");
		PageData data = page.getData();
		assertEquals("\uba80\uba81\uba82\uba83", data.getContent());
	}

	public void testUnicodeInVersions() throws Exception
	{
		WikiPage page = crawler.addPage(root, PathParser.parse("SomePage"), "\uba80\uba81\uba82\uba83");
		PageData data = page.getData();
		data.setContent("blah");
		VersionInfo info = page.commit(data);

		data = page.getDataVersion(info.getName());
		String expected = "\uba80\uba81\uba82\uba83";
		String actual = data.getContent();

		assertEquals(expected, actual);
	}

	public void testLoadChildrenWhenPageIsDeletedManualy() throws Exception
	{
		WikiPage page = crawler.addPage(root, PathParser.parse("TestPage"));
		page.getChildren();
		FileUtil.deleteFileSystemDirectory(((FileSystemPage) page).getFileSystemPath());
		try
		{
			page.getChildren();
		}
		catch(Exception e)
		{
			fail("No Exception should be thrown");
		}
	}

	public void testVersionedPropertiedLoadedProperly() throws Exception
	{
		WikiPage page = crawler.addPage(root, PathParser.parse("TestPage"));
		PageData data = page.getData();
		WikiPageProperties oldProps = data.getProperties();
		WikiPageProperties props = new WikiPageProperties();
		props.set("MyProp", "my value");
		data.setProperties(props);
		page.commit(data);

		data.setProperties(oldProps);
		VersionInfo version = page.commit(data);

		PageData versionedData = page.getDataVersion(version.getName());
		WikiPageProperties versionedProps = versionedData.getProperties();

		assertTrue(versionedProps.has("MyProp"));
		assertEquals("my value", versionedProps.get("MyProp"));
	}
}
