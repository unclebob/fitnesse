// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

import junit.framework.TestCase;

public class CountsTest extends TestCase
{
	public void testEquality() throws Exception
	{
		assertFalse(new Counts().equals(null));
		assertFalse(new Counts().equals(""));

		assertEquals(new Counts(), new Counts());
		assertEquals(new Counts(0, 0, 0, 0), new Counts(0, 0, 0, 0));
		assertEquals(new Counts(1, 1, 1, 1), new Counts(1, 1, 1, 1));
		assertEquals(new Counts(5, 0, 1, 3), new Counts(5, 0, 1, 3));

		assertFalse(new Counts(1, 0, 0, 0).equals(new Counts(0, 0, 0, 0)));
	}
}