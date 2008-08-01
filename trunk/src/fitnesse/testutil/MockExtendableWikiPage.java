// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.testutil;

import fitnesse.wiki.*;

import java.util.List;

public class MockExtendableWikiPage extends ExtendableWikiPage
{
	public MockExtendableWikiPage(Extension e)
	{
		super("blah", null);
		addExtention(e);
	}

	public WikiPage getParent() throws Exception
	{
		return null;
	}

	public WikiPage addChildPage(String name) throws Exception
	{
		return null;
	}

	public boolean hasChildPage(String name) throws Exception
	{
		return false;
	}

	public WikiPage getNormalChildPage(String name) throws Exception
	{
		return null;
	}

	public void removeChildPage(String name) throws Exception
	{
	}

	public List getNormalChildren() throws Exception
	{
		return null;
	}

	public String getName() throws Exception
	{
		return null;
	}

	public PageData getData() throws Exception
	{
		return null;
	}

	public PageData getDataVersion(String versionName) throws Exception
	{
		return null;
	}

	public VersionInfo commit(PageData data) throws Exception
	{
		return null;
	}

	public int compareTo(Object o)
	{
		return 0;
	}
}
