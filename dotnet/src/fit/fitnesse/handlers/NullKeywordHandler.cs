// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using fit;

namespace fitnesse.handlers
{
	public class NullKeywordHandler : AbstractCellHandler
	{
		public override bool Match(string searchString, Type type)
		{
			return searchString.ToLower() == "null";
		}

		public override void HandleInput(Fixture fixture, Parse cell, Accessor accessor)
		{
			accessor.Set(fixture, null);
		}

		public override bool HandleEvaluate(Fixture fixture, Parse cell, Accessor accessor)
		{
			return accessor.Get(fixture) == null;
		}
	}
}