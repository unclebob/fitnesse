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

	public void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		crawler = root.getPageCrawler();
		pageOne = crawler.addPage(root, PathParser.parse("PageOne"), "page one");
		pageTwo = crawler.addPage(root, PathParser.parse("PageTwo"), "page two");

		symPage = new SymbolicPage("SymPage", pageOne, pageTwo);
	}

	public void testCreation() throws Exception
	{
		assertEquals("SymPage", symPage.getName());
	}

	public void testLinkage() throws Exception
	{
		assertSame(pageOne, symPage.getRealPage());
	}

	public void testData() throws Exception
	{
		PageData data = symPage.getData();
		assertEquals("page one", data.getContent());
		assertSame(symPage, data.getWikiPage());
	}

	public void testCommit() throws Exception
	{
		PageData data = symPage.getData();
		data.setContent("new content");
		symPage.commit(data);

		data = pageOne.getData();
		assertEquals("new content", data.getContent());

		data = symPage.getData();
		assertEquals("new content", data.getContent());
	}

	public void testGetChild() throws Exception
	{
  	WikiPage childPage = crawler.addPage(pageOne, PathParser.parse("ChildPage"), "child page");
		WikiPage page = symPage.getChildPage("ChildPage");
		assertNotNull(page);
		assertEquals(SymbolicPage.class, page.getClass());
		SymbolicPage symChild = (SymbolicPage)page;
		assertSame(childPage, symChild.getRealPage());
	}

	public void testGetChildren() throws Exception
	{
		crawler.addPage(pageOne, PathParser.parse("ChildOne"), "child one");
		crawler.addPage(pageOne, PathParser.parse("ChildTwo"), "child two");
		List children = symPage.getChildren();
		assertEquals(2, children.size());
		assertEquals(SymbolicPage.class, children.get(0).getClass());
		assertEquals(SymbolicPage.class, children.get(1).getClass());
	}

	public void testCyclicSymbolicLinks() throws Exception
	{
		PageData data = pageOne.getData();
		data.getProperties().addSymbolicLink("SymTwo", PathParser.parse("PageTwo"));
		pageOne.commit(data);

		data = pageTwo.getData();
		data.getProperties().addSymbolicLink("SymOne", PathParser.parse("PageOne"));
		pageTwo.commit(data);

		WikiPage deepPage = crawler.getPage(root, PathParser.parse("PageOne.SymTwo.SymOne.SymTwo.SymOne.SymTwo"));
		List children = deepPage.getChildren();
		assertEquals(1, children.size());
	}
}
