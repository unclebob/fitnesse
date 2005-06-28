// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System.Text.RegularExpressions;
using fit;

namespace fitnesse.handlers
{
	public class SymbolRecallHandler : AbstractSymbolHandler
	{
		public override bool Match(string searchString, System.Type type)
		{
			return Regex.IsMatch(searchString, "^<<");
		}

		public override void HandleInput(Fixture fixture, Parse cell, Accessor accessor)
		{
			string symbol = ExtractSymbol(cell);
			object value = Fixture.Recall(symbol);
			accessor.Set(fixture, value);
			cell.SetBody(value == null ? "null" : value.ToString() + Fixture.Gray("&lt;&lt;" + symbol));
		}

		public override bool HandleEvaluate(Fixture fixture, Parse cell, Accessor accessor) 
		{
			string symbol = ExtractSymbol(cell);
			object value = Fixture.Recall(symbol);
			cell.SetBody(value == null ? "null" : value.ToString() + Fixture.Gray("&lt;&lt;" + symbol));
			if (value == null)
				return false;
			return TypeAdapter.AreEqual(accessor.Get(fixture), value);
		}
	}
}