// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.fixtures;

import fit.ColumnFixture;

public class ColumnFixtureTestFixture extends ColumnFixture
{
	public int input;

	public int output() {return input;}

	public boolean exception() throws Exception {throw new Exception("I thowed up");}
}
