// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.components;

import fitnesse.testutil.RegexTest;
import fitnesse.wiki.*;
import junit.swingui.TestRunner;

import java.util.List;

public class RecentChangesTest extends RegexTest
{
	private WikiPage rootPage;
	private WikiPage newPage;
	private WikiPage page1;
	private WikiPage page2;

	public static void main(String[] args)
	{
		TestRunner.main(new String[]{"RecentChangesTest"});
	}

	public void setUp() throws Exception
	{
		rootPage = InMemoryPage.makeRoot("RooT");
		newPage = rootPage.addChildPage("SomeNewPage");
		page1 = rootPage.addChildPage("PageOne");
		page2 = rootPage.addChildPage("PageTwo");
	}

	public void tearDown() throws Exception
	{
	}

	public void testFirstRecentChange() throws Exception
	{
		assertEquals(false, rootPage.hasChildPage("RecentChanges"));
		RecentChanges.updateRecentChanges(newPage.getData());
		assertEquals(true, rootPage.hasChildPage("RecentChanges"));
		WikiPage recentChanges = rootPage.getChildPage("RecentChanges");
		List lines = RecentChanges.getRecentChangesLines(recentChanges.getData());
		assertEquals(1, lines.size());
		assertHasRegexp("SomeNewPage", (String) lines.get(0));
	}

	public void testTwoChanges() throws Exception
	{
		RecentChanges.updateRecentChanges(page1.getData());
		RecentChanges.updateRecentChanges(page2.getData());
		WikiPage recentChanges = rootPage.getChildPage("RecentChanges");
		List lines = RecentChanges.getRecentChangesLines(recentChanges.getData());
		assertEquals(2, lines.size());
		assertHasRegexp("PageTwo", (String) lines.get(0));
		assertHasRegexp("PageOne", (String) lines.get(1));
	}

	public void testNoDuplicates() throws Exception
	{
		RecentChanges.updateRecentChanges(page1.getData());
		RecentChanges.updateRecentChanges(page1.getData());
		WikiPage recentChanges = rootPage.getChildPage("RecentChanges");
		List lines = RecentChanges.getRecentChangesLines(recentChanges.getData());
		assertEquals(1, lines.size());
		assertHasRegexp("PageOne", (String) lines.get(0));
	}

	public void testMaxSize() throws Exception
	{
		for(int i = 0; i < 101; i++)
		{
			StringBuffer b = new StringBuffer("LotsOfAs");
			for(int j = 0; j < i; j++)
				b.append("a");
			WikiPage page = rootPage.addChildPage(b.toString());
			RecentChanges.updateRecentChanges(page.getData());
		}

		WikiPage recentChanges = rootPage.getChildPage("RecentChanges");
		List lines = RecentChanges.getRecentChangesLines(recentChanges.getData());
		assertEquals(100, lines.size());
	}

	public void testUsernameColumnWithoutUser() throws Exception
	{
		RecentChanges.updateRecentChanges(page1.getData());
		WikiPage recentChanges = rootPage.getChildPage("RecentChanges");
		List lines = RecentChanges.getRecentChangesLines(recentChanges.getData());
		String line = lines.get(0).toString();
		assertSubString("|PageOne||", line);
	}

	public void testUsernameColumnWithUser() throws Exception
	{
		PageData data = page1.getData();
		data.setAttribute(WikiPage.LAST_MODIFYING_USER, "Aladdin");
		page1.commit(data);

		RecentChanges.updateRecentChanges(page1.getData());
		WikiPage recentChanges = rootPage.getChildPage("RecentChanges");
		List lines = RecentChanges.getRecentChangesLines(recentChanges.getData());
		String line = lines.get(0).toString();
		assertSubString("|PageOne|Aladdin|", line);
	}
}