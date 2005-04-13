// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import java.util.*;

public class VirtualCouplingPage implements WikiPage
{
	private WikiPage hostPage;
	private HashMap children = new HashMap();

	protected VirtualCouplingPage(WikiPage hostPage)
	{
		this.hostPage = hostPage;
	}

	public VirtualCouplingPage(WikiPage hostPage, WikiPage proxy) throws Exception
	{
		this.hostPage = hostPage;
		List proxyChildren = proxy.getChildren();
		for(Iterator iterator = proxyChildren.iterator(); iterator.hasNext();)
		{
			CommitingPage wikiPage = (CommitingPage) iterator.next();
			wikiPage.parent = this;
			children.put(wikiPage.getName(), wikiPage);
		}
	}

	public boolean hasChildPage(String pageName) throws Exception
	{
		return children.containsKey(pageName);
	}

	public PageData getData() throws Exception
	{
		return hostPage.getData();
	}

	public int compareTo(Object o)
	{
		return 0;
	}

	public WikiPage addChildPage(String name) throws Exception
	{
		return null;
	}

	public void removeChildPage(String name) throws Exception
	{
	}

	public PageData getDataVersion(String versionName) throws Exception
	{
		return null;
	}

	public WikiPage getParent() throws Exception
	{
		return hostPage.getParent();
	}

	public String getName() throws Exception
	{
		return hostPage.getName();
	}

	public WikiPage getChildPage(String name) throws Exception
	{
		WikiPage subpage = (WikiPage) children.get(name);
		if(subpage == null) subpage = hostPage.getChildPage(name);
		return subpage;
	}

	public List getChildren() throws Exception
	{
		return new ArrayList(children.values());
	}

	public VersionInfo commit(PageData data) throws Exception
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

	public PageCrawler getPageCrawler()
	{
		return hostPage.getPageCrawler();
	}
}