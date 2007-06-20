// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders;

import fitnesse.wiki.WikiPage;

public class PageDataWikiPageResponder extends BasicWikiPageResponder
{
	protected String contentFrom(WikiPage requestedPage)
		throws Exception
	{
		return requestedPage.getData().getContent();
	}

}
