// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.updates;

import fitnesse.wiki.*;

public class AttributeAdderUpdate extends PageTraversingUpdate
{
	private String attributeName;

	public AttributeAdderUpdate(Updater updater, String attributeName)
	{
		super(updater);
		this.attributeName = attributeName;
	}

	public String getName()
	{
		return attributeName + "AttributeUpdate";
	}

	public String getMessage()
	{
		return "Adding '" + attributeName + "' attribute to all pages";
	}

	public void processPage(WikiPage page) throws Exception
	{
		try
		{
			PageData data = page.getData();
			data.setAttribute(attributeName, "true");
			page.commit(data);
		}
		catch(Exception e)
		{
			String fullPathName = PathParser.render(page.getPageCrawler().getFullPath(page));
			System.out.println("Failed to add attribute " + attributeName + " to " + fullPathName);
			throw e;
		}
	}

	public String getSearchPattern()
	{
		return ".*";
	}
}

