// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.

using fit;

namespace eg
{

	public class ArithmeticFixture : PrimitiveFixture 
	{

		int x=0;
		int y=0;

		public override void DoRows(Parse rows)
		{
			base.DoRows(rows.More);    // skip column heads
		}

		public override void DoCell(Parse cell, int column)
		{
			switch (column) 
			{
				case 0: x = (int)ParseLong(cell); break;
				case 1: y = (int)ParseLong(cell); break;
				case 2: Check(cell, x+y); break;
				case 3: Check(cell, x-y); break;
				case 4: Check(cell, x*y); break;
				case 5: Check(cell, x/y); break;
				default: Ignore(cell); break;
			}
		}
	}
}