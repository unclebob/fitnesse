// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import junit.framework.TestCase;
import java.util.List;

public class BaseWikiPageTest extends TestCase
{
	private BaseWikiPage root;
	private WikiPage linkingPage;

	public void setUp() throws Exception
	{
		root = (BaseWikiPage)InMemoryPage.makeRoot("RooT");
		root.addChildPage("LinkedPage");
		linkingPage = root.addChildPage("LinkingPage");
		linkingPage.addChildPage("ChildPage");
	}

	public void testGetChildrenUsesSymbolicPages() throws Exception
	{
		createLink();

		List children = linkingPage.getChildren();
		assertEquals(2, children.size());
		assertEquals("ChildPage", ((WikiPage)children.get(0)).getName());

		checkSymbolicPage(children.get(1));
	}

	public void testgetChildUsesSymbolicPages() throws Exception
	{
		createLink();
		checkSymbolicPage(linkingPage.getChildPage("SymLink"));
	}

	private void createLink() throws Exception
	{
		PageData data = linkingPage.getData();
		WikiPageProperties properties = data.getProperties();
		properties.addSymbolicLink("SymLink", PathParser.parse("LinkedPage"));
		linkingPage.commit(data);
	}

	private void checkSymbolicPage(Object page) throws Exception
	{
		assertEquals(SymbolicPage.class, page.getClass());
		SymbolicPage symPage = (SymbolicPage)page;
		assertEquals("SymLink", symPage.getName());
		assertEquals("LinkedPage", symPage.getRealPage().getName());
	}

}
