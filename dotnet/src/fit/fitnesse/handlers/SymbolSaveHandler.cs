// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using System.Text.RegularExpressions;
using fit;

namespace fitnesse.handlers
{
	public class SymbolSaveHandler : AbstractSymbolHandler
	{
		public override bool Match(string searchString, System.Type type)
		{
			return Regex.IsMatch(searchString, "^>>");
		}

		public override void HandleCheck(Fixture fixture, Parse cell, Accessor accessor)
		{
			Fixture.Save(ExtractSymbol(cell), accessor.Get(fixture));
		}
	}
}