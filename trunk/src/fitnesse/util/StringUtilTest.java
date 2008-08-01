// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.util;

import junit.framework.TestCase;

public class StringUtilTest extends TestCase
{
	public void testCombineArraysBothEmpty()
	{
		assertEquals(0, StringUtil.combineArrays(new String[]{}, new String[]{}).length);
	}

	public void testCombineArraysWithOneItemInFirst()
	{
		String[] first = new String[]{"a"};
		String[] result = StringUtil.combineArrays(first, new String[]{});
		assertEquals(1, result.length);
		assertEquals("a", result[0]);
	}

	public void testCombineArraysWithOneItemInEach()
	{
		String[] first = new String[]{"a"};
		String[] second = new String[]{"b"};
		String[] result = StringUtil.combineArrays(first, second);
		assertEquals(2, result.length);
		assertEquals("a", result[0]);
		assertEquals("b", result[1]);
	}

	public void testCombineArraysWithMixedNumbers()
	{
		String[] first = new String[]{"a", "b", "c"};
		String[] second = new String[]{"d", "e"};
		String[] result = StringUtil.combineArrays(first, second);
		assertEquals(5, result.length);
		assertEquals("a", result[0]);
		assertEquals("b", result[1]);
		assertEquals("c", result[2]);
		assertEquals("d", result[3]);
		assertEquals("e", result[4]);
	}
}
