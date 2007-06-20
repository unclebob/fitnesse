// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.testutil.RegexTest;

import java.util.regex.*;

public abstract class WidgetTest extends RegexTest
{
	protected void assertMatchEquals(String value, String expected)
	{
		Matcher match = Pattern.compile(getRegexp(), Pattern.DOTALL | Pattern.MULTILINE).matcher(value);
		if(expected != null)
		{
			assertTrue("pattern not found in: " + value, match.find());
			assertEquals(expected, match.group());
		}
		else
		{
			boolean found = match.find();
			assertTrue((found ? match.group() : "nothing") + " was found in: " + value, !found);
		}
	}

	protected void assertMatches(String s)
	{
		assertMatchEquals(s, s);
	}

	protected void assertNoMatch(String s)
	{
		assertMatchEquals(s, null);
	}

	protected abstract String getRegexp();
}
