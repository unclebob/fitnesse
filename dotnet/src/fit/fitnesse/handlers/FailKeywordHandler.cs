// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using System.Text.RegularExpressions;
using fit;

namespace fitnesse.handlers
{
	public class FailKeywordHandler : AbstractCellHandler
	{
		private static Regex matchExpression = 
			new Regex("^fail\\[.*\\]$");
		public override bool Match(string searchString, Type type)
		{
			return matchExpression.IsMatch(searchString);
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