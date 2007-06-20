// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.authentication;

import fitnesse.FitNesseContext;
import fitnesse.http.Request;
import fitnesse.wiki.*;

import java.util.*;

public abstract class SecurePageOperation implements SecureOperation
{
	protected abstract String getSecurityMode();

	public boolean shouldAuthenticate(FitNesseContext context, Request request) throws Exception
	{
		WikiPagePath path = PathParser.parse(request.getResource());
		PageCrawler crawler = context.root.getPageCrawler();
		crawler.setDeadEndStrategy(new VirtualMockingPageCrawler());
		WikiPage page = crawler.getPage(context.root, path);
		if(page == null)
			return false;

		List ancestors = WikiPageUtil.getAncestorsStartingWith(page);
		for(Iterator iterator = ancestors.iterator(); iterator.hasNext();)
		{
			WikiPage ancestor = (WikiPage) iterator.next();
			if(hasSecurityModeAttribute(ancestor))
				return true;
		}
		return false;
	}

	private boolean hasSecurityModeAttribute(WikiPage ancestor) throws Exception
	{
		PageData data = ancestor.getData();
		boolean hasSecurityMode = data.hasAttribute(getSecurityMode());
		return hasSecurityMode;
	}
}
