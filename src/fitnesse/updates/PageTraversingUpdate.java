// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.updates;

import fitnesse.components.FitNesseTraversalListener;
import fitnesse.wiki.*;

import java.util.Properties;

public abstract class PageTraversingUpdate implements FitNesseTraversalListener, Update
{
	private Properties properties;
	private WikiPage root;

	public PageTraversingUpdate(Updater updater)
	{
		properties = updater.getProperties();
		root = updater.getRoot();
	}

	public void doUpdate() throws Exception
	{
		root.getPageCrawler().traverse(root, this);
		properties.setProperty(getName(), "applied");
	}

	public abstract void processPage(WikiPage currentPage) throws Exception;

	public boolean shouldBeApplied() throws Exception
	{
		boolean usesFileSystem = root instanceof FileSystemPage;
		boolean hasBeenApplied = properties.getProperty(getName()) != null;

		return usesFileSystem && !hasBeenApplied;
	}

	public String getSearchPattern() throws Exception
	{
		return ".*";
	}
}
