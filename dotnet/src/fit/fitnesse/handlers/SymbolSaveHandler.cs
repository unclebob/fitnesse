// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
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