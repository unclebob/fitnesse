// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.

package fitnesse.wiki;

import java.util.*;

public class WikiPageDummy implements WikiPage
{
	public String name;
	protected String location;
	private PageData pageData;
	private WikiPage parent;
	public static final int daysTillVersionsExpire = 14;

	public WikiPageDummy(String name, String content) throws Exception
	{
		this.name = name;
		pageData = new PageData(this, content);
	}

	public WikiPageDummy(String location)
	{
		this.location = location;
		name = "Default";
	}

	public WikiPageDummy()
	{
		location = null;
		name = "Default";
	}

	public String getName()
	{
		return name;
	}

	public WikiPage getParent()
	{
		return parent;
	}

	public void setParent(WikiPage parent)
	{
		this.parent = parent;
	}

	public PageData getData() throws Exception
	{
		return pageData;
	}

	public VersionInfo commit(PageData data) throws Exception
	{
		pageData = data;
		return new VersionInfo("mockVersionName", "mockAuthor", new Date());
	}

	public List getChildren()
	{
		return new ArrayList();
	}

	public int compareTo(Object o)
	{
		return 0;
	}

	public PageData getDataVersion(String versionName) throws Exception
	{
		return null;
	}

	public void removeChildPage(String name) throws Exception
	{
	}

	public PageCrawler getPageCrawler()
	{
		return new PageCrawlerImpl();
	}

	public WikiPage addChildPage(String name) throws Exception
	{
		return null;
	}

	public boolean hasChildPage(String name) throws Exception
	{
		return false;
	}

	public WikiPage getChildPage(String name) throws Exception
	{
		return null;
	}

	public boolean hasExtension(String extensionName)
	{
		return false;
	}

	public Extension getExtension(String extensionName)
	{
		return null;
	}
}