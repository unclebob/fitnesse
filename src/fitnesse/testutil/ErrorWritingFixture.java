// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.testutil;

import fit.*;

public class ErrorWritingFixture extends Fixture
{
	public void doTable(Parse parse)
	{
		Parse cell = parse.parts.more.parts;
		String value = cell.text();
		System.err.println(value);
		right(cell);
	}
}
