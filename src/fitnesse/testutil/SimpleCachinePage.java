// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.testutil;

import fitnesse.wiki.*;

import java.util.Date;

public class SimpleCachinePage extends CachingPage
{
	private PageData data;

	public SimpleCachinePage(String name, WikiPage parent) throws Exception
	{
		super(name, parent);
	}

	public boolean hasChildPage(String pageName) throws Exception
	{
		return hasCachedSubpage(pageName);
	}

	protected WikiPage createChildPage(String name) throws Exception
	{
		return new SimpleCachinePage(name, this);
	}

	protected void loadChildren() throws Exception
	{
	}

	protected PageData makePageData() throws Exception
	{
		if(data == null)
			return new PageData(this, "some content");
		else
			return new PageData(data);
	}

	protected VersionInfo makeVersion() throws Exception
	{
		return new VersionInfo("abc", "Jon", new Date());
	}

	protected void doCommit(PageData data) throws Exception
	{
		this.data = data;
	}

	public PageData getDataVersion(String versionName) throws Exception
	{
		return new PageData(this, "content from version " + versionName);
	}
}
