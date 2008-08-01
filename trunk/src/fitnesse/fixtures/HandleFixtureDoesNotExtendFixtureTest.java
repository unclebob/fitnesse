// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.fixtures;

import fit.*;
import fitnesse.util.StringUtil;
import junit.framework.TestCase;

import java.util.*;

public class HandleFixtureDoesNotExtendFixtureTest extends TestCase
{
	public void testLearnHowBadFixtureClassIsHandled() throws Exception
	{
		List tableLines = Arrays.asList(new String[]
			{"<table>",
				"    <tr>",
				"        <td>fitnesse.fixtures.WouldBeFixture</td>",
				"    </tr>",
				"</table>"});

		String tableText = StringUtil.join(tableLines, "\r\n");

		Parse tableForFaultyFixture = new Parse(tableText);

		new Fixture().doTables(tableForFaultyFixture);
		String fixtureClassCellText = tableForFaultyFixture.at(0, 0, 0).body;

		assertEquals("fitnesse.fixtures.WouldBeFixture<hr/> "
			+ "<span class=\"fit_label\">"
			+ "Class fitnesse.fixtures.WouldBeFixture is not a fixture." + "</span>",
		             fixtureClassCellText);
	}

}