// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.fixtures;

public class RowEntryExample extends RowEntryFixture
{
	public int v;

	public void enterRow() throws Exception
	{
		if(v == 0)
			throw new Exception("Oh, no!  Zero!");
	}
}
