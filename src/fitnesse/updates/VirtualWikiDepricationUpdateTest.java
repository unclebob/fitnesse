// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.updates;

import fitnesse.wiki.*;

public class VirtualWikiDepricationUpdateTest extends UpdateTest
{
	protected Update makeUpdate()
	{
		return new VirtualWikiDeprecationUpdate(updater);
	}

	public void testDoVisiting() throws Exception
	{
		WikiPage page = crawler.addPage(root, PathParser.parse("SomePage"), "!virtualwiki http://some.url");
		PageTraversingUpdate update2 = (PageTraversingUpdate) update;
		update2.processPage(page);

		PageData data = page.getData();
		assertEquals("http://some.url", data.getAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE));
	}
}
