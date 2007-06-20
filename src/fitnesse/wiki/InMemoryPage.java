// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import java.util.*;

public class InMemoryPage extends CommitingPage
{
	protected static final String currentVersionName = "current_version";

	protected Map versions = new HashMap();
	protected Map children = new HashMap();

	protected InMemoryPage(String name, String content, WikiPage parent) throws Exception
	{
		super(name, parent);
		addExtention(new VirtualCouplingExtension(this));
		versions.put(currentVersionName, new PageData(this, content));
	}

	public WikiPage addChildPage(String name) throws Exception
	{
		WikiPage page = createChildPage(name);
		children.put(name, page);
		return page;
	}

	public static WikiPage makeRoot(String name) throws Exception
	{
		InMemoryPage root = new InMemoryPage(name, "", null);
		return root;
	}

	public static WikiPage makeRoot(Properties props) throws Exception
	{
		return makeRoot(props.getProperty("FitNesseRoot", "FitNesseRoot"));
	}

	protected WikiPage createChildPage(String name) throws Exception
	{
		BaseWikiPage newPage = new InMemoryPage(name, "", this);
		children.put(newPage.getName(), newPage);
		return newPage;
	}

	public void removeChildPage(String name) throws Exception
	{
		children.remove(name);
	}

	public boolean hasChildPage(String pageName)
	{
		return children.containsKey(pageName);
	}

	protected VersionInfo makeVersion() throws Exception
	{
		PageData current = getDataVersion(currentVersionName);

		String name = String.valueOf(VersionInfo.nextId());
		VersionInfo version = makeVersionInfo(current, name);
		versions.put(version.getName(), current);
		return version;
	}

	protected WikiPage getNormalChildPage(String name) throws Exception
	{
		return (WikiPage) children.get(name);
	}

	public List getNormalChildren() throws Exception
	{
		return new LinkedList(children.values());
	}

	public PageData getData() throws Exception
	{
		return new PageData(getDataVersion(currentVersionName));
	}

	public void doCommit(PageData newData) throws Exception
	{
		newData.setWikiPage(this);
		newData.getProperties().setLastModificationTime(new Date());
		versions.put(currentVersionName, newData);
	}

	public PageData getDataVersion(String versionName) throws Exception
	{
		PageData version = (PageData) versions.get(versionName);
		if(version == null)
			throw new NoSuchVersionException("There is no version '" + versionName + "'");

		Set names = new HashSet(versions.keySet());
		names.remove(currentVersionName);
		List pageVersions = new LinkedList();
		for(Iterator iterator = names.iterator(); iterator.hasNext();)
		{
			String name = (String) iterator.next();
			PageData data = (PageData) versions.get(name);
			pageVersions.add(makeVersionInfo(data, name));
		}
		version.addVersions(pageVersions);
		return new PageData(version);
	}

	public int numberOfVersions()
	{
		return versions.size() - 1;
	}

	protected VersionInfo makeVersionInfo(PageData current, String name) throws Exception
	{
		String author = current.getAttribute(WikiPage.LAST_MODIFYING_USER);
		if(author == null)
			author = "";
		Date date = current.getProperties().getLastModificationTime();
		VersionInfo version = new VersionInfo(name, author, date);
		return version;
	}
}