// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using System.Text.RegularExpressions;
using fit;

namespace fitnesse.handlers
{
	public class StartsWithHandler : AbstractCellHandler
	{
		public override bool Match(string searchString, System.Type type)
		{
			return Regex.IsMatch(searchString, "^[^\\.\\.].*\\.\\.$");
		}

		public override bool HandleEvaluate(Fixture fixture, Parse cell, Accessor accessor)
		{
			if (GetActual(accessor, fixture) == null)
			{
				return false;
			}
			return GetActual(accessor, fixture).ToString().StartsWith(ExtractExpectedSubstring(cell.Text));
		}

		private static string ExtractExpectedSubstring(string text)
		{
			return text.Substring(0, text.Length - 2);
		}
	}
}