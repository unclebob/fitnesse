// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.testutil;

import fit.ColumnFixture;

public class VectorSum extends ColumnFixture
{
	public CartesianVector v1;
	public CartesianVector v2;

	public CartesianVector sum()
	{
		return v1.add(v2);
	}
}
