// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import junit.framework.TestCase;
import java.util.List;

public class SymbolicPageTest extends TestCase
{
	private PageCrawler crawler;
	private WikiPage root;
	private WikiPage pageOne;
	private WikiPage pageTwo;
	private SymbolicPage symPage;
	private String pageOnePath = "PageOne";
	private String pageTwoPath = "PageTwo";
	private String pageOneContent = "page one";
	private String pageTwoContent = "page two";

	public void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		crawler = root.getPageCrawler();
		pageOne = crawler.addPage(root, PathParser.parse(pageOnePath), pageOneContent);
		pageTwo = crawler.addPage(root, PathParser.parse(pageTwoPath), pageTwoContent);
		symPage = new SymbolicPage("SymPage", pageTwo, pageOne);
	}

	public void testCreation() throws Exception
	{
		assertEquals("SymPage", symPage.getName());
	}

	public void testLinkage() throws Exception
	{
		assertSame(pageTwo, symPage.getRealPage());
	}

	public void testInternalData() throws Exception
	{
		PageData data = symPage.getData();
		assertEquals(pageTwoContent, data.getContent());
		assertSame(symPage, data.getWikiPage());
	}

	public void testCommitInternal() throws Exception
	{
		PageData data = symPage.getData();
		data.setContent("new content");
		symPage.commit(data);

		data = pageTwo.getData();
		assertEquals("new content", data.getContent());

		data = symPage.getData();
		assertEquals("new content", data.getContent());

	}

	public void testGetChild() throws Exception
	{
  	WikiPage childPage = crawler.addPage(pageTwo, PathParser.parse("ChildPage"), "child page");
		WikiPage page = symPage.getChildPage("ChildPage");
		assertNotNull(page);
		assertEquals(SymbolicPage.class, page.getClass());
		SymbolicPage symChild = (SymbolicPage)page;
		assertSame(childPage, symChild.getRealPage());
	}

	public void testGetChildren() throws Exception
	{
		crawler.addPage(pageTwo, PathParser.parse("ChildOne"), "child one");
		crawler.addPage(pageTwo, PathParser.parse("ChildTwo"), "child two");
		List children = symPage.getChildren();
		assertEquals(2, children.size());
		assertEquals(SymbolicPage.class, children.get(0).getClass());
		assertEquals(SymbolicPage.class, children.get(1).getClass());
	}

	public void testCyclicSymbolicLinks() throws Exception
	{
		PageData data = pageOne.getData();
		data.getProperties().set("SymbolicLinks").set("SymOne", pageTwoPath);
		pageOne.commit(data);

		data = pageTwo.getData();
		data.getProperties().set("SymbolicLinks").set("SymTwo", pageOnePath);
		pageTwo.commit(data);

		WikiPage deepPage = crawler.getPage(root, PathParser.parse(pageOnePath + ".SymOne.SymTwo.SymOne.SymTwo.SymOne"));
		List children = deepPage.getChildren();
		assertEquals(1, children.size());

		deepPage = crawler.getPage(root, PathParser.parse(pageTwoPath + ".SymTwo.SymOne.SymTwo.SymOne.SymTwo"));
		children = deepPage.getChildren();
		assertEquals(1, children.size());
	}
}
