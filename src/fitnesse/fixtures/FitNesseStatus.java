// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.fixtures;

import fit.ColumnFixture;

public class FitNesseStatus extends ColumnFixture
{
	public boolean isRunning()
	{
		return FitnesseFixtureContext.fitnesse.isRunning();
	}
}
