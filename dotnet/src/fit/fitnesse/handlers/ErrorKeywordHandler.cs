// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using fit;

namespace fitnesse.handlers
{
	public class ErrorKeywordHandler : AbstractCellHandler
	{
		public override bool Match(string searchString, Type type)
		{
			return searchString.ToLower() == "error";
		}

		public override void HandleCheck(Fixture fixture, Parse cell, Accessor accessor)
		{
			try
			{
				accessor.Get(fixture);
			}
			catch
			{
				fixture.Right(cell);
				return;
			}
			fixture.Wrong(cell, accessor.Get(fixture).ToString());
		}
	}
}