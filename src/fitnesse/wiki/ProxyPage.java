// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import fitnesse.http.ResponseParser;

import java.io.*;
import java.net.URL;
import java.util.*;

public class ProxyPage extends CachingPage implements Serializable
{
	public static int retrievalCount = 0;

	private String host;
	private int hostPort;
	private WikiPagePath realPath;
	public ResponseParser parser;
	private long lastLoadChildrenTime = 0;

	public ProxyPage(WikiPage original) throws Exception
	{
		super(original.getName(), null);
		realPath = original.getPageCrawler().getFullPath(original);

		List children = original.getChildren();
		for(Iterator iterator = children.iterator(); iterator.hasNext();)
		{
			ProxyPage child = new ProxyPage((WikiPage) iterator.next());
			child.parent = this;
			this.children.put(child.getName(), child);
		}
	}

	protected ProxyPage(String name, WikiPage parent) throws Exception
	{
		super(name, parent);
	}

	public ProxyPage(String name, WikiPage parent, String host, int port, WikiPagePath path) throws Exception
	{
		super(name, parent);
		this.host = host;
		hostPort = port;
		realPath = path;
	}

	public static ProxyPage retrievePage(String urlString) throws Exception
	{
		retrievalCount++;
		URL url = new URL(urlString + "?responder=proxy&type=bones");
		ProxyPage page = (ProxyPage) getObjectFromUrl(url);
		page.setTransientValues(url.getHost(), new Date().getTime());
		int port = url.getPort();
		page.setHostPort((port == -1) ? 80 : port);
		page.lastLoadChildrenTime = System.currentTimeMillis();
		return page;
	}

	protected WikiPage createChildPage(String name) throws Exception
	{
		WikiPagePath childPath = realPath.copy().addName(name);
		return new ProxyPage(name, this, host, getHostPort(), childPath);
	}

	protected void loadChildren() throws Exception
	{
		if(cacheTime <= (System.currentTimeMillis() - lastLoadChildrenTime))
		{
			ProxyPage page = retrievePage(getThisPageUrl());
			children.clear();
			for(Iterator iterator = page.children.values().iterator(); iterator.hasNext();)
			{
				ProxyPage child = (ProxyPage) iterator.next();
				child.parent = this;
				children.put(child.getName(), child);
			}
			lastLoadChildrenTime = System.currentTimeMillis();
		}
	}

	public String getThisPageUrl()
	{
		StringBuffer url = new StringBuffer("http://");
		url.append(host);
		url.append(":").append(getHostPort());
		url.append("/").append(PathParser.render(realPath));
		return url.toString();
	}

	public boolean hasChildPage(String pageName) throws Exception
	{
		if(children.containsKey(pageName))
			return true;
		else
		{
			loadChildren();
			return children.containsKey(pageName);
		}
	}

	public void setTransientValues(String host, long lastLoadTime)
	{
		this.host = host;
		lastLoadChildrenTime = lastLoadTime;
		for(Iterator i = children.values().iterator(); i.hasNext();)
		{
			ProxyPage page = (ProxyPage) i.next();
			page.setTransientValues(host, lastLoadTime);
		}
	}

	public String getHost()
	{
		return host;
	}

	public void setHostPort(int port)
	{
		hostPort = port;
		for(Iterator i = children.values().iterator(); i.hasNext();)
		{
			ProxyPage page = (ProxyPage) i.next();
			page.setHostPort(port);
		}
	}

	public int getHostPort()
	{
		return hostPort;
	}

	public PageData getMeat() throws Exception
	{
		return getMeat(null);
	}

	public PageData getMeat(String versionName) throws Exception
	{
		StringBuffer urlString = new StringBuffer(getThisPageUrl());
		urlString.append("?responder=proxy&type=meat");
		if(versionName != null)
			urlString.append("&version=").append(versionName);
		URL url = new URL(urlString.toString());
		PageData data = (PageData) getObjectFromUrl(url);
		if(data != null)
			data.setWikiPage(this);
		return data;
	}

	private static Object getObjectFromUrl(URL url) throws Exception
	{
		Object obj;
		InputStream is = url.openStream();
		ObjectInputStream ois = new ObjectInputStream(is);
		obj = ois.readObject();
		ois.close();
		return obj;
	}

	protected PageData makePageData() throws Exception
	{
		return getMeat();
	}

	public PageData getDataVersion(String versionName) throws Exception
	{
		PageData data = getMeat(versionName);
		if(data == null)
			throw new NoSuchVersionException("There is no version '" + versionName + "'");
		return data;
	}

	//TODO-MdM these are not needed
	// We expect this to go away when we do the checkout model
	protected VersionInfo makeVersion() throws Exception
	{
		return null;
	}

	protected void doCommit(PageData data) throws Exception
	{
	}
}
