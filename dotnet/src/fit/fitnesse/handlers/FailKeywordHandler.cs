using System;
using System.Text.RegularExpressions;
using fit;

namespace fitnesse.handlers
{
	public class FailKeywordHandler : AbstractCellHandler
	{
		public override bool Match(string searchString, Type type)
		{
			return Regex.IsMatch(searchString, "^fail\\[.*\\]$");
		}

		public override void HandleCheck(Fixture fixture, Parse cell, Accessor accessor)
		{
			string expected = cell.Text.Substring("fail[".Length, cell.Text.Length - ("fail[".Length + 1));
			ICellHandler handler = CellOperation.GetHandler(cell, accessor);
			Parse newCell = new Parse("td", expected, null, null);
			if (handler.HandleEvaluate(fixture, newCell, accessor))
			{
				fixture.Wrong(cell);
			}
			else
			{
				fixture.Right(cell);
			}

		}
	}
}