// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import junit.framework.*;
import fitnesse.util.FileUtil;
import fitnesse.FitNesse;
import java.io.File;
import java.util.*;
import java.text.*;

public class FileSystemPageVersioningTest extends TestCase
{
	public FileSystemPage page;
	private VersionInfo firstVersion;
	private PageCrawler crawler;
	private WikiPage root;

	public void setUp() throws Exception
	{
		root = FileSystemPage.makeRoot("testDir", "RooT");
		crawler = root.getPageCrawler();
		page = (FileSystemPage) crawler.addPage(root, PathParser.parse("PageOne"), "original content");

		PageData data = page.getData();
		firstVersion = page.commit(data);
	}

	public void tearDown() throws Exception
	{
		FileUtil.deleteFileSystemDirectory("testDir");
	}

	public void testSave() throws Exception
	{
		String dirPath = page.getFileSystemPath();
		File dir = new File(dirPath);
		String[] filenames = dir.list();

		List list = Arrays.asList(filenames);
		assertTrue(list.contains(firstVersion + ".zip"));
	}

	public void testLoad() throws Exception
	{
		PageData data = page.getData();
		data.setContent("new content");
		VersionInfo version = page.commit(data);

		PageData loadedData = page.getDataVersion(version.getName());
		assertEquals("original content", loadedData.getContent());
	}

	public void testGetVersions() throws Exception
	{
		Set versionNames = page.getData().getVersions();
		assertEquals(1, versionNames.size());
		assertTrue(versionNames.contains(firstVersion));
	}

	public void testSubWikisDontInterfere() throws Exception
	{
		crawler.addPage(page, PathParser.parse("SubPage"), "sub page content");
		try
		{
			page.commit(page.getData());
		}
		catch(Exception e)
		{
			fail("this exception should not have been thrown: " + e.getMessage());
		}
	}

	public void testTwoVersions() throws Exception
	{
		PageData data = page.getData();
		data.setContent("new content");
		VersionInfo secondVersion = page.commit(data);
		Set versionNames = page.getData().getVersions();
		assertEquals(2,versionNames.size());
		assertTrue(versionNames.contains(firstVersion));
		assertTrue(versionNames.contains(secondVersion));
	}

	public void testVersionsExpire() throws Exception
	{
		PageVersionPruner.daysTillVersionsExpire = 3;
		PageData data = page.makePageData();
		Set versions = data.getVersions();
		for(Iterator iterator = versions.iterator(); iterator.hasNext();)
			page.removeVersion(iterator.next().toString());

		data.setLastModificationTime(FileSystemPage.dateFormat().parse("20031213000000"));
		page.makeVersion(data);
		data.setLastModificationTime(FileSystemPage.dateFormat().parse("20031214000000"));
		page.makeVersion(data);
		data.setLastModificationTime(FileSystemPage.dateFormat().parse("20031215000000"));
		page.makeVersion(data);
		data.setLastModificationTime(FileSystemPage.dateFormat().parse("20031216000000"));
		page.makeVersion(data);

		versions = page.makePageData().getVersions();
		PageVersionPruner.pruneVersions(page, versions);
		versions = page.makePageData().getVersions();
		assertEquals(3, versions.size());

		List versionsList = new LinkedList(versions);
		Collections.sort(versionsList);
		assertTrue(versionsList.get(0).toString().endsWith("20031214000000"));
		assertTrue(versionsList.get(1).toString().endsWith("20031215000000"));
		assertTrue(versionsList.get(2).toString().endsWith("20031216000000"));
	}

	public void testGetContent() throws Exception
	{
		WikiPagePath alpha = PathParser.parse("AlphaAlpha");
		WikiPage a = crawler.addPage(root, alpha, "a");

		PageData data = a.getData();
		assertEquals("a", data.getContent());
	}

	public void testReplaceContent() throws Exception
	{
		WikiPagePath alpha = PathParser.parse("AlphaAlpha");
		WikiPage page = crawler.addPage(root, alpha, "a");

		PageData data = page.getData();
		data.setContent("b");
		page.commit(data);
		assertEquals("b", page.getData().getContent());
	}

	public void testSetAttributes() throws Exception
	{
		PageData data = root.getData();
		data.setAttribute("Test", "true");
		data.setAttribute("Search", "true");
		root.commit(data);
		assertTrue(root.getData().hasAttribute("Test"));
		assertTrue(root.getData().hasAttribute("Search"));

		assertEquals("true", root.getData().getAttribute("Test"));
	}

	public void testSimpleVersionTasks() throws Exception
	{
		WikiPagePath path = PathParser.parse("MyPageOne");
		WikiPage page = crawler.addPage(root, path, "old content");
		PageData data = page.getData();
		data.setContent("new content");
		VersionInfo previousVersion = page.commit(data);

		data = page.getData();
		Set versions = data.getVersions();
		assertEquals(1, versions.size());
		assertEquals(true, versions.contains(previousVersion));

		PageData loadedData = page.getDataVersion(previousVersion.getName());
		assertSame(page, loadedData.getWikiPage());
		assertEquals("old content", loadedData.getContent());
	}

	public void testUserNameIsInVersionName() throws Exception
	{
		WikiPagePath testPagePath = PathParser.parse("TestPage");
		WikiPage testPage = crawler.addPage(root, testPagePath, "version1");

		PageData data = testPage.getData();
		data.setAttribute(WikiPage.LAST_MODIFYING_USER, "Aladdin");
		VersionInfo record = testPage.commit(data);

		data = testPage.getData();
		data.setAttribute(WikiPage.LAST_MODIFYING_USER, "Joe");
		record = testPage.commit(data);

		assertTrue(record.getName().startsWith("Aladdin"));
	}

	public void testNoVersionException() throws Exception
	{
		WikiPagePath pageOnePath = PathParser.parse("PageOne");
		WikiPage page = crawler.addPage(root, pageOnePath, "old content");
		try
		{
			page.getDataVersion("abc");
			fail("a NoSuchVersionException should have been thrown");
		}
		catch(NoSuchVersionException e)
		{
			assertEquals("There is no version 'abc'", e.getMessage());
		}
	}
}
