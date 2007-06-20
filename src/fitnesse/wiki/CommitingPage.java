// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

public abstract class CommitingPage extends ExtendableWikiPage
{
	protected CommitingPage(String name, WikiPage parent)
	{
		super(name, parent);
	}

	protected abstract VersionInfo makeVersion() throws Exception;

	protected abstract void doCommit(PageData data) throws Exception;

	public VersionInfo commit(PageData data) throws Exception
	{
		VersionInfo previousVersion = makeVersion();
		doCommit(data);
		return previousVersion;
	}

}
