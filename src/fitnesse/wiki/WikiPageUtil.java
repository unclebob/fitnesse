// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import java.util.LinkedList;

public class WikiPageUtil
{
	public static LinkedList getAncestorsOf(WikiPage page) throws Exception
	{
		PageCrawler crawler = page.getPageCrawler();
		LinkedList ancestors = new LinkedList();
		WikiPage parent = page;
		do
		{
			parent = parent.getParent();
			ancestors.add(parent);
		} while(!crawler.isRoot(parent));

		return ancestors;
	}

	public static LinkedList getAncestorsStartingWith(WikiPage page) throws Exception
	{
		LinkedList ancestors = getAncestorsOf(page);
		ancestors.addFirst(page);
		return ancestors;
	}
}
