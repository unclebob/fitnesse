// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import java.util.*;

public class FixtureListBuilder extends InheritedItemBuilder
{
	public List getFixtureNames(WikiPage page) throws Exception
	{
		return getInheritedItems(page, new HashSet(89));
	}

	protected List getItemsFromPage(WikiPage page) throws Exception
	{
		return page.getData().getFixtureNames();
	}
}
