// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import fitnesse.testutil.*;
import junit.framework.TestCase;

public class ExtendableWikiPageTest extends TestCase
{
	public void testAddExtention() throws Exception
	{
		Extension e = new SimpleExtension();
		WikiPage page = new MockExtendableWikiPage(e);

		assertFalse(page.hasExtension("blah"));
		assertEquals(null, page.getExtension("blah"));

		assertTrue(page.hasExtension(e.getName()));
		assertSame(e, page.getExtension(e.getName()));
	}
}
