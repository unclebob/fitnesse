// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.updates;

import fitnesse.wiki.*;

public class FrontPageUpdateTest extends UpdateTest
{
	protected Update makeUpdate() throws Exception
	{
		return new FrontPageUpdate(updater);
	}

	public void testShouldUpdate() throws Exception
	{
		assertTrue(update.shouldBeApplied());
		updater.getRoot().addChildPage("FrontPage");
		assertFalse(update.shouldBeApplied());
	}

	public void testProperties() throws Exception
	{
		update.doUpdate();
		WikiPage page = updater.getRoot().getChildPage("FrontPage");
		assertNotNull(page);

		PageData data = page.getData();
		assertTrue(data.hasAttribute("Edit"));
		assertTrue(data.hasAttribute("Properties"));
	}
}
