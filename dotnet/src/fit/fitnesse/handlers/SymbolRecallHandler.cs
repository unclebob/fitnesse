// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using System.Text.RegularExpressions;

namespace fitnesse.handlers
{
	public class SymbolRecallHandler : AbstractSymbolHandler
	{
		public override bool Match(string searchString, System.Type type)
		{
			return Regex.IsMatch(searchString, "^<<");
		}
	}
}