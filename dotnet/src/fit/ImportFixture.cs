// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using fit;

namespace fit
{
	public class ImportFixture : Fixture
	{
		public override void DoCell(Parse cell, int columnNumber)
		{
			ObjectFactory.AddNamespace(cell.Text);
		}
	}
}

