// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using fit;

namespace fitnesse.handlers
{
	public class BlankKeywordHandler : AbstractCellHandler
	{
		public override bool Match(string searchString, Type type)
		{
			return searchString.ToLower() == "blank";
		}

		public override void HandleInput(Fixture fixture, Parse cell, Accessor accessor)
		{
			accessor.Set(fixture, "");
		}

		public override bool HandleEvaluate(Fixture fixture, Parse cell, Accessor accessor)
		{
			object actual = accessor.Get(fixture);
			if (null == actual)
			{
				return false;
			}
			else if ("".Equals(actual.ToString()))
			{
				return true;
			}
			else
			{
				return false;
			}
		}

	}
}