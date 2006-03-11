// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System.Text.RegularExpressions;
using fit;

namespace fitnesse.handlers
{
	public class SymbolSaveHandler : AbstractSymbolHandler
	{
		private static Regex matchExpression =
			new Regex("^>>");
		public override bool Match(string searchString, System.Type type)
		{
			return matchExpression.IsMatch(searchString);
		}

		public override void HandleCheck(Fixture fixture, Parse cell, Accessor accessor)
		{
			string symbol = ExtractSymbol(cell);
			object value = accessor.Get(fixture);
			Fixture.Save(symbol, value);
			cell.SetBody(Fixture.Gray(value + " &gt;&gt;"  + symbol));
		}
	}
}