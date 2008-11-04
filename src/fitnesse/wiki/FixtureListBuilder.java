// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import java.util.*;

public class FixtureListBuilder extends InheritedItemBuilder
{
	public List<String> getFixtureNames(WikiPage page) throws Exception
	{
		return getInheritedItems(page, new HashSet<WikiPage>(89));
	}

	protected List<String> getItemsFromPage(WikiPage page) throws Exception
	{
		PageData pageData = page.getData();
		return pageData == null ? new ArrayList<String>() : pageData.getFixtureNames();
	}
}
