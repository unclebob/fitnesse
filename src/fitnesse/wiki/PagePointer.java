// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

public class PagePointer
{
	public WikiPage root;
	public WikiPagePath path;

	public PagePointer(WikiPage root, WikiPagePath path)
	{
		this.root = root;
		this.path = path;
	}

	public WikiPage getPage() throws Exception
	{
		return root.getPageCrawler().getPage(root, path);
	}
}
