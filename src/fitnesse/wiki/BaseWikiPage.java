// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import java.util.*;

public abstract class BaseWikiPage implements WikiPage
{
	protected String name;
	protected WikiPage parent;

	protected BaseWikiPage(String name, WikiPage parent)
	{
		this.name = name;
		this.parent = parent;
	}

	public String getName() throws Exception
	{
		return name;
	}

	public PageCrawler getPageCrawler()
	{
		return new PageCrawlerImpl();
	}

	public WikiPage getParent() throws Exception
	{
		return parent == null ? this : parent;
	}

	protected abstract List getNormalChildren() throws Exception;

	public List getChildren() throws Exception
	{
		List children = getNormalChildren();
		WikiPageProperties props = getData().getProperties();
		for(Iterator iterator = props.getSymbolicLinkNames().iterator(); iterator.hasNext();)
		{
			String linkName = (String) iterator.next();
			WikiPage page = createSymbolicPage(props, linkName);
			if(page != null)
				children.add(page);
		}
		return children;
	}

	private WikiPage createSymbolicPage(WikiPageProperties props, String linkName) throws Exception
	{
		WikiPagePath path = props.getSymbolicLink(linkName);
		PageCrawler crawler = getPageCrawler();
		WikiPage page = crawler.getPage(crawler.getRoot(this), path);
		if(page != null)
			page = new SymbolicPage(linkName, page, this);
		return page;
	}

	protected abstract WikiPage getNormalChildPage(String name) throws Exception;
	public WikiPage getChildPage(String name) throws Exception
	{
		WikiPage page = getNormalChildPage(name);
		if(page == null)
			page = createSymbolicPage(getData().getProperties(), name);
		return page;
	}

	public String toString()
	{
		return this.getClass().getName() + ": " + name;
	}

	public int compareTo(Object o)
	{
		try
		{
			return getName().compareTo(((WikiPage) o).getName());
		}
		catch(Exception e)
		{
			return 0;
		}
	}

	public boolean equals(Object o)
	{
		if(this == o)
			return true;
		if(!(o instanceof WikiPage))
			return false;
		try
		{
			PageCrawler crawler = getPageCrawler();
			return crawler.getFullPath(this).equals(crawler.getFullPath(((WikiPage) o)));
		}
		catch(Exception e)
		{
			return false;
		}
	}

	public int hashCode()
	{
		try
		{
			return getPageCrawler().getFullPath(this).hashCode();
		}
		catch(Exception e)
		{
			return 0;
		}
	}
}
