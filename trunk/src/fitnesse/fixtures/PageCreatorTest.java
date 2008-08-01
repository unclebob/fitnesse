// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.fixtures;

import fitnesse.wiki.*;
import junit.framework.TestCase;

public class PageCreatorTest extends TestCase
{
	protected void setUp() throws Exception
	{
		FitnesseFixtureContext.root = InMemoryPage.makeRoot("root");
	}

	public void testCreatePage() throws Exception
	{
		WikiPage testPage = makePage("TestPage", "contents", "attr=val");
		assertNotNull(testPage);
		PageData data = testPage.getData();
		assertEquals("contents", data.getContent());
		assertEquals("val", data.getAttribute("attr"));
	}

	private WikiPage makePage(String pageName, String pageContent, String pageAttributes) throws Exception
	{
		PageCreator creator = new PageCreator();
		creator.pageName = pageName;
		creator.pageContents = pageContent;
		creator.pageAttributes = pageAttributes;
		assertTrue(creator.valid());
		WikiPage testPage = FitnesseFixtureContext.root.getChildPage("TestPage");
		return testPage;
	}

	public void testMultipleAttributes() throws Exception
	{
		WikiPage testPage = makePage("TestPage", "Contents", "att1=one,att2=two");
		PageData data = testPage.getData();
		assertEquals("one", data.getAttribute("att1"));
		assertEquals("two", data.getAttribute("att2"));
	}
}
