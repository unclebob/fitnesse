// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.testutil;

import fit.*;

public class ErrorFixture extends Fixture
{
	public void doTable(Parse parse)
	{
		exception(parse, new Exception("Testing Exception"));
	}
}
