// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
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

