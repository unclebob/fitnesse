// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using fit;

namespace fitnesse.handlers
{
	public class EmptyCellHandler : AbstractCellHandler
	{
		public override bool Match(string searchString, Type type)
		{
			return "".Equals(searchString);
		}

		public override void HandleInput(Fixture fixture, Parse cell, Accessor accessor)
		{
			if (!accessor.AccessesMethodWithAtLeastOneParameter())
			{
				HandleCheck(fixture, cell, accessor);
			}
		}

		public override void HandleCheck(Fixture fixture, Parse cell, Accessor accessor)
		{
			if (null == accessor.Get(fixture))
			{
				cell.AddToBody(Fixture.Gray("null"));
			}
			else if ("".Equals(accessor.Get(fixture).ToString()))
			{
				cell.AddToBody(Fixture.Gray("blank"));
			}
			else
			{
				cell.AddToBody(Fixture.Gray(accessor.Get(fixture).ToString()));
			}
		}
	}
}